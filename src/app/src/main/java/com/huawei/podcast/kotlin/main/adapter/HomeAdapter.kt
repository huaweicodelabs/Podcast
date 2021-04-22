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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.huawei.podcast.R
import com.huawei.podcast.databinding.ItemHomeFragmentBinding
import com.huawei.podcast.kotlin.data.model.HomePageModel
import com.huawei.podcast.kotlin.interfaces.CategoryClickListener

class HomeAdapter(val clickListener: CategoryClickListener) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    private var categoryList: List<HomePageModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val viewBinding: ItemHomeFragmentBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_home_fragment, parent, false
        )
        return HomeViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class HomeViewHolder(private val viewBinding: ItemHomeFragmentBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        fun onBind(position: Int) {
            val row = categoryList[position]
            viewBinding.type = true
            viewBinding.trendingList = row
            viewBinding.position = position
            viewBinding.clickInterface = clickListener
        }
    }

    fun setList(podCastList: List<HomePageModel>) {
        this.categoryList = podCastList
        notifyDataSetChanged()
    }
}
