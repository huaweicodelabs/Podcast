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

import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.applinking.AppLinking
import com.huawei.agconnect.applinking.ShortAppLinking
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.analytics.HiAnalyticsTools
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.data.model.PodCastList
import com.huawei.podcast.kotlin.database.AppDatabase
import com.huawei.podcast.kotlin.database.CloudDBZoneWrapper
import com.huawei.podcast.kotlin.database.model.Favourites
import com.huawei.podcast.kotlin.database.model.PodCasts
import com.huawei.podcast.kotlin.preference.SharedPreference
import com.huawei.podcast.kotlin.utils.Constants
import kotlinx.android.synthetic.main.activity_episode_details.*
import kotlinx.android.synthetic.main.include_episode_details.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EpisodeDetailsActivity :
    AppCompatActivity(),
    View.OnClickListener,
    CloudDBZoneWrapper.UiCallBack {

    private var position: Int = Constants.ZERO
    private val mCloudDBZoneWrapper: CloudDBZoneWrapper = CloudDBZoneWrapper()
    private var instance: HiAnalyticsInstance? = null
    private lateinit var db: AppDatabase
    private lateinit var txtFav: TextView
    private var favPodCastList: List<Favourites> = arrayListOf()
    private lateinit var episodeList: ArrayList<PodCasts>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_details)
        setupUI()
    }

    private fun setupUI() {
        if (intent.getSerializableExtra(getString(R.string.episode_list)) != null) {
            episodeList =
                intent.getSerializableExtra(getString(R.string.episode_list)) as ArrayList<PodCasts>
            position = intent.getIntExtra(getString(R.string.position), position)
        }
        /*database*/
        db = AppDatabase(this)
        /*Analytics*/
        instance = HiAnalytics.getInstance(this)
        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog()
        /*details*/
        setDetails(position)
        /*onclick listener*/
        img_back_arrow.setOnClickListener(this)
        txt_stream.setOnClickListener(this)
        img_sub_menu.setOnClickListener(this)
        txt_download.setOnClickListener(this)
        // Load the default banner ad.
        loadDefaultBannerAd()
        try {
            mCloudDBZoneWrapper.addCallBacks(this)
            mCloudDBZoneWrapper.createObjectType()
            mCloudDBZoneWrapper.openCloudDBZoneV2()
        } catch (e: AGConnectCloudDBException) {
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_back_arrow -> onBackPressed()
            R.id.txt_stream -> {
                /*play back history*/
                GlobalScope.launch {
                    episodeList[position].let {
                        val data = it.title?.let { it1 -> db.todoDao().findByTitle(it1) }
                        if (!(data?.title.equals(it.title))) {
                            insertData()
                        }
                    }
                }
                val i = Intent(this, PlayAudioActivity::class.java).apply {
                    putExtra(getString(R.string.episode_list), episodeList)
                    putExtra(getString(R.string.position), position)
                }
                startActivity(i)
            }
            R.id.img_sub_menu -> {
                if (SharedPreference.contains(getString(R.string.username))) {
                    showSubmenus()
                } else {
                    login()
                }
            }
            R.id.txt_download -> {
                val folder =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
                val filtered =
                    folder?.listFiles()?.filter { it.name == episodeList[position].title }
                if (filtered.isNullOrEmpty()) {
                    val downloadManager =
                        this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val request = DownloadManager.Request(Uri.parse(episodeList[position].url))
                    request.setTitle(title)
                    request.setDescription(getString(R.string.audio_downloadmanager))
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_PODCASTS,
                        episodeList[position].title
                    )
                    downloadManager.enqueue(request)
                    Toast.makeText(
                        this, getString(R.string.file_download_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this, getString(R.string.file_downloaded),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setDetails(position: Int) {
        txt_title.text = episodeList[position].title
        txt_details.text = episodeList[position].description
        txt_episode.text = episodeList[position].date
        txt_name.text = episodeList[position].title
    }

    private fun showSubmenus() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_episode_details)
        val window: Window = dialog.window!!
        val wlp: WindowManager.LayoutParams = window.attributes
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT
        wlp.gravity = Gravity.END or Gravity.TOP
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = wlp
        val txtNext = dialog.findViewById(R.id.txt_next) as TextView
        val txtShare = dialog.findViewById(R.id.txt_share) as TextView
        txtFav = dialog.findViewById(R.id.txt_fav)
        if (episodeList[position].isInFavourites)
            txtFav.setText(Constants.ADDED)
        else
            txtFav.setText(R.string.add_fav)

        /*next episode*/
        txtNext.setOnClickListener {
            position += Constants.REQUEST_CODE_ONE
            if (position < episodeList.size) {
                setDetails(position)
            } else {
                position = Constants.ZERO
                setDetails(position)
            }
            if (episodeList[position].isInFavourites!!)
                txtFav.setText(R.string.added_to_fav)
            else
                txtFav.setText(R.string.add_fav)
        }
        /*share*/
        txtShare.setOnClickListener {
            val builder = AppLinking.Builder().setUriPrefix(Constants.domainUriPrefix)
                .setDeepLink(Uri.parse(Constants.deepLink))
                .setAndroidLinkInfo(AppLinking.AndroidLinkInfo.Builder().build())

            builder.buildShortAppLinking()
                .addOnSuccessListener { shortAppLinking: ShortAppLinking ->
                    shareLink(shortAppLinking.shortUrl.toString())
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        getString(R.string.app_link),
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
        /*Add to favourites*/
        txtFav.setOnClickListener {
            val favPodCast = Favourites()
            val podCasts: PodCasts =
                episodeList[position]
            favPodCast.setPodcast_id(
                SharedPreference.getValueString(
                    getString(R.string.userid)
                ) + podCasts.id.toString()
            )
            favPodCast.actual_podcast_id = podCasts.id
            favPodCast.podcast_title = podCasts.title
            favPodCast.url = podCasts.url
            favPodCast.isFavourite = true
            favPodCast.user_id = SharedPreference.getValueString(getString(R.string.userid))
            favPodCast.user_name = SharedPreference.getValueString(getString(R.string.username))
            var isExist = false
            if (favPodCastList.size > Constants.ZERO) {
                for (fav in favPodCastList) {
                    if (fav.actual_podcast_id.equals(podCasts.getId())) {
                        isExist = true
                        break
                    }
                }
            }
            if (isExist) {
                podCasts.isInFavourites = false
                showToast(getString(R.string.already_added_to_fav))
            } else {
                podCasts.isInFavourites = true
                mCloudDBZoneWrapper.upsertFavPodCasts(favPodCast)
            }
        }
        dialog.show()
    }

    /**
     * Login with AGCUI
     */
    private fun login() {

        startActivityForResult(
            Intent(
                this,
                LoginActivity::class.java
            ),
            Constants.SIGNIN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_OK) {
            getFavouritePodcasts()
        }
    }

    /**
     * Sharing App link
     */
    private fun shareLink(agcLink: String?) {
        if (agcLink != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = Constants.type
                putExtra(Intent.EXTRA_TEXT, agcLink)
            }
            startActivity(intent)
            val bundle = Bundle()
            bundle.putString(getString(R.string.share), getString(R.string.count))
            instance?.onEvent(getString(R.string.count), bundle)
        }
    }

    /**
     * Load the default banner ad.
     */
    private fun loadDefaultBannerAd() {
        hw_banner_view.adListener = adListener
        hw_banner_view.setBannerRefresh(refreshTime.toLong())
        val adParam = AdParam.Builder().build()
        hw_banner_view.loadAd(adParam)
    }

    /**
     * Ad listener.
     */
    private val adListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            // Called when an ad is loaded successfully.
        }

        override fun onAdFailed(errorCode: Int) {
            // Called when an ad fails to be loaded.
        }

        override fun onAdOpened() {
            // Called when an ad is opened.
        }

        override fun onAdClicked() {
            // Called when a user taps an ad.
        }

        override fun onAdLeave() {
            // Called when a user has left the app.
        }

        override fun onAdClosed() {
            // Called when an ad is closed.
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onGetPodCasts(podCastList: ArrayList<PodCasts>) {
    }

    override fun onAddOrQuery(favPodCastList: List<Favourites>) {
        if (favPodCastList.isNotEmpty()) {
            this.favPodCastList = favPodCastList
            for (i in episodeList.indices) {
                for (j in favPodCastList.indices) {
                    if (episodeList[i].id == favPodCastList[j]
                        .actual_podcast_id
                    ) {
                        episodeList[i].isInFavourites = true
                    }
                }
            }
        }
    }

    override fun onSubscribe(favPodCastList: List<Favourites>?) {
    }

    override fun onDelete(isDeleted: Boolean?) {
    }

    override fun updateUiOnError(errorMessage: String?) {
        if (errorMessage.equals(Constants.ADDED)) {
            showToast(getString(R.string.added_to_fav))
            txtFav.setText(R.string.added_to_fav)
            getFavouritePodcasts()
        } else {
        }
    }

    override fun onDBReady(value: Boolean) {
        getFavouritePodcasts()
    }

    private fun insertData() {
        episodeList[position].let {
            db.todoDao().insertAll(
                PodCastList(
                    it.id,
                    it.description,
                    it.url,
                    it.author,
                    it.date,
                    it.title,
                    it.category,
                    position
                )
            )
        }
    }

    companion object {
        const val refreshTime = 30
    }

    private fun getFavouritePodcasts() {
        mCloudDBZoneWrapper.getFavouritePodCasts()
    }
}
