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

package com.huawei.podcast.kotlin.main.view

import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.main.adapter.DownLoadAdapter
import kotlinx.android.synthetic.main.activity_favorites.*
import kotlinx.android.synthetic.main.include_header.*

class DownloadActivity : AppCompatActivity() {

    private lateinit var adapter: DownLoadAdapter
    private val fileList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        setUpUI()
    }

    private fun setUpUI() {
        txt_title.text = getString(R.string.str_downloads)
        txt_no_data.text = getString(R.string.no_downloads)
        img_back_arrow.setOnClickListener {
            onBackPressed()
        }
        // gets the files in the directory
        val folder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)

        if (folder?.listFiles() != null) {
            if (folder.listFiles().isNotEmpty()) {
                for (i in folder.listFiles().indices) {
                    fileList.add(folder.listFiles()[i].name)
                }
                rv_fav.layoutManager =
                    LinearLayoutManager(this).also {
                        rv_fav.layoutManager = it
                    }
                adapter = DownLoadAdapter(this, fileList, txt_no_data)
                rv_fav.adapter = adapter
                txt_no_data.visibility = View.GONE
                rv_fav.visibility = View.VISIBLE
            } else {
                txt_no_data.visibility = View.VISIBLE
                rv_fav.visibility = View.GONE
            }
        }
    }
}
