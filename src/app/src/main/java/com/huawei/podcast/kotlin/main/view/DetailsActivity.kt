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
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hms.push.HmsMessaging
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.data.model.SubscribeModel
import com.huawei.podcast.kotlin.database.AppDatabase
import com.huawei.podcast.kotlin.database.CloudDBZoneWrapper
import com.huawei.podcast.kotlin.database.model.PodCasts
import com.huawei.podcast.databinding.ActivityDetailsBinding
import com.huawei.podcast.kotlin.database.model.Favourites
import com.huawei.podcast.kotlin.interfaces.OnClickPodCast
import com.huawei.podcast.kotlin.main.adapter.EpisodeAdapter
import com.huawei.podcast.kotlin.main.viewmodel.DetailsViewModel
import com.huawei.podcast.kotlin.utils.Constants
import com.huawei.podcast.kotlin.utils.ProgressDialog
import com.huawei.podcast.kotlin.utils.isNetworkConnected
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class DetailsActivity : AppCompatActivity(), OnClickPodCast, CloudDBZoneWrapper.UiCallBack {
    private val mCloudDBZoneWrapper: CloudDBZoneWrapper = CloudDBZoneWrapper()
    private val detailsViewModel: DetailsViewModel by viewModel()
    private lateinit var adapter: EpisodeAdapter
    private lateinit var dialog: Dialog
    private lateinit var activityDetailsBinding: ActivityDetailsBinding
    private lateinit var episodeList: ArrayList<PodCasts>
    private lateinit var db: AppDatabase
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details)
        activityDetailsBinding.lifecycleOwner = this
        db = AppDatabase(this)
        setupUI()
    }

    private fun setupUI() {
        if (isNetworkConnected(this)) {
            dialog = ProgressDialog.showProgress(this)
            try {
                mCloudDBZoneWrapper.addCallBacks(this)
                mCloudDBZoneWrapper.createObjectType()
                mCloudDBZoneWrapper.openCloudDBZoneV2()
            } catch (e: AGConnectCloudDBException) {
            }
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.no_internet),
                Toast.LENGTH_LONG
            ).show()
        }
        episodeList = arrayListOf()
        val topic = intent.extras?.getString(getString(R.string.topic))
        activityDetailsBinding.txtTitle.text = topic
        rv_episodes.layoutManager =
            LinearLayoutManager(this).also { rv_episodes.layoutManager = it }
        adapter = EpisodeAdapter(this, this)
        rv_episodes.adapter = adapter
        activityDetailsBinding.imgBackArrow.setOnClickListener {
            onBackPressed()
        }

        GlobalScope.launch {
            val data = topic?.let { db.todoDao().findBySubscribeTitle(it) }
            if (data != null) {
                runOnUiThread {
                    if (data.topic == topic) {
                        activityDetailsBinding.txtSubscribe.text =
                            getString(R.string.str_unsubscribe)
                    } else {
                        activityDetailsBinding.txtSubscribe.text = getString(R.string.subscribe)
                    }
                }
            }
        }

        activityDetailsBinding.txtSubscribe.setOnClickListener {
            if (topic != null) {
                val validTopic = topic.replace(Constants.SPACE_STRING.toRegex(), Constants.EMPTY_STRING)
                if (activityDetailsBinding.txtSubscribe.text == getString(R.string.str_unsubscribe)) {
                    unSubscribeTopic(validTopic)
                } else {
                    subscribeTopic(validTopic)
                }
            }
        }
    }

    /**
     * to subscribe to topics in asynchronous mode.
     */
    private fun subscribeTopic(topic: String) {
        try {
            HmsMessaging.getInstance(applicationContext)
                .subscribe(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(
                            getString(R.string.push_demo_log),
                            getString(R.string.subscribe_complete)
                        )

                        GlobalScope.launch {
                            val subs = SubscribeModel(position, topic, getString(R.string.double_quotes))
                            db.todoDao().insertSubscribeList(subs)
                        }
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                resources.getString(R.string.subscribe_complete),
                                Toast.LENGTH_LONG
                            ).show()
                            activityDetailsBinding.txtSubscribe.text = getString(R.string.str_unsubscribe)
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.subscribe_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                getString(R.string.subscribe_exception),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * to unsubscribe to topics in asynchronous mode.
     */
    private fun unSubscribeTopic(topic: String) {
        try {
            HmsMessaging.getInstance(this@DetailsActivity)
                .unsubscribe(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        GlobalScope.launch {
                            val data = topic.let { db.todoDao().findBySubscribeTitle(it) }
                            db.todoDao().deleteSubscribe(data)
                        }
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.unsubscribe_complete),
                                Toast.LENGTH_LONG
                            ).show()
                            activityDetailsBinding.txtSubscribe.text = getString(R.string.subscribe)
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.unsubscribe_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } catch (e: java.lang.Exception) {
            Toast.makeText(
                applicationContext,
                getString(R.string.unsubscribe_exception),
                Toast.LENGTH_LONG
            )
        }
    }

    override fun onGetPodCasts(podCastList: ArrayList<PodCasts>) {
        dialog.dismiss()
        episodeList = podCastList
        adapter.setList(podCastList)
    }

    override fun onAddOrQuery(favPodCastList: List<Favourites>) {
    }

    override fun onSubscribe(favPodCastList: List<Favourites>?) {
    }

    override fun onDelete(isDeleted: Boolean?) {
    }

    override fun updateUiOnError(errorMessage: String?) {
    }

    override fun onDBReady(value: Boolean) {
        mCloudDBZoneWrapper.getAllPodCasts()
    }

    override fun onClickPodCast(episode: PodCasts, position: Int) {
        val i = Intent(this, EpisodeDetailsActivity::class.java).apply {
            putExtra(getString(R.string.episode_list), episodeList)
            putExtra(getString(R.string.position), position)
        }
        startActivity(i)
    }
}
