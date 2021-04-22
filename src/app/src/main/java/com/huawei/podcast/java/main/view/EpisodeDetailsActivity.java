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
package com.huawei.podcast.java.main.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.applinking.AppLinking;
import com.huawei.agconnect.applinking.ShortAppLinking;
import com.huawei.agconnect.auth.AGConnectAuth;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.PodCastList;
import com.huawei.podcast.java.database.AppDatabase;
import com.huawei.podcast.java.database.CloudDBZoneWrapper;
import com.huawei.podcast.java.database.model.Favourites;
import com.huawei.podcast.java.database.model.PodCasts;
import com.huawei.podcast.java.preference.SharedPreference;
import com.huawei.podcast.java.utils.Constants;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.xml.transform.Result;

public class EpisodeDetailsActivity extends AppCompatActivity
        implements View.OnClickListener, CloudDBZoneWrapper.UiCallBack {
    private int position = Constants.ZERO;
    CloudDBZoneWrapper mCloudDBZoneWrapper = new CloudDBZoneWrapper();
    /* ==========AppLinking================ */
    private final String domainUriPrefix = Constants.domainUriPrefix;
    private final String deepLink = Constants.deepLink;
    HiAnalyticsInstance instance = null;
    private TextView txtFav;
    private List<Favourites> favPodCastList = new ArrayList<>();
    private ArrayList<PodCasts> episodeList = new ArrayList<>();
    private BannerView defaultBannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_details);
        setUpUI();
    }

    private void setUpUI() {
        if (getIntent().hasExtra(getString(R.string.episode_list))) {
            episodeList = (ArrayList<PodCasts>) getIntent().getSerializableExtra(getString(R.string.episode_list));
            position = getIntent().getIntExtra(getString(R.string.position), position);
        }
        /* Analytics */
        instance = HiAnalytics.getInstance(this);
        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog();
        /* details */
        setDetails(position);
        TextView txt_download = (findViewById(R.id.txt_download));
        TextView txt_stream = (findViewById(R.id.txt_stream));
        ImageView img_back_arrow = findViewById(R.id.img_back_arrow);
        ImageView img_sub_menu = findViewById(R.id.img_sub_menu);

        /* onclick listener */
        img_back_arrow.setOnClickListener(this);
        txt_stream.setOnClickListener(this);
        img_sub_menu.setOnClickListener(this);
        txt_download.setOnClickListener(this);
        // Load the default banner ad.
        loadDefaultBannerAd();
        mCloudDBZoneWrapper.addCallBacks(this);
        mCloudDBZoneWrapper.createObjectType();
        mCloudDBZoneWrapper.openCloudDBZoneV2();

    }
    /**
     * Load the default banner ad.
     */
    private void loadDefaultBannerAd() {
        defaultBannerView = findViewById(R.id.hw_banner_view);
        defaultBannerView.setAdListener(adListener);
        defaultBannerView.setBannerRefresh(Constants.REFRESH_TIME);
        AdParam adParam = new AdParam.Builder().build();
        defaultBannerView.loadAd(adParam);
    }

    /**
     * Ad listener.
     */
    private final AdListener adListener =
            new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Called when an ad is loaded successfully.
                }

                @Override
                public void onAdFailed(int errorCode) {
                    // Called when an ad fails to be loaded.
                }

                @Override
                public void onAdOpened() {
                    // Called when an ad is opened.
                }

                @Override
                public void onAdClicked() {
                    // Called when a user taps an ad.
                }

                @Override
                public void onAdLeave() {
                    // Called when a user has left the app.
                }

                @Override
                public void onAdClosed() {
                    // Called when an ad is closed.
                }
            };

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void setDetails(int position) {
        TextView txt_title = (findViewById(R.id.txt_title));
        TextView txt_details = (findViewById(R.id.txt_details));
        TextView txt_episode = (findViewById(R.id.txt_episode));
        TextView txt_name = (findViewById(R.id.txt_name));
        txt_title.setText(episodeList.get(position).getTitle());
        if (episodeList.get(position).getDescription().isEmpty()) {
            txt_details.setText(getString(R.string.details));
        } else {
            txt_details.setText(episodeList.get(position).getDescription());
        }
        txt_episode.setText(episodeList.get(position).getDate());
        txt_name.setText(episodeList.get(position).getTitle());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back_arrow:
                onBackPressed();
                break;
            case R.id.txt_stream:
                Executors.newSingleThreadExecutor()
                        .execute(
                                () -> {
                                    PodCastList podCastList =
                                            AppDatabase.getDatabase(EpisodeDetailsActivity.this)
                                                    .todoDao()
                                                    .findByTitle(episodeList.get(position).getTitle());
                                    if (podCastList == null) {
                                        podCastList = new PodCastList();
                                        podCastList.setId(episodeList.get(position).getId());
                                        podCastList.setDescription(episodeList.get(position).getDescription());
                                        podCastList.setUrl(episodeList.get(position).getUrl());
                                        podCastList.setAuthor(episodeList.get(position).getAuthor());
                                        podCastList.setCategory(episodeList.get(position).getCategory());
                                        podCastList.setTitle(episodeList.get(position).getTitle());
                                        podCastList.setDate(episodeList.get(position).getDate());
                                        podCastList.setPosition(position);
                                        insertData(podCastList);
                                    }
                                });
                Intent i = new Intent(this, PlayAudioActivity.class);
                i.putExtra(getString(R.string.episode_list), episodeList);
                i.putExtra(getString(R.string.position), position);
                startActivity(i);
                break;
            case R.id.img_sub_menu:
                if (SharedPreference.contains(getString(R.string.username))) {
                    showSubmenus();
                } else {
                    startActivityForResult(new Intent(this,LoginActivity.class),Constants.SIGN_CODE);
                }
                break;
            case R.id.txt_download:
                File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
                File[] files = folder.listFiles();
                boolean isExist = false;
                if (files != null && files.length > Constants.ZERO)
                    for (File file : files) {
                        if (file.getName().equals(episodeList.get(position).getTitle())) {
                            isExist = true;
                            continue;
                        }
                    }
                if (isExist) {
                    showToast(getString(R.string.file_downloaded));
                    break;
                } else {
                    DownloadManager downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request =
                            new DownloadManager.Request(Uri.parse(episodeList.get(position).getUrl()));
                    request.setTitle(getTitle());
                    request.setDescription(getString(R.string.audio_downloadmanager));
                    request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_PODCASTS, episodeList.get(position).getTitle());
                    downloadManager.enqueue(request);
                    showToast(getString(R.string.file_download_success));
                    break;
                }
        }
    }

    @Override
    public void onAddOrQuery(List<Favourites> favPodCastList) {
        if (!favPodCastList.isEmpty()) {
            this.favPodCastList = favPodCastList;
            for (int i = Constants.ZERO; i < episodeList.size(); i++) {
                for (int j = Constants.ZERO; j <favPodCastList.size(); j++) {
                    if (episodeList.get(i).getId().equals(favPodCastList.get(j).getActual_podcast_id())) {
                        episodeList.get(i).setIsInFavourites(true);
                        break;
                    }
                }
            }
        }
    }

    private void insertData(PodCastList podCastList) {
        AppDatabase.getDatabase(EpisodeDetailsActivity.this).todoDao().insertAll(podCastList);
    }

    @Override
    public void onSubscribe(List<Favourites> favPodCastList) {
    }

    @Override
    public void onDelete(@Nullable Boolean isDeleted) {
    }

    @Override
    public void onGetPodcasts(ArrayList<PodCasts> podCastList) {
    }

    @Override
    public void updateUiOnError(String errorMessage) {
        if (errorMessage.equals(Constants.ADDED)) {
            showToast(getString(R.string.added_to_fav));
            txtFav.setText(R.string.added_to_fav);
            getFavouritePodcasts();
        } else {
            showToast(errorMessage);
        }
    }

    @Override
    public void onDBReady(boolean value) {
        if (AGConnectAuth.getInstance().getCurrentUser() != null) {
            getFavouritePodcasts();
        }
    }

    private void showSubmenus() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_episode_details);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.gravity = Gravity.END | Gravity.TOP;
        wlp.flags = wlp.flags | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        TextView txtNext = dialog.findViewById(R.id.txt_next);
        TextView txtShare = dialog.findViewById(R.id.txt_share);
        txtFav = dialog.findViewById(R.id.txt_fav);
        if (episodeList.get(position).getIsInFavourites()) {
            txtFav.setText(R.string.added_to_fav);
        } else {
            txtFav.setText(R.string.add_fav);
        }

        /* next episode */
        txtNext.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        position += Constants.REQUEST_CODE_ONE;
                        if (position < episodeList.size()) {
                            setDetails(position);
                        } else {
                            position = Constants.ZERO;
                            setDetails(position);
                        }
                        if (episodeList.get(position).getIsInFavourites())
                            txtFav.setText(R.string.added_to_fav);
                        else txtFav.setText(R.string.add_fav);
                    }
                });

        /* share */
        txtShare.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppLinking.Builder builder =
                                new AppLinking.Builder()
                                        .setUriPrefix(domainUriPrefix)
                                        .setDeepLink(Uri.parse(deepLink))
                                        .setAndroidLinkInfo(new AppLinking.AndroidLinkInfo.Builder().build());

                        builder.buildShortAppLinking()
                                .addOnSuccessListener(
                                        new OnSuccessListener<ShortAppLinking>() {
                                            @Override
                                            public void onSuccess(ShortAppLinking shortAppLinking) {
                                                com.huawei.podcast.java.utils.Utils.shareLink(shortAppLinking.getShortUrl().toString(), EpisodeDetailsActivity.this, instance);
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {
                                            }
                                        });
                    }
                });

        /* Add to favourites */
        txtFav.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Favourites favPodcast = new Favourites();
                        PodCasts podCasts = episodeList.get(position);
                        favPodcast.setPodcast_id(SharedPreference.getValueString(getString(R.string.userid))+podCasts.getId().toString());
                        favPodcast.setActual_podcast_id(podCasts.getId());
                        favPodcast.setUrl(podCasts.getUrl());
                        favPodcast.setUser_id(SharedPreference.getValueString(getString(R.string.userid)));
                        favPodcast.setUser_name(SharedPreference.getValueString(getString(R.string.username)));
                        favPodcast.setPodcast_title(podCasts.getTitle());
                        favPodcast.setIsFavourite(true);

                        boolean isExist =false;
                        if(favPodCastList!=null && favPodCastList.size()>Constants.ZERO) {
                            for (Favourites fav : favPodCastList) {
                                if (fav.getActual_podcast_id().equals(podCasts.getId()) ) {
                                    isExist = true;
                                    break;
                                }
                            }
                        }
                        if (isExist) {
                            podCasts.setIsInFavourites(false);
                            showToast(getString(R.string.already_added_to_fav));
                        } else {
                            podCasts.setIsInFavourites(true);
                            mCloudDBZoneWrapper.upsertFavPodCasts(favPodcast);
                        }

                    }
                });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // To dismiss the dialog
       if (resultCode == RESULT_OK)
        getFavouritePodcasts();
    }

    private void getFavouritePodcasts() {
        mCloudDBZoneWrapper.getFavouritePodCasts();
    }
}
