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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.database.CloudDBZoneWrapper
import com.huawei.podcast.kotlin.database.model.Favourites
import com.huawei.podcast.kotlin.database.model.PodCasts
import com.huawei.podcast.kotlin.interfaces.OnDeletePodCast
import com.huawei.podcast.kotlin.main.adapter.FavouritesAdapter
import com.huawei.podcast.kotlin.utils.Constants
import kotlinx.android.synthetic.main.activity_favorites.*
import kotlinx.android.synthetic.main.include_header.*

class FavouritesActivity : AppCompatActivity(), CloudDBZoneWrapper.UiCallBack, OnDeletePodCast {
    private val mCloudDBZoneWrapper: CloudDBZoneWrapper = CloudDBZoneWrapper()
    private lateinit var favouritePodCastList: ArrayList<Favourites>
    private lateinit var adapter: FavouritesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        setUpUi()
    }

    private fun setUpUi() {
        favouritePodCastList = arrayListOf()
        try {
            mCloudDBZoneWrapper.addCallBacks(this)
            mCloudDBZoneWrapper.createObjectType()
            mCloudDBZoneWrapper.openCloudDBZoneV2()
        } catch (e: AGConnectCloudDBException) {
        }
        txt_title.text = getString(R.string.str_fav)
        img_back_arrow.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onGetPodCasts(podCastList: ArrayList<PodCasts>) {
    }

    override fun onAddOrQuery(favPodCastList: List<Favourites>) {
        if (favPodCastList.isNotEmpty()) {
            favouritePodCastList.clear()
            favouritePodCastList = favPodCastList as ArrayList<Favourites>
            txt_no_data.visibility = View.GONE
            rv_fav.visibility = View.VISIBLE
            rv_fav.layoutManager = LinearLayoutManager(this).also { rv_fav.layoutManager = it }
            adapter = FavouritesAdapter(this, favouritePodCastList)
            rv_fav.adapter = adapter
        } else {
            txt_no_data.visibility = View.VISIBLE
            rv_fav.visibility = View.GONE
        }
    }
    override fun onSubscribe(favPodCastList: List<Favourites>?) {
    }

    override fun onDelete(isDeleted: Boolean?) {
        Toast.makeText(this, getString(R.string.removed_from_favourites), Toast.LENGTH_SHORT).show()
    }

    override fun updateUiOnError(errorMessage: String?) {
        Toast.makeText(
            this,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDBReady(value: Boolean) {
        mCloudDBZoneWrapper.getFavouritePodCasts()
    }

    override fun onDeletePodCast(podCast: Favourites, position: Int) {
        favouritePodCastList.remove(podCast)
        adapter.setList()
        if (adapter.itemCount == Constants.ZERO) {
            txt_no_data.visibility = View.VISIBLE
            rv_fav.visibility = View.GONE
        }
        mCloudDBZoneWrapper.deleteFavPodCast(podCast)
    }
}
