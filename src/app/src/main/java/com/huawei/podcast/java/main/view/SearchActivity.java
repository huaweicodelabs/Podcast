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
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.main.adapter.SearchAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchActivity extends AppCompatActivity {
    SearchAdapter searchAdapter;
    ArrayList<String> categoryFilterList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupUI();
    }

    private void setupUI() {
        RecyclerView rv_search = findViewById(R.id.rv_search);
        searchAdapter = new SearchAdapter(categoryFilterList, this);
        rv_search.setAdapter(searchAdapter);
        renderList();
        ImageView img_back_arrow = findViewById(R.id.img_back_arrow);
        SearchView pod_cast_search = findViewById(R.id.pod_cast_search);
        img_back_arrow.setOnClickListener(v -> onBackPressed());
        pod_cast_search.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        searchAdapter.getFilter().filter(newText);
                        return false;
                    }
                });
    }

    private void renderList() {
        categoryFilterList.add(getString(R.string.books));
        categoryFilterList.add(getString(R.string.carrers));
        categoryFilterList.add(getString(R.string.standup));
        categoryFilterList.add(getString(R.string.language));
        categoryFilterList.add(getString(R.string.drama));
        categoryFilterList.add(getString(R.string.music));
        categoryFilterList.add(getString(R.string.daily_news));
        categoryFilterList.add(getString(R.string.comedy));
        categoryFilterList.add(getString(R.string.education));
        categoryFilterList.add(getString(R.string.arts));
        categoryFilterList.add(getString(R.string.drama));
        categoryFilterList.add(getString(R.string.history));
        searchAdapter.setList(categoryFilterList);
        searchAdapter.notifyDataSetChanged();
    }
}
