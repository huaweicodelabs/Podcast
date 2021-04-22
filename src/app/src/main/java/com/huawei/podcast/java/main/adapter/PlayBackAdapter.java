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
import com.huawei.podcast.java.data.model.PodCastList;
import com.huawei.podcast.databinding.ItemPlayBackHistoryBinding;
import com.huawei.podcast.java.interfaces.EpisodeClickListener;

import java.util.List;

public class PlayBackAdapter extends RecyclerView.Adapter<PlayBackAdapter.ViewHolder> {
    private final List<PodCastList> podCastLists;
    Context context;

    public PlayBackAdapter(List<PodCastList> podCastLists, Context context) {
        this.podCastLists = podCastLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemPlayBackHistoryBinding itemPlayBackHistoryBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_play_back_history, parent, false);
        return new ViewHolder(itemPlayBackHistoryBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PodCastList podCastList = podCastLists.get(position);
        holder.bind(context, podCastList);
    }

    @Override
    public int getItemCount() {
        return podCastLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlayBackHistoryBinding itemPlayBackHistoryBinding;

        public ViewHolder(@NonNull ItemPlayBackHistoryBinding itemPlayBackHistoryBinding) {
            super(itemPlayBackHistoryBinding.getRoot());
            this.itemPlayBackHistoryBinding = itemPlayBackHistoryBinding;
        }

        public void bind(Context context, PodCastList podCastList) {
            this.itemPlayBackHistoryBinding.setType(false);
            this.itemPlayBackHistoryBinding.setEpisodeListJava(podCastList);
            this.itemPlayBackHistoryBinding.setClickInterfaceJava((EpisodeClickListener) context);
            this.itemPlayBackHistoryBinding.setPosition(podCastList.getPosition());
            itemPlayBackHistoryBinding.executePendingBindings();
        }
    }
}
