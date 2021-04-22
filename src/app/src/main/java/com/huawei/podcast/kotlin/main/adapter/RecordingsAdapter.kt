/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.kotlin.main.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.data.model.RecordingList
import com.huawei.podcast.kotlin.main.adapter.RecordingsAdapter.ViewHolder
import com.huawei.podcast.kotlin.main.view.PlayAudioActivity
import java.util.* // ktlint-disable no-wildcard-imports

class RecordingsAdapter(var context: Context, var audioArrayList: ArrayList<RecordingList>) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recordings_list, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        holder.title.text = audioArrayList[i].title
    }

    override fun getItemCount(): Int {
        return audioArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, PlayAudioActivity::class.java).apply {
                    putExtra(context.getString(R.string.recording_list), audioArrayList)
                    putExtra(context.getString(R.string.position), layoutPosition)
                }

                context.startActivity(intent)
            }
        }
    }
}
