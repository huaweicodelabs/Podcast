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
package com.huawei.podcast.kotlin.database

import android.content.Context
import com.huawei.agconnect.cloud.database.* // ktlint-disable no-wildcard-imports
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.database.model.Favourites
import com.huawei.podcast.kotlin.database.model.PodCasts
import com.huawei.podcast.kotlin.preference.SharedPreference.getValueString
import com.huawei.podcast.kotlin.utils.Constants
import com.huawei.podcast.kotlin.utils.Strings

/**
 * Proxying implementation of CloudDBZone.
 */
class CloudDBZoneWrapper {
    private val mCloudDB: AGConnectCloudDB = AGConnectCloudDB.getInstance()
    private var mCloudDBZone: CloudDBZone? = null
    private var mRegister: ListenerHandler? = null
    private var mConfig: CloudDBZoneConfig? = null
    private var mUiCallBack = UiCallBack.DEFAULT

    /**
     * Monitor data change from database. Update book info list if data have changed
     */
    private val mSnapshotListener = OnSnapshotListener<Favourites> { cloudDBZoneSnapshot, e ->
        if (e != null) {
            return@OnSnapshotListener
        }
        val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
        val favPodCastList: MutableList<Favourites> = ArrayList()
        try {
            if (snapshotObjects != null) {
                while (snapshotObjects.hasNext()) {
                    val favPodCast = snapshotObjects.next()
                    favPodCastList.add(favPodCast)
                }
            }
            mUiCallBack.onSubscribe(favPodCastList)
        } catch (snapshotException: AGConnectCloudDBException) {
        } finally {
            cloudDBZoneSnapshot.release()
        }
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    fun createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
        } catch (e: AGConnectCloudDBException) {
        }
    }

    /**
     * Call AGConnectCloudDB.openCloudDBZone2 to open a cloudDBZone.
     * We set it with cloud cache mode, and data can be store in local storage.
     * AGConnectCloudDB.openCloudDBZone2 is an asynchronous method, we can add
     * OnSuccessListener/OnFailureListener to receive the result for opening cloudDBZone
     */
    fun openCloudDBZoneV2() {
        mConfig = CloudDBZoneConfig(
            Strings.get(R.string.cloud_db),
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
        )
        mConfig!!.persistenceEnabled = true
        val task = mCloudDB.openCloudDBZone2(mConfig!!, true)
        task.addOnSuccessListener {
            mCloudDBZone = it
            mUiCallBack.onDBReady(true)
            addSubscription()
        }.addOnFailureListener {
        }
    }

    /**
     * Add a callback to update podcast  list
     *
     * @param uiCallBack callback to update podcast list
     */
    fun addCallBacks(uiCallBack: UiCallBack) {
        mUiCallBack = uiCallBack
    }

    /**
     * Add mSnapshotListener to monitor data changes from storage
     */
    private fun addSubscription() {
        if (mCloudDBZone == null) {
            return
        }
        try {
            val snapshotQuery = CloudDBZoneQuery.where(Favourites::class.java)
                .equalTo(Constants.FAV_STATUS, true)
            mRegister = mCloudDBZone!!.subscribeSnapshot(
                snapshotQuery,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                mSnapshotListener
            )
        } catch (e: AGConnectCloudDBException) {
        }
    }

    /**
     * Query all favourite podcast in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     */
    fun getFavouritePodCasts() {
        if (mCloudDBZone == null) {
            return
        }
        val queryTask =
            mCloudDBZone!!.executeQuery(
                CloudDBZoneQuery.where(Favourites::class.java).equalTo(
                    Constants.USER_ID,
                    getValueString(Constants.USER_ID)
                ),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )
        queryTask
            .addOnSuccessListener { snapshot -> processQueryResult(snapshot) }
            .addOnFailureListener { mUiCallBack.updateUiOnError(Strings.get(R.string.str_query)) }
    }

    /**
     * Query all podcasts in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     */
    fun getAllPodCasts() {
        if (mCloudDBZone == null) {
            return
        }
        val queryTask = mCloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(PodCasts::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        queryTask.addOnSuccessListener { snapshot -> processPodCastsResult(snapshot) }
            .addOnFailureListener {
                mUiCallBack.updateUiOnError(Strings.get(R.string.str_cloud_fail))
            }
    }

    private fun processQueryResult(snapshot: CloudDBZoneSnapshot<Favourites>) {
        val favPodCastCursor = snapshot.snapshotObjects
        val favPodCastList: MutableList<Favourites> = ArrayList()
        try {
            while (favPodCastCursor.hasNext()) {
                val favPodCast = favPodCastCursor.next()
                favPodCastList.add(favPodCast)
            }
        } catch (e: AGConnectCloudDBException) {
        } finally {
            snapshot.release()
        }
        mUiCallBack.onAddOrQuery(favPodCastList)
    }

    private fun processPodCastsResult(snapshot: CloudDBZoneSnapshot<PodCasts>) {
        val podCastCursor = snapshot.snapshotObjects
        val podCastList: MutableList<PodCasts> = ArrayList()
        try {
            while (podCastCursor.hasNext()) {
                val podCast = podCastCursor.next()
                podCastList.add(podCast)
            }
        } catch (e: AGConnectCloudDBException) {
        } finally {
            snapshot.release()
        }
        mUiCallBack.onGetPodCasts(podCastList as ArrayList<PodCasts>)
    }

    /**
     * Upsert favPodcast
     *
     * @param favPodcast favPodcast added or modified from local
     */
    fun upsertFavPodCasts(favPodCast: Favourites?) {
        if (mCloudDBZone == null) {
            return
        }
        val upsertTask = mCloudDBZone!!.executeUpsert(favPodCast!!)
        upsertTask.addOnSuccessListener { cloudDBZoneResult ->
            mUiCallBack.updateUiOnError(Constants.ADDED)
        }.addOnFailureListener {
            mUiCallBack.updateUiOnError(Constants.ADDED_FAILED)
        }
    }

    /**
     * Delete favPodcast
     *
     * @param favPodcastList books selected by user
     */
    fun deleteFavPodCast(favouritePodcast: Favourites?) {
        if (mCloudDBZone == null) {
            return
        }
        val deleteTask =
            mCloudDBZone!!.executeDelete(favouritePodcast!!)
        deleteTask.addOnSuccessListener { mUiCallBack.onDelete(true) }
        deleteTask.addOnFailureListener { mUiCallBack.updateUiOnError(com.huawei.podcast.java.utils.Constants.DELETE_QUERY_FAILED) }
    }

    /**
     * Call back to update ui in HomePageFragment
     */
    interface UiCallBack {
        fun onGetPodCasts(podCastList: ArrayList<PodCasts>)
        fun onAddOrQuery(favPodCastList: List<Favourites>)
        fun onSubscribe(favPodCastList: List<Favourites>?)
        fun onDelete(isDeleted: Boolean?)
        fun updateUiOnError(errorMessage: String?)
        fun onDBReady(value: Boolean)

        companion object {
            val DEFAULT: UiCallBack = object : UiCallBack {
                override fun onGetPodCasts(podCastList: ArrayList<PodCasts>) {
                }

                override fun onAddOrQuery(favPodCastList: List<Favourites>) {
                }

                override fun onSubscribe(favPodCastList: List<Favourites>?) {
                }

                override fun onDelete(isDeleted: Boolean?) {
                }

                override fun updateUiOnError(errorMessage: String?) {
                }

                override fun onDBReady(value: Boolean) {
                }
            }
        }
    }

    companion object {
        /**
         * Init AGConnectCloudDB in Application
         *
         * @param context application context
         */
        fun initAGConnectCloudDB(context: Context?) {
            AGConnectCloudDB.initialize(context!!)
        }
    }
}
