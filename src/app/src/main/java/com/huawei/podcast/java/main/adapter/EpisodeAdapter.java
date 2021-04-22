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

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.database.model.PodCasts;
import com.huawei.podcast.databinding.ItemEpisodeBinding;
import com.huawei.podcast.java.interfaces.OnClickPodCast;
import com.huawei.podcast.java.utils.Constants;

import java.io.File;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.ViewHolder> {
    private List<PodCasts> episodeList;
    Context context;

    public EpisodeAdapter(Context context, List<PodCasts> episodeList) {
        this.context = context;
        this.episodeList = episodeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemEpisodeBinding itemEpisodeBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_episode, parent, false);
        return new ViewHolder(itemEpisodeBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PodCasts podCasts = episodeList.get(position);
        holder.bind(context, podCasts, position);
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemEpisodeBinding itemEpisodeBinding;

        public ViewHolder(@NonNull ItemEpisodeBinding itemEpisodeBinding) {
            super(itemEpisodeBinding.getRoot());
            this.itemEpisodeBinding = itemEpisodeBinding;
        }

        public void bind(Context context, PodCasts podCasts, int position) {
            this.itemEpisodeBinding.setType(false);
            this.itemEpisodeBinding.setEpisodeListJava(podCasts);
            this.itemEpisodeBinding.setClickInterfaceJava((OnClickPodCast) context);
            this.itemEpisodeBinding.setPosition(position);
            itemEpisodeBinding.imgDownload.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
                            ;
                            File[] files = folder.listFiles();
                            boolean isExist = false;
                            if (files != null && files.length > Constants.ZERO)
                                for (File file : files) {
                                    if (file.getName().equals(podCasts.getTitle())) {
                                        isExist = true;
                                        continue;
                                    }
                                }
                            if (!isExist) {
                                DownloadManager downloadManager =
                                        (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                                DownloadManager.Request request =
                                        new DownloadManager.Request(Uri.parse(podCasts.getUrl()));
                                request.setTitle(podCasts.getTitle());
                                request.setDescription(context.getString(R.string.download_using_download_manager));
                                request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_PODCASTS, podCasts.getTitle());
                                downloadManager.enqueue(request);
                                Toast.makeText(
                                                context,
                                                context.getString(R.string.file_download_success),
                                                Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(context, context.getString(R.string.file_downloaded), Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    });
        }
    }

    public void setList(List<PodCasts> episodeList) {
        this.episodeList = episodeList;
        notifyDataSetChanged();
    }
}
