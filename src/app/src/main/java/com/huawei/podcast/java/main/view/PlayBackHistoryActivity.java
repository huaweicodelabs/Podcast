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

import static com.huawei.podcast.java.utils.Utils.isNetworkConnected;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.PodCastList;
import com.huawei.podcast.java.database.AppDatabase;
import com.huawei.podcast.java.database.CloudDBZoneWrapper;
import com.huawei.podcast.java.database.model.Favourites;
import com.huawei.podcast.java.database.model.PodCasts;
import com.huawei.podcast.java.interfaces.EpisodeClickListener;
import com.huawei.podcast.java.main.adapter.PlayBackAdapter;
import com.huawei.podcast.java.utils.Constants;
import com.huawei.podcast.java.utils.ProgressDialog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PlayBackHistoryActivity extends AppCompatActivity
        implements CloudDBZoneWrapper.UiCallBack, EpisodeClickListener {
    CloudDBZoneWrapper mCloudDBZoneWrapper = new CloudDBZoneWrapper();
    List<PodCastList> podCastLists;
    PlayBackAdapter playBackAdapter;
    TextView txt_no_data;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        setUpUI();
    }

    private void setUpUI() {
        TextView txt_title = (findViewById(R.id.txt_title));
        txt_no_data = (findViewById(R.id.txt_no_data));
        ImageView img_back_arrow = findViewById(R.id.img_back_arrow);
        txt_title.setText(getString(R.string.str_play_back));
        txt_no_data.setText(getString(R.string.no_play_back));
        img_back_arrow.setOnClickListener(v -> onBackPressed());
        if (isNetworkConnected(this)) {
            dialog = ProgressDialog.showProgress(this);
            mCloudDBZoneWrapper.addCallBacks(this);
            mCloudDBZoneWrapper.createObjectType();
            mCloudDBZoneWrapper.openCloudDBZoneV2();
            getPlayList();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_LONG).show();
        }
    }

    private void getPlayList() {
        RecyclerView recyclerView = findViewById(R.id.rv_fav);
        Executors.newSingleThreadExecutor()
                .execute(() -> podCastLists = AppDatabase.getDatabase(PlayBackHistoryActivity.this).todoDao().getAll());
        try {
            Thread.sleep(Constants.HUNDRED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (podCastLists != null && podCastLists.size() != Constants.ZERO) {
            playBackAdapter = new PlayBackAdapter(podCastLists, PlayBackHistoryActivity.this);
            recyclerView.setAdapter(playBackAdapter);
            playBackAdapter.notifyDataSetChanged();
            recyclerView.setHasFixedSize(true);
            txt_no_data.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            txt_no_data.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAddOrQuery(List<Favourites> favPodCastList) {}

    @Override
    public void onSubscribe(List<Favourites> favPodCastList) {}

    @Override
    public void onDelete(@Nullable Boolean isDeleted) {}

    @Override
    public void onGetPodcasts(ArrayList<PodCasts> podCastList) {
        // this.podCastLists = podCastList;

    }

    @Override
    public void updateUiOnError(@Nullable String errorMessage) {}

    @Override
    public void onDBReady(boolean value) {
        dialog.dismiss();
        mCloudDBZoneWrapper.getAllPodCasts();
    }

    @Override
    public void onItemClick(@NotNull PodCastList episode, int position) {
        Intent i = new Intent(this, PlayAudioActivity.class);
        i.putExtra(getString(R.string.episode_list), (Serializable) podCastLists);
        i.putExtra(getString(R.string.position), position);
        startActivity(i);
    }
}
