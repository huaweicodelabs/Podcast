/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.java.main.view;

import static com.huawei.podcast.java.utils.Utils.isNetworkConnected;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import com.huawei.hms.push.HmsMessaging;
import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.SubscribeModel;
import com.huawei.podcast.java.database.AppDatabase;
import com.huawei.podcast.java.database.CloudDBZoneWrapper;
import com.huawei.podcast.java.database.model.Favourites;
import com.huawei.podcast.java.database.model.PodCasts;
import com.huawei.podcast.databinding.ActivityDetailsBinding;
import com.huawei.podcast.java.interfaces.OnClickPodCast;
import com.huawei.podcast.java.main.adapter.EpisodeAdapter;
import com.huawei.podcast.java.utils.Constants;
import com.huawei.podcast.java.utils.ProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DetailsActivity extends AppCompatActivity implements OnClickPodCast, CloudDBZoneWrapper.UiCallBack {
    CloudDBZoneWrapper mCloudDBZoneWrapper = new CloudDBZoneWrapper();

    EpisodeAdapter episodeAdapter;
    Dialog dialog;
    ActivityDetailsBinding activityDetailsBinding;
    ArrayList<PodCasts> episodeList = new ArrayList<>();
    int position = Constants.ZERO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        activityDetailsBinding.setLifecycleOwner(this);
        setupUI();
    }

    private void setupUI() {
        if (isNetworkConnected(this)) {
            dialog = ProgressDialog.showProgress(this);
            mCloudDBZoneWrapper.addCallBacks(this);
            mCloudDBZoneWrapper.createObjectType();
            mCloudDBZoneWrapper.openCloudDBZoneV2();
        } else {
            showToast(getString(R.string.no_internet));
        }
        Bundle bundle = getIntent().getExtras();
        String topic = bundle.getString(getString(R.string.topic));
        activityDetailsBinding.txtTitle.setText(topic);
        activityDetailsBinding.imgBackArrow.setOnClickListener(v -> onBackPressed());
        Executors.newSingleThreadExecutor()
                .execute(
                        () -> {
                            String validTopic = topic.replaceAll(Constants.REGEX_STRING, Constants.EMPTY_STRING);
                            SubscribeModel data =
                                    AppDatabase.getDatabase(DetailsActivity.this)
                                            .todoDao()
                                            .findBySubscribeTitle(validTopic);
                            if (data != null && data.getTopic().equals(validTopic)) {
                                activityDetailsBinding.txtSubscribe.setText(getString(R.string.str_unsubscribe));
                            } else {
                                activityDetailsBinding.txtSubscribe.setText(getString(R.string.subscribe));
                            }
                        });

        activityDetailsBinding.txtSubscribe.setOnClickListener(
                v -> {
                    if (topic != null) {
                        String validTopic = topic.replaceAll(Constants.REGEX_STRING, Constants.EMPTY_STRING);
                        if (activityDetailsBinding
                                .txtSubscribe
                                .getText()
                                .toString()
                                .equals(getString(R.string.str_unsubscribe))) {
                            unSubscribeTopic(validTopic);
                        } else {
                            subscribeTopic(validTopic);
                        }
                    }
                });
    }

    private void subscribeTopic(String validTopic) {
        try {
            HmsMessaging.getInstance(DetailsActivity.this)
                    .subscribe(validTopic)
                    .addOnCompleteListener(
                            task -> {
                                if (task.isSuccessful()) {
                                    SubscribeModel subscribeModel = new SubscribeModel();
                                    subscribeModel.setPrim_id(position);
                                    subscribeModel.setTopic(validTopic);
                                    Executors.newSingleThreadExecutor()
                                            .execute(
                                                    () ->
                                                            AppDatabase.getDatabase(DetailsActivity.this)
                                                                    .todoDao()
                                                                    .insertSubscribeList(subscribeModel));
                                    showToast(getString(R.string.subscribe_complete));
                                    activityDetailsBinding.txtSubscribe.setText(getString(R.string.str_unsubscribe));
                                } else {
                                    showToast(getString(R.string.subscribe_failed));
                                }
                            });
        } catch (Exception e) {
            showToast(getString(R.string.subscribe_exception));
        }
    }

    private void unSubscribeTopic(String validTopic) {
        try {
            HmsMessaging.getInstance(DetailsActivity.this)
                    .unsubscribe(validTopic)
                    .addOnCompleteListener(
                            task -> {
                                if (task.isSuccessful()) {
                                    Executors.newSingleThreadExecutor()
                                            .execute(
                                                    () -> {
                                                        SubscribeModel data =
                                                                AppDatabase.getDatabase(DetailsActivity.this)
                                                                        .todoDao()
                                                                        .findBySubscribeTitle(validTopic);
                                                        AppDatabase.getDatabase(DetailsActivity.this)
                                                                .todoDao()
                                                                .deleteSubscribe(data);
                                                    });
                                    showToast(getString(R.string.unsubscribe_complete));
                                    activityDetailsBinding.txtSubscribe.setText(getString(R.string.subscribe));
                                } else {
                                    showToast(getString(R.string.unsubscribe_failed));
                                }
                            });
        } catch (Exception e) {
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddOrQuery(List<Favourites> favPodCastList) {}

    @Override
    public void onSubscribe(List<Favourites> favPodCastList) {}

    @Override
    public void onDelete(Boolean isDeleted) {}

    @Override
    public void onGetPodcasts(ArrayList<PodCasts> podCastList) {
        dialog.dismiss();
        episodeList = podCastList;
        episodeAdapter = new EpisodeAdapter(this, podCastList);
        activityDetailsBinding.rvEpisodes.setAdapter(episodeAdapter);
        episodeAdapter.notifyDataSetChanged();
        activityDetailsBinding.rvEpisodes.setHasFixedSize(true);
    }

    @Override
    public void updateUiOnError(String errorMessage) {}

    @Override
    public void onDBReady(boolean value) {
        mCloudDBZoneWrapper.getAllPodCasts();
    }

    @Override
    public void onClickPodCast(PodCasts episode, int position) {
        Intent i = new Intent(this, EpisodeDetailsActivity.class);
        i.putExtra(getString(R.string.episode_list), episodeList);
        i.putExtra(getString(R.string.position), position);
        startActivity(i);
    }
}
