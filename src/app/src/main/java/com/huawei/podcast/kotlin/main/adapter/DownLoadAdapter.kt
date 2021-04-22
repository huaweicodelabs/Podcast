/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.podcast.kotlin.main.adapter

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.huawei.podcast.R
import kotlinx.android.synthetic.main.item_downloads.view.*
import java.io.File

class DownLoadAdapter(private val context: Context, private val downloadList: ArrayList<String>, private var txtNoData: TextView) :
    RecyclerView.Adapter<DownLoadAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_downloads, parent, false))
    }

    override fun getItemCount(): Int {
        return downloadList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.txtTitle.text = downloadList[position]
        holder.imageDownload.setOnClickListener {
            val directory: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
            if (directory.isDirectory && directory.exists()) {
                val file = File(directory, downloadList[position])
                val deleted: Boolean = file.delete()
                if (deleted) {
                    Toast.makeText(context, context.getString(R.string.txt_file_delete), Toast.LENGTH_LONG).show()
                    downloadList.removeAt(position)
                    if (downloadList.size == 0) {
                        txtNoData.visibility = View.VISIBLE
                    }
                    notifyDataSetChanged()
                } else {
                    Toast.makeText(context, context.getString(R.string.txt_delete_file), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitle: TextView = view.txt_title
        val imageDownload: ImageView = view.img_download
    }
}
