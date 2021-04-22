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
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.RecordingList;
import com.huawei.podcast.java.main.view.PlayAudioActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.ViewHolder> {
    ArrayList<RecordingList> audioArrayList;
    Context context;

    public RecordingsAdapter(ArrayList<RecordingList> audioArrayList, Context context) {
        this.audioArrayList = audioArrayList;
        this.context = context;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_recordings_list, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RecordingList recordingList = audioArrayList.get(position);
        holder.title.setText(recordingList.getTitle());
        holder.itemView.setOnClickListener(
                view -> {
                    Intent intent = new Intent(context, PlayAudioActivity.class);
                    intent.putExtra(context.getString(R.string.recording_list), audioArrayList);
                    intent.putExtra(context.getString(R.string.position), position);
                    context.startActivity(intent);
                });
    }

    @Override
    public int getItemCount() {
        return audioArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
        }
    }
}
