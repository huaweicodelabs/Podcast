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
package com.huawei.podcast.java.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.HomePageModel;
import com.huawei.podcast.databinding.ItemHomeFragmentBinding;
import com.huawei.podcast.java.interfaces.CategoryClickListener;

import java.util.List;

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.ViewHolder> {
    private List<HomePageModel> trendingList;
    Context context;

    public TrendingAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemHomeFragmentBinding itemHomeBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_home_fragment, parent, false);
        return new ViewHolder(itemHomeBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomePageModel homePageModel = trendingList.get(position);
        holder.bind(context, homePageModel, position);
    }

    @Override
    public int getItemCount() {
        return trendingList.size();
    }

    public void setList(List<HomePageModel> trendingList) {
        this.trendingList = trendingList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeFragmentBinding itemHomeBinding;

        public ViewHolder(@NonNull ItemHomeFragmentBinding itemHomeFragmentBinding) {
            super(itemHomeFragmentBinding.getRoot());
            this.itemHomeBinding = itemHomeFragmentBinding;
        }

        public void bind(Context context, HomePageModel homePageModel, int position) {
            this.itemHomeBinding.setType(false);
            this.itemHomeBinding.setTrendingListJava(homePageModel);
            this.itemHomeBinding.setClickInterfaceJava((CategoryClickListener) context);
            this.itemHomeBinding.setPosition(position);
            itemHomeBinding.executePendingBindings();
        }
    }
}
