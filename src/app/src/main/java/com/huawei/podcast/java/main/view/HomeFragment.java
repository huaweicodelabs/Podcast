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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.HomePageModel;
import com.huawei.podcast.java.interfaces.CategoryClickListener;
import com.huawei.podcast.java.main.adapter.HomeAdapter;
import com.huawei.podcast.java.main.adapter.InterestAdapter;
import com.huawei.podcast.java.main.adapter.TrendingAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements CategoryClickListener {
    HomeAdapter homeAdapter;
    InterestAdapter interestAdapter;
    TrendingAdapter trendingAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_home, container, false);
        setupUI(mView);
        return mView;
    }

    private void setupUI(View mView) {
        RecyclerView rv_trending = mView.findViewById(R.id.rv_trending);
        rv_trending.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        trendingAdapter = new TrendingAdapter(getActivity());
        rv_trending.setAdapter(trendingAdapter);
        trendingList();
        RecyclerView rv_category = mView.findViewById(R.id.rv_category);
        rv_category.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        homeAdapter = new HomeAdapter(getActivity());
        rv_category.setAdapter(homeAdapter);
        categoryList();
        RecyclerView rv_interest = mView.findViewById(R.id.rv_interest);
        rv_interest.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        interestAdapter = new InterestAdapter(getActivity());
        rv_interest.setAdapter(interestAdapter);
        interestList();
    }

    private void categoryList() {
        ArrayList<HomePageModel> categoryList = new ArrayList<>();
        categoryList.add(new HomePageModel(getString(R.string.books)));
        categoryList.add(new HomePageModel(getString(R.string.carrers)));
        categoryList.add(new HomePageModel(getString(R.string.standup)));
        categoryList.add(new HomePageModel(getString(R.string.comedy)));
        categoryList.add(new HomePageModel(getString(R.string.education)));
        homeAdapter.setList(categoryList);
        homeAdapter.notifyDataSetChanged();
    }

    private void trendingList() {
        ArrayList<HomePageModel> trendingList = new ArrayList<>();
        trendingList.add(new HomePageModel(getString(R.string.language)));
        trendingList.add(new HomePageModel(getString(R.string.drama)));
        trendingList.add(new HomePageModel(getString(R.string.music)));
        trendingList.add(new HomePageModel(getString(R.string.daily_news)));
        trendingAdapter.setList(trendingList);
        trendingAdapter.notifyDataSetChanged();
    }

    private void interestList() {
        ArrayList<HomePageModel> interestList = new ArrayList<>();
        interestList.add(new HomePageModel(getString(R.string.music)));
        interestList.add(new HomePageModel(getString(R.string.arts)));
        interestList.add(new HomePageModel(getString(R.string.drama)));
        interestList.add(new HomePageModel(getString(R.string.history)));
        interestAdapter.setList(interestList);
        interestAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(@NotNull HomePageModel category) {
        Intent i = new Intent(getActivity(), DetailsActivity.class);
        i.putExtra(getString(R.string.topic), category.getLabel());
        startActivity(i);
    }
}
