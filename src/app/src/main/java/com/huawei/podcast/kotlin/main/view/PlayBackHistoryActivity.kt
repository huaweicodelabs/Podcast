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

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.data.model.PodCastList
import com.huawei.podcast.kotlin.database.AppDatabase
import com.huawei.podcast.kotlin.database.CloudDBZoneWrapper
import com.huawei.podcast.kotlin.database.model.Favourites
import com.huawei.podcast.kotlin.database.model.PodCasts
import com.huawei.podcast.kotlin.interfaces.EpisodeClickListener
import com.huawei.podcast.kotlin.main.adapter.PlayBackAdapter
import com.huawei.podcast.kotlin.utils.ProgressDialog
import com.huawei.podcast.kotlin.utils.isNetworkConnected
import kotlinx.android.synthetic.main.activity_favorites.*
import kotlinx.android.synthetic.main.include_header.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PlayBackHistoryActivity : AppCompatActivity(), EpisodeClickListener, CloudDBZoneWrapper.UiCallBack {

    private lateinit var adapter: PlayBackAdapter
    private lateinit var episodeList: ArrayList<PodCasts>
    private lateinit var dialog: Dialog
    private lateinit var mData: List<PodCastList>
    private val mCloudDBZoneWrapper: CloudDBZoneWrapper = CloudDBZoneWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        setUpUI()
    }

    private fun setUpUI() {
        if (isNetworkConnected(this)) {
            dialog = ProgressDialog.showProgress(this)
            try {
                mCloudDBZoneWrapper.addCallBacks(this)
                mCloudDBZoneWrapper.createObjectType()
                mCloudDBZoneWrapper.openCloudDBZoneV2()
            } catch (e: AGConnectCloudDBException) {
            }
            getPlayList()
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.no_internet),
                Toast.LENGTH_LONG
            ).show()
        }
        txt_title.text = getString(R.string.str_play_back)
        txt_no_data.text = getString(R.string.no_play_back)
        img_back_arrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getPlayList() {
        /*get play list from data base*/
        GlobalScope.launch {
            val db = AppDatabase(this@PlayBackHistoryActivity)
            mData = db.todoDao().getAll()
            runOnUiThread {
                if (mData.isNotEmpty()) {
                    rv_fav.layoutManager =
                        LinearLayoutManager(this@PlayBackHistoryActivity).also {
                            rv_fav.layoutManager = it
                        }
                    adapter = PlayBackAdapter(this@PlayBackHistoryActivity, mData)
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

    override fun onItemClick(episode: PodCastList, position: Int) {
        val i = Intent(this, PlayAudioActivity::class.java).apply {
            putExtra(getString(R.string.episode_list), episodeList)
            putExtra(getString(R.string.position), mData[position].position)
        }
        startActivity(i)
    }

    override fun onGetPodCasts(podCastList: ArrayList<PodCasts>) {
        episodeList = podCastList
    }

    override fun onAddOrQuery(podCastList: List<Favourites>) {
    }

    override fun onSubscribe(podCastList: List<Favourites>?) {
    }

    override fun onDelete(isDeleted: Boolean?) {
    }

    override fun updateUiOnError(errorMessage: String?) {
    }

    override fun onDBReady(value: Boolean) {
        dialog.dismiss()
        mCloudDBZoneWrapper.getAllPodCasts()
    }
}
