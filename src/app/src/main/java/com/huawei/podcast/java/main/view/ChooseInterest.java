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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.HomePageModel;
import com.huawei.podcast.java.interfaces.CategoryClickListener;
import com.huawei.podcast.java.main.adapter.TrendingAdapter;
import com.huawei.podcast.java.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseInterest extends AppCompatActivity implements CategoryClickListener {
    TrendingAdapter trendingAdapter;
    private boolean isAllowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_your_interest);
        setupUI();
    }

    private void setupUI() {
        RecyclerView rv_interest = findViewById(R.id.rv_interest);
        ImageView img_back_arrow = findViewById(R.id.img_back_arrow);
        TextView txt_title = findViewById(R.id.txt_title);
        trendingAdapter = new TrendingAdapter(this);
        rv_interest.setAdapter(trendingAdapter);
        img_back_arrow.setVisibility(View.GONE);
        txt_title.setText(getString(R.string.interest));
        renderList();
        isAllowed = checkReadPermissionBoolean();
    }

    private boolean checkReadPermissionBoolean() {
        int result =
                ContextCompat.checkSelfPermission(
                        ChooseInterest.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(ChooseInterest.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onItemClick(@NotNull HomePageModel category) {
        if (isAllowed) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                ChooseInterest.this,
                new String[] {
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                },
                Constants.REQUEST_CODE_ONE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_CODE_ONE) { // If request is cancelled, the result arrays are empty.
            if (grantResults.length > Constants.ZERO && grantResults[Constants.ZERO] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(
                                ChooseInterest.this,
                                getString(R.string.permission_denied),
                                Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void renderList() {
        List<HomePageModel> chooseInterestList = new ArrayList<HomePageModel>();
        chooseInterestList.add(new HomePageModel(getString(R.string.books)));
        chooseInterestList.add(new HomePageModel(getString(R.string.carrers)));
        chooseInterestList.add(new HomePageModel(getString(R.string.standup)));
        chooseInterestList.add(new HomePageModel(getString(R.string.language)));
        chooseInterestList.add(new HomePageModel(getString(R.string.drama)));
        chooseInterestList.add(new HomePageModel(getString(R.string.music)));
        chooseInterestList.add(new HomePageModel(getString(R.string.daily_news)));
        chooseInterestList.add(new HomePageModel(getString(R.string.arts)));
        chooseInterestList.add(new HomePageModel(getString(R.string.comedy)));
        chooseInterestList.add(new HomePageModel(getString(R.string.education)));
        trendingAdapter.setList(chooseInterestList);
        trendingAdapter.notifyDataSetChanged();
    }
}
