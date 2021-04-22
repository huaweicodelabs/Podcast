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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.main.adapter.SearchAdapter
import com.huawei.podcast.kotlin.utils.Constants
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.include_search.*

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setupUI()
    }

    private fun setupUI() {
        val categoryFilterList = resources.getStringArray(R.array.choose_your_interest).toList()
        rv_search.layoutManager = GridLayoutManager(this, Constants.TWO).also { rv_search.layoutManager = it }
        adapter = SearchAdapter(this, categoryFilterList as ArrayList<String>)
        rv_search.adapter = adapter
        renderList(categoryFilterList)
        img_back_arrow.setOnClickListener {
            onBackPressed()
        }
        pod_cast_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun renderList(categoryFilterList: ArrayList<String>) {
        adapter.setList(categoryFilterList)
        adapter.notifyDataSetChanged()
    }
}
