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
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.main.view.DetailsActivity
import kotlinx.android.synthetic.main.item_search.view.*
import java.util.* // ktlint-disable no-wildcard-imports
import kotlin.collections.ArrayList

class SearchAdapter(private var mContext: Context, private var categoryList: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var categoryFilterList = ArrayList<String>()
    class SearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    init {
        categoryFilterList = categoryList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val categoryListView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return SearchHolder(categoryListView)
    }

    override fun getItemCount(): Int {
        return categoryFilterList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.txt_search.text = categoryFilterList[position]
        holder.itemView.cv_search.setOnClickListener {
            val i = Intent(mContext, DetailsActivity::class.java)
            i.putExtra(mContext.getString(R.string.topic), categoryFilterList[position])
            mContext.startActivity(i)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                categoryFilterList = if (charSearch.isEmpty()) {
                    categoryList
                } else {
                    val resultList = ArrayList<String>()
                    for (row in categoryList) {
                        if (row.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = categoryFilterList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                categoryFilterList = results?.values as ArrayList<String>
                notifyDataSetChanged()
            }
        }
    }

    fun setList(sList: ArrayList<String>) {
        categoryFilterList = sList
        notifyDataSetChanged()
    }
}
