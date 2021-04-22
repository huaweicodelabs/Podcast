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
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.main.view.DownloadActivity;
import com.huawei.podcast.java.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class DownLoadAdapter extends RecyclerView.Adapter<DownLoadAdapter.ViewHolder> {
    ArrayList<String> downloadList;
    Context context;

    public DownLoadAdapter(Context context, ArrayList<String> downloadList) {
        this.downloadList = downloadList;
        this.context = context;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_downloads, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String category = downloadList.get(position);
        holder.txtTitle.setText(category);
        holder.imageDownload.setOnClickListener(
                view -> {
                    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
                    if (directory.isDirectory() && directory.exists()) {
                        File file = new File(directory, downloadList.get(position));
                        boolean deleted = file.delete();
                        if (deleted) {
                            Toast.makeText(context, context.getString(R.string.txt_file_delete), Toast.LENGTH_SHORT).show();
                            downloadList.remove(position);
                            if (downloadList.size() == Constants.ZERO) {
                                ((DownloadActivity) context).updateText();
                            }
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, context.getString(R.string.txt_delete_file), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return downloadList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageDownload;
        public TextView txtTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            this.txtTitle = itemView.findViewById(R.id.txt_title);
            this.imageDownload = itemView.findViewById(R.id.img_download);
        }
    }
}
