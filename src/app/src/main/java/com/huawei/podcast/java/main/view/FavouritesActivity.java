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

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.database.CloudDBZoneWrapper;
import com.huawei.podcast.java.database.model.Favourites;
import com.huawei.podcast.java.database.model.PodCasts;
import com.huawei.podcast.java.interfaces.OnDeletePodCast;
import com.huawei.podcast.java.main.adapter.FavouritesAdapter;
import com.huawei.podcast.java.utils.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity implements CloudDBZoneWrapper.UiCallBack, OnDeletePodCast {
    CloudDBZoneWrapper mCloudDBZoneWrapper = new CloudDBZoneWrapper();
    ArrayList<Favourites> favouritePodCastList;
    FavouritesAdapter favouritesAdapter;
    RecyclerView recyclerView;
    TextView txt_no_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        setUpUI();
    }

    private void setUpUI() {
        favouritePodCastList = new ArrayList<>();
        mCloudDBZoneWrapper.addCallBacks(this);
        mCloudDBZoneWrapper.createObjectType();
        mCloudDBZoneWrapper.openCloudDBZoneV2();
        TextView txt_title = (findViewById(R.id.txt_title));
        txt_no_data = (findViewById(R.id.txt_no_data));
        txt_title.setText(getString(R.string.str_fav));
        recyclerView = findViewById(R.id.rv_fav);
        recyclerView.setHasFixedSize(true);
        favouritesAdapter = new FavouritesAdapter(favouritePodCastList, FavouritesActivity.this);
        recyclerView.setAdapter(favouritesAdapter);
        ImageView img_back_arrow = findViewById(R.id.img_back_arrow);
        img_back_arrow.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onAddOrQuery(List<Favourites> favPodCastList) {
        if (favPodCastList.size() != Constants.ZERO) {
            favouritePodCastList.clear();
            txt_no_data.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            favouritePodCastList.addAll(favPodCastList);
            favouritesAdapter.notifyDataSetChanged();

        } else {
            txt_no_data.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSubscribe(List<Favourites> favPodCastList) {}

    @Override
    public void onDelete(@Nullable Boolean isDeleted) {
        Toast.makeText(this, getString(R.string.removed_from_favourites), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetPodcasts(ArrayList<PodCasts> podCastList) {}

    @Override
    public void updateUiOnError(@Nullable String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDBReady(boolean value) {
        mCloudDBZoneWrapper.getFavouritePodCasts();
    }

    @Override
    public void onDeletePodCast(@NotNull Favourites podCast, int position) {
        favouritePodCastList.remove(podCast);
        favouritesAdapter.notifyDataSetChanged();
        if (favouritesAdapter.getItemCount() == Constants.ZERO) {
            txt_no_data.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        mCloudDBZoneWrapper.deleteFavPodCast(podCast);
    }
}
