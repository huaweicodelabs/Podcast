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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.interfaces.SubscribeClickListener
import com.huawei.podcast.kotlin.main.adapter.SubscribeAdapter
import com.huawei.podcast.kotlin.main.viewmodel.SubscribeViewModel
import kotlinx.android.synthetic.main.activity_favorites.*
import kotlinx.android.synthetic.main.include_header.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class SubscribeListActivity : AppCompatActivity(), SubscribeClickListener {

    private lateinit var adapter: SubscribeAdapter
    private val viewModel: SubscribeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        setUpUI()
    }

    private fun setUpUI() {
        txt_title.text = getString(R.string.str_subscription_list)
        txt_no_data.text = getString(R.string.no_subscription)
        img_back_arrow.setOnClickListener {
            onBackPressed()
        }
        /*get Subscribe list from data base*/
        GlobalScope.launch {
            runOnUiThread {
                rv_fav.layoutManager =
                    LinearLayoutManager(this@SubscribeListActivity).also {
                        rv_fav.layoutManager = it
                    }
                viewModel.getSubscriptionList().observe(
                    this@SubscribeListActivity,
                    Observer { data ->
                        if (data.isNotEmpty()) {
                            adapter = SubscribeAdapter(this@SubscribeListActivity, data)
                            rv_fav.adapter = adapter
                            txt_no_data.visibility = View.GONE
                            rv_fav.visibility = View.VISIBLE
                        } else {
                            txt_no_data.visibility = View.VISIBLE
                            rv_fav.visibility = View.GONE
                        }
                    }
                )
            }
        }
    }
    override fun onItemClick(topic: String) {
        val i = Intent(this, DetailsActivity::class.java).apply {
            putExtra(getString(R.string.topic), topic)
        }
        startActivity(i)
    }
}
