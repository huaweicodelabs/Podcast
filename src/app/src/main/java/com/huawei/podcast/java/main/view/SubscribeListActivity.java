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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.interfaces.SubscribeClickListener;
import com.huawei.podcast.java.main.adapter.SubscribeAdapter;
import com.huawei.podcast.java.main.viewmodel.SubscribeViewModel;
import com.huawei.podcast.java.utils.Constants;

import org.jetbrains.annotations.NotNull;

public class SubscribeListActivity extends AppCompatActivity implements SubscribeClickListener {
    SubscribeAdapter subscribeAdapter;
    SubscribeViewModel subscribeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        subscribeViewModel = ViewModelProviders.of(this).get(SubscribeViewModel.class);
        setUpUI();
    }

    private void setUpUI() {
        TextView txt_title = (findViewById(R.id.txt_title));
        TextView txt_no_data = (findViewById(R.id.txt_no_data));
        ImageView img_back_arrow = findViewById(R.id.img_back_arrow);
        RecyclerView recyclerView = findViewById(R.id.rv_fav);
        txt_title.setText(getString(R.string.str_subscription_list));
        txt_no_data.setText(getString(R.string.no_subscription));
        img_back_arrow.setOnClickListener(v -> onBackPressed());
        subscribeViewModel
                .getSubscribeListLiveData()
                .observe(
                        this,
                        data -> {
                            if (data != null && data.size() != Constants.ZERO) {
                                subscribeAdapter = new SubscribeAdapter(data, SubscribeListActivity.this);
                                recyclerView.setAdapter(subscribeAdapter);
                                subscribeAdapter.notifyDataSetChanged();
                                recyclerView.setHasFixedSize(true);
                                txt_no_data.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                txt_no_data.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        });
    }

    @Override
    public void onItemClick(@NotNull String topic) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(getString(R.string.topic), topic);
        startActivity(intent);
    }
}
