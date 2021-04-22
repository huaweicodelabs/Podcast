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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.data.model.HomePageModel
import com.huawei.podcast.kotlin.interfaces.CategoryClickListener
import com.huawei.podcast.kotlin.main.adapter.HomeAdapter
import com.huawei.podcast.kotlin.main.adapter.InterestAdapter
import com.huawei.podcast.kotlin.main.adapter.TrendingAdapter
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment(), CategoryClickListener {

    private lateinit var adapter: HomeAdapter
    private lateinit var interestAdapter: InterestAdapter
    private lateinit var trendingAdapter: TrendingAdapter
    private lateinit var mView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_home, container, false)
        setupUI(mView)
        return mView
    }

    private fun setupUI(view: View) {
        /*trending*/
        view.rv_trending.layoutManager =
            LinearLayoutManager(this.requireActivity()).also {
                view.rv_trending.layoutManager = it
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
        trendingAdapter = TrendingAdapter(this)
        view.rv_trending.adapter = trendingAdapter
        trendingList()

        /*category*/
        view.rv_category.layoutManager =
            LinearLayoutManager(this.requireActivity()).also {
                view.rv_category.layoutManager = it
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
        adapter = HomeAdapter(this)
        view.rv_category.adapter = adapter
        categoryList()

        /*interest*/
        view.rv_interest.layoutManager =
            LinearLayoutManager(this.requireActivity()).also {
                view.rv_interest.layoutManager = it
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
        interestAdapter = InterestAdapter(this)
        view.rv_interest.adapter = interestAdapter
        interestList()
    }

    private fun trendingList() {
        val trendList = resources.getStringArray(R.array.trending).toList()
        val trendingList = ArrayList<HomePageModel>()
        for (items in trendList) {
            trendingList.add(HomePageModel(items))
        }
        trendingAdapter.setList(trendingList)
        trendingAdapter.notifyDataSetChanged()
    }

    private fun categoryList() {
        val catList = resources.getStringArray(R.array.category).toList()
        val categoryList = ArrayList<HomePageModel>()
        for (items in catList) {
            categoryList.add(HomePageModel(items))
        }
        adapter.setList(categoryList)
        adapter.notifyDataSetChanged()
    }

    private fun interestList() {
        val intList = resources.getStringArray(R.array.interest).toList()
        val interestList = ArrayList<HomePageModel>()
        for (items in intList) {
            interestList.add(HomePageModel(items))
        }
        interestAdapter.setList(interestList)
        interestAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(category: HomePageModel) {
        val i = Intent(this.requireActivity(), DetailsActivity::class.java)
        i.putExtra(getString(R.string.topic), category.label)
        startActivity(i)
    }
}
