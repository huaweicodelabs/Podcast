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
import com.huawei.podcast.databinding.ItemFavoritesBinding
import com.huawei.podcast.kotlin.database.model.Favourites
import com.huawei.podcast.kotlin.interfaces.OnDeletePodCast

class FavouritesAdapter(
    val clickListener: OnDeletePodCast,
    var favouriteList: List<Favourites>
) : RecyclerView.Adapter<FavouritesAdapter.FavouriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val viewBinding: ItemFavoritesBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_favorites, parent, false
        )
        return FavouriteViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return favouriteList.size
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        holder.onBind(position)
    }
    inner class FavouriteViewHolder(private val viewBinding: ItemFavoritesBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        fun onBind(position: Int) {
            val row = favouriteList[position]
            viewBinding.eList = row
            viewBinding.type = true

            viewBinding.imgDownload.setOnClickListener {
                clickListener.onDeletePodCast(
                    favouriteList[layoutPosition],
                    position
                )
            }
        }
    }
    fun setList() {
        notifyDataSetChanged()
    }
}
