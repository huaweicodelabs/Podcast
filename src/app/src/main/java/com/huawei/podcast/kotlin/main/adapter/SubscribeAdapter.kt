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
import com.huawei.podcast.databinding.ItemSubscriptionListBinding
import com.huawei.podcast.kotlin.data.model.SubscribeModel
import com.huawei.podcast.kotlin.main.view.SubscribeListActivity

class SubscribeAdapter(val clickListener: SubscribeListActivity, private var subscrptionList: List<SubscribeModel>) : RecyclerView.Adapter<SubscribeAdapter.SubscribeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscribeViewHolder {
        val viewBinding: ItemSubscriptionListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_subscription_list, parent, false
        )
        return SubscribeViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return subscrptionList.size
    }

    override fun onBindViewHolder(holder: SubscribeViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class SubscribeViewHolder(private val viewBinding: ItemSubscriptionListBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        fun onBind(position: Int) {
            val row = subscrptionList[position]
            viewBinding.type = true
            viewBinding.subscriptionModelList = row
            viewBinding.clickInterface = clickListener
            viewBinding.topic = row.topic
        }
    }
}
