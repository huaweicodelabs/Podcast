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
import com.huawei.podcast.databinding.ItemFavoritesBinding;
import com.huawei.podcast.java.database.model.Favourites;
import com.huawei.podcast.java.interfaces.OnDeletePodCast;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {
    private final List<Favourites> favouritePodcasts;
    Context context;

    public FavouritesAdapter(List<Favourites> favouritePodcasts, Context context) {
        this.favouritePodcasts = favouritePodcasts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemFavoritesBinding itemFavoritesBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_favorites, parent, false);
        return new ViewHolder(itemFavoritesBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Favourites favouritePodcast = favouritePodcasts.get(position);
        holder.bind(context, favouritePodcast, position);
    }

    @Override
    public int getItemCount() {
        return favouritePodcasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavoritesBinding itemFavoritesBinding;

        public ViewHolder(@NonNull ItemFavoritesBinding itemFavoritesBinding) {
            super(itemFavoritesBinding.getRoot());
            this.itemFavoritesBinding = itemFavoritesBinding;
        }

        public void bind(Context context, Favourites favouritePodcast, int position) {
            this.itemFavoritesBinding.setEListJava(favouritePodcast);
            itemFavoritesBinding.imgDownload.setOnClickListener(
                    v -> ((OnDeletePodCast) context).onDeletePodCast(favouritePodcast, position));
        }
    }
}
