/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.java.database;

import android.content.Context;


import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.podcast.java.database.model.Favourites;
import com.huawei.podcast.java.database.model.PodCasts;
import com.huawei.podcast.java.preference.SharedPreference;
import com.huawei.podcast.java.utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class CloudDBZoneWrapper {
    private static final String TAG = Constants.CloudDBZoneWrapper;

    private final AGConnectCloudDB mCloudDB;

    private CloudDBZone mCloudDBZone;

    private ListenerHandler mRegister;

    private CloudDBZoneConfig mConfig;

    private UiCallBack mUiCallBack = UiCallBack.DEFAULT;

    /**
     * Monitor data change from database. Update podcast info list if data have changed
     */
    private OnSnapshotListener<Favourites> mSnapshotListener =
            new OnSnapshotListener<Favourites>() {
                @Override
                public void onSnapshot(
                        CloudDBZoneSnapshot<Favourites> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
                    if (e != null) {
                        return;
                    }
                    CloudDBZoneObjectList<Favourites> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
                    List<Favourites> favPodcastList = new ArrayList<>();
                    try {
                        if (snapshotObjects != null) {
                            while (snapshotObjects.hasNext()) {
                                Favourites favPodcast = snapshotObjects.next();
                                favPodcastList.add(favPodcast);
                            }
                        }
                        mUiCallBack.onSubscribe(favPodcastList);
                    } catch (AGConnectCloudDBException snapshotException) {
                    } finally {
                        cloudDBZoneSnapshot.release();
                    }
                }
            };

    public CloudDBZoneWrapper() {
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    /**
     * Init AGConnectCloudDB in Application
     *
     * @param context application context
     */
    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
        }
    }

    /**
     * Call AGConnectCloudDB.openCloudDBZone to open a cloudDBZone.
     * We set it with cloud cache mode, and data can be store in local storage
     */
    public void openCloudDBZoneV2() {
        mConfig =
                new CloudDBZoneConfig(
                        Constants.PODCAST_CLOUD,
                        CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                        CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask
                .addOnSuccessListener(
                        new OnSuccessListener<CloudDBZone>() {
                            @Override
                            public void onSuccess(CloudDBZone cloudDBZone) {
                                mCloudDBZone = cloudDBZone;
                                mUiCallBack.onDBReady(true);
                                // Add subscription after opening cloudDBZone success
                                addSubscription();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                            }
                        });
    }

    /**
     * Call AGConnectCloudDB.closeCloudDBZone
     */
    public void closeCloudDBZone() {
        try {
            mRegister.remove();
            mCloudDB.closeCloudDBZone(mCloudDBZone);
        } catch (AGConnectCloudDBException e) {
        }
    }

    /**
     * Call AGConnectCloudDB.deleteCloudDBZone
     */
    public void deleteCloudDBZone() {
        try {
            mCloudDB.deleteCloudDBZone(mConfig.getCloudDBZoneName());
        } catch (AGConnectCloudDBException e) {
        }
    }

    /**
     * Add a callback to update book info list
     *
     * @param uiCallBack callback to update book list
     */
    public void addCallBacks(UiCallBack uiCallBack) {
        mUiCallBack = uiCallBack;
    }

    /**
     * Add mSnapshotListener to monitor data changes from storage
     */
    public void addSubscription() {
        if (mCloudDBZone == null) {
            return;
        }
        try {
            CloudDBZoneQuery<Favourites> snapshotQuery =
                    CloudDBZoneQuery.where(Favourites.class).equalTo(Constants.FAV_STATUS, true);
            mRegister =
                    mCloudDBZone.subscribeSnapshot(
                            snapshotQuery,
                            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                            mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
        }
    }


    /**
     * Query all podcast in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     */
    public void getAllPodCasts() {
        if (mCloudDBZone == null) {
            return;
        }
        Task<CloudDBZoneSnapshot<PodCasts>> queryTask =
                mCloudDBZone.executeQuery(
                        CloudDBZoneQuery.where(PodCasts.class),
                        CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<CloudDBZoneSnapshot<PodCasts>>() {
                            @Override
                            public void onSuccess(CloudDBZoneSnapshot<PodCasts> snapshot) {
                                processPodCastsResult(snapshot);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                mUiCallBack.updateUiOnError(Constants.QUERY_FAILED);
                            }
                        });
    }

    /**
     * Query favouritePodcasts
     */
    public void getFavouritePodCasts() {
        if (mCloudDBZone == null) {
            return;
        }
        Task<CloudDBZoneSnapshot<Favourites>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Favourites.class).
                        equalTo(Constants.USER_ID, SharedPreference.getValueString(Constants.USER_ID)),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<CloudDBZoneSnapshot<Favourites>>() {
                            @Override
                            public void onSuccess(CloudDBZoneSnapshot<Favourites> snapshot) {
                                processQueryResult(snapshot);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                mUiCallBack.updateUiOnError(Constants.QUERY_FAILED);
                            }
                        });
    }

    private void processQueryResult(CloudDBZoneSnapshot<Favourites> snapshot) {
        CloudDBZoneObjectList<Favourites> favPodcastCursor = snapshot.getSnapshotObjects();
        List<Favourites> favPodcastList = new ArrayList<>();
        try {
            while (favPodcastCursor.hasNext()) {
                Favourites favPodcast = favPodcastCursor.next();
                favPodcastList.add(favPodcast);
            }
        } catch (AGConnectCloudDBException e) {
        } finally {
            snapshot.release();
        }
        mUiCallBack.onAddOrQuery(favPodcastList);
    }

    private void processPodCastsResult(CloudDBZoneSnapshot<PodCasts> snapshot) {
        CloudDBZoneObjectList<PodCasts> podCastCursor = snapshot.getSnapshotObjects();
        ArrayList<PodCasts> podCastList = new ArrayList<>();
        try {
            while (podCastCursor.hasNext()) {
                PodCasts podCast = podCastCursor.next();
                podCastList.add(podCast);
            }
        } catch (AGConnectCloudDBException e) {
        } finally {
            snapshot.release();
        }
        mUiCallBack.onGetPodcasts(podCastList);
    }

    /**
     * Upsert bookinfo
     *
     * @param bookInfo bookinfo added or modified from local
     */
    public void upsertFavPodCasts(Favourites bookInfo) {
        if (mCloudDBZone == null) {
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(bookInfo);
        upsertTask.addOnSuccessListener(
                        new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer cloudDBZoneResult) {
                                mUiCallBack.updateUiOnError(Constants.ADDED);
                            }
                        }).addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                mUiCallBack.updateUiOnError(Constants.ADDED_FAILED);
                            }
                        });
    }

    /**
     * Delete bookinfo
     *
     * @param favouritePodcast books selected by user
     */
    public void deleteFavPodCast(Favourites favouritePodcast) {
        if (mCloudDBZone == null) {
            return;
        }
        Task<Integer> deleteTask = mCloudDBZone.executeDelete(favouritePodcast);
        deleteTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                mUiCallBack.onDelete(true);
            }
        });
        deleteTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiCallBack.updateUiOnError(Constants.DELETE_QUERY_FAILED);
            }
        });

    }

    /**
     * Call back to update ui in HomePageFragment
     */
    public interface UiCallBack {
        UiCallBack DEFAULT =
                new UiCallBack() {
                    @Override
                    public void onAddOrQuery(List<Favourites> favPodcastList) {
                    }

                    @Override
                    public void onSubscribe(List<Favourites> favPodcastList) {
                    }

                    @Override
                    public void onDelete(Boolean isDelete) {

                    }

                    @Override
                    public void updateUiOnError(String errorMessage) {
                    }

                    @Override
                    public void onGetPodcasts(ArrayList<PodCasts> podCastList) {
                    }

                    @Override
                    public void onDBReady(boolean value) {
                    }
                };

        void onAddOrQuery(List<Favourites> favPodcastList);

        void onSubscribe(List<Favourites> favPodcastList);

        void onDelete(Boolean isDelete);

        void onGetPodcasts(ArrayList<PodCasts> podCastList);

        void onDBReady(boolean value);

        void updateUiOnError(String errorMessage);
    }
}
