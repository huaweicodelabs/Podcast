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

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.* // ktlint-disable no-wildcard-imports
import android.view.View
import android.widget.* // ktlint-disable no-wildcard-imports
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.api.bean.HwAudioPlayItem
import com.huawei.hms.audiokit.player.callback.HwAudioConfigCallBack
import com.huawei.hms.audiokit.player.manager.* // ktlint-disable no-wildcard-imports
import com.huawei.hms.kit.awareness.barrier.HeadsetBarrier
import com.huawei.hms.kit.awareness.status.HeadsetStatus
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.data.model.RecordingList
import com.huawei.podcast.kotlin.database.model.PodCasts
import com.huawei.podcast.kotlin.receiver.HeadsetBarrierReceiver
import com.huawei.podcast.kotlin.services.AudioNotificationService
import com.huawei.podcast.kotlin.utils.Constants
import com.huawei.podcast.kotlin.utils.Constants.CANCEL_NOTIFICATION
import com.huawei.podcast.kotlin.utils.Constants.FIVE_HUNDRED
import com.huawei.podcast.kotlin.utils.Constants.HUNDRED
import com.huawei.podcast.kotlin.utils.Constants.MSG_REFRESH
import com.huawei.podcast.kotlin.utils.Constants.PUSH_TOKEN
import com.huawei.podcast.kotlin.utils.Constants.REQUEST_CODE_TWO
import com.huawei.podcast.kotlin.utils.Constants.TEN
import com.huawei.podcast.kotlin.utils.Constants.THOUSAND
import com.huawei.podcast.kotlin.utils.Constants.ZERO
import com.huawei.podcast.kotlin.utils.NotificationUtils
import com.huawei.podcast.kotlin.utils.NotificationUtils.addChannel
import com.huawei.podcast.kotlin.utils.Utils.addBarrier
import com.huawei.podcast.kotlin.utils.localeString
import kotlinx.android.synthetic.main.activity_play_audio.*
import kotlinx.android.synthetic.main.include_play_audio.*
import java.lang.Exception
import java.util.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.CopyOnWriteArrayList

class PlayAudioActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = PlayAudioActivity::class.java.simpleName
    private var mHwAudioPlayerManager: HwAudioPlayerManager? = null
    private var mHwAudioConfigManager: HwAudioConfigManager? = null
    private var mHwAudioQueueManager: HwAudioQueueManager? = null
    private var builder: NotificationCompat.Builder? = null
    private val mTempListeners: MutableList<HwAudioStatusListener> = CopyOnWriteArrayList()
    private var isReallyPlaying = true
    private var playList: List<HwAudioPlayItem> = arrayListOf()
    private var playItemList: MutableList<HwAudioPlayItem> = ArrayList()
    private var position = Constants.ZERO
    private var eList: ArrayList<PodCasts> = arrayListOf()
    private var recordingList: ArrayList<RecordingList> = arrayListOf()
    private var mBarrierReceiver: HeadsetBarrierReceiver? = null
    private var mPosOverride: Long = -1
    private var mDuration: Long = -1
    private var mFromTouch = false
    private var mTempPosition: Long = -5
    private var mCurrentBufferPercent = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_audio)
        /*Notification*/
        val audioService = Intent(this, AudioNotificationService::class.java)
        startService(audioService)
        /*on click listener*/
        playButtonImageView.setOnClickListener(this)
        nextSongImageView.setOnClickListener(this)
        previousSongImageView.setOnClickListener(this)
        playButtonImageView.setOnClickListener(this)
        img_back_arrow.setOnClickListener(this)
        /*seekBar*/
        musicSeekBar.setOnSeekBarChangeListener(mSeekListener)
        musicSeekBar.max = THOUSAND
        musicSeekBar.progress = ZERO
        musicSeekBar.secondaryProgress = ZERO
        /*podCast episode list*/
        if (intent.getSerializableExtra(getString(R.string.episode_list)) != null) {
            eList =
                intent.getSerializableExtra(getString(R.string.episode_list)) as ArrayList<PodCasts>
            position = intent.getIntExtra(getString(R.string.position), position)
        }

        /*recording list*/
        if (intent.getSerializableExtra(getString(R.string.recording_list)) != null) {
            recordingList =
                intent.getSerializableExtra(getString(R.string.recording_list)) as ArrayList<RecordingList>
            position = intent.getIntExtra(getString(R.string.position), position)
        }
        /*Audio play*/
        AudioPlayTask(this).execute()
        if (mHwAudioQueueManager != null) {
            setSongDetails(mHwAudioQueueManager?.currentPlayItem)
        }
        /*notification and cancel notification */
        val receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction(PUSH_TOKEN)
        filter.addAction(CANCEL_NOTIFICATION)
        this@PlayAudioActivity.registerReceiver(receiver, filter)
        /*
        <<<<<<<Awareness Headset>>>>>>>>>>>>>>>>
        */
        val barrierReceiverAction =
            application.packageName + resources.getString(R.string.headset_action)
        val intentBarrier = Intent(barrierReceiverAction)
        // This depends on what action you want Awareness Kit to trigger when the barrier status changes.
        val mPendingIntent =
            PendingIntent.getBroadcast(this, ZERO, intentBarrier, PendingIntent.FLAG_UPDATE_CURRENT)
        // Register a broadcast receiver to receive the broadcast sent by Awareness Kit when the barrier status changes.
        mBarrierReceiver = HeadsetBarrierReceiver()
        registerReceiver(mBarrierReceiver, IntentFilter(barrierReceiverAction))
        val keepingConnectedBarrier = HeadsetBarrier.keeping(HeadsetStatus.CONNECTED)
        addBarrier(this, KEEPING_BARRIER_LABEL, keepingConnectedBarrier, mPendingIntent)
    }

    override fun onClick(v: View) {
        val drawablePlay = ContextCompat.getDrawable(this, R.drawable.ic_play_arrow)
        val drawablePause = ContextCompat.getDrawable(this, R.drawable.ic_pause)
        when (v.id) {
            R.id.playButtonImageView ->
                if (playButtonImageView?.drawable?.constantState == drawablePlay?.constantState) {
                    if (mHwAudioPlayerManager != null) {
                        mHwAudioPlayerManager?.play()
                        playButtonImageView?.setImageDrawable(drawablePause)
                        isReallyPlaying = true
                    }
                } else if (playButtonImageView?.drawable?.constantState == drawablePause?.constantState) {
                    if (mHwAudioPlayerManager != null) {
                        mHwAudioPlayerManager?.pause()
                        playButtonImageView?.setImageDrawable(drawablePlay)
                        isReallyPlaying = false
                    }
                }
            R.id.nextSongImageView -> if (mHwAudioPlayerManager != null) {
                mHwAudioPlayerManager?.playNext()
                isReallyPlaying = true
            }
            R.id.previousSongImageView -> if (mHwAudioPlayerManager != null) {
                mHwAudioPlayerManager?.playPre()
                isReallyPlaying = true
            }
            R.id.img_back_arrow -> onBackPressed()
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (CANCEL_NOTIFICATION == action) {
                stop()
            }
            if (PUSH_TOKEN == action) {
                val token = intent.getStringExtra(getString(R.string.token))
            }
        }
    }

    var mPlayListener: HwAudioStatusListener = object : HwAudioStatusListener {
        override fun onSongChange(hwAudioPlayItem: HwAudioPlayItem) {
            setSongDetails(hwAudioPlayItem)
        }

        override fun onQueueChanged(list: List<HwAudioPlayItem>) {
            if (mHwAudioPlayerManager != null && list.isNotEmpty() && !isReallyPlaying) {
                mHwAudioPlayerManager?.play()
                isReallyPlaying = true
                playButtonImageView?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PlayAudioActivity,
                        R.drawable.ic_pause
                    )
                )
            }
        }

        override fun onBufferProgress(percent: Int) {}
        override fun onPlayProgress(currentPosition: Long, duration: Long) {}
        override fun onPlayCompleted(isStopped: Boolean) {
            if (mHwAudioPlayerManager != null && isStopped) {
                mHwAudioPlayerManager?.playNext()
            }
            isReallyPlaying = !isStopped
        }

        override fun onPlayError(errorCode: Int, isUserForcePlay: Boolean) {
            Toast.makeText(
                this@PlayAudioActivity,
                getString(R.string.can_not_play),
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onPlayStateChange(isPlaying: Boolean, isBuffering: Boolean) {
            refresh()
            if (isPlaying || isBuffering) {
                playButtonImageView?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PlayAudioActivity,
                        R.drawable.ic_pause
                    )
                )
                isReallyPlaying = true
            } else {
                playButtonImageView?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PlayAudioActivity,
                        R.drawable.ic_play_arrow
                    )
                )
                isReallyPlaying = false
                if (builder != null) builder?.setOngoing(false) // probably not working as intended
            }
        }
    }

    private val mSeekListener: SeekBar.OnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {
            private var mProgress = ZERO
            override fun onStartTrackingTouch(bar: SeekBar) {
                mFromTouch = true
            }

            override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
                if (!fromuser) {
                    return
                }
                mProgress = progress
            }

            override fun onStopTrackingTouch(bar: SeekBar) {
                mPosOverride = mDuration * mProgress / THOUSAND
                seek(mPosOverride)
                if (!mFromTouch) {
                    refreshNow(true)
                }
                mFromTouch = false
                mPosOverride = -1
                if (mDuration == -1L && mTempPosition == -1L) {
                    setProgressValue(ZERO)
                }
            }
        }

    private fun setProgressValue(pos: Int) {
        musicSeekBar.progress = pos
    }

    fun setSongDetails(currentItem: HwAudioPlayItem?) {
        if (currentItem != null) {
            txt_author.text = currentItem.singer
            txt_title.text = currentItem.audioTitle
            txt_header_title.text = currentItem.audioTitle
        } else {
            txt_title.setText(R.string.er_audio_play)
        }
    }

    /**
     * seek
     *
     * @param pos pos
     */
    fun seek(pos: Long) {
        if (mHwAudioPlayerManager == null) {
            return
        }
        mHwAudioPlayerManager?.seekTo(pos.toInt())
    }

    private inner class AudioPlayTask(val context: Context) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            val hwAudioPlayerConfig = HwAudioPlayerConfig(context)
            HwAudioManagerFactory.createHwAudioManager(
                hwAudioPlayerConfig,
                object : HwAudioConfigCallBack {
                    override fun onSuccess(hwAudioManager: HwAudioManager) {
                        try {
                            mHwAudioManager = hwAudioManager
                            mHwAudioPlayerManager = hwAudioManager.playerManager
                            mHwAudioConfigManager = hwAudioManager.configManager
                            mHwAudioQueueManager = hwAudioManager.queueManager
                            playList = getOnlinePlaylist()
                            if (playList.isNotEmpty()) {
                                play()
                            }
                            doListenersAndNotifications()
                        } catch (e: Exception) {
                        }
                    }

                    override fun onError(errorCode: Int) {
                    }
                }
            )
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    addListener(mPlayListener)
                },
                HUNDRED
            )

            super.onPostExecute(aVoid)
        }
    }

    private fun play() {
        if (mHwAudioPlayerManager != null && mHwAudioQueueManager != null && mHwAudioQueueManager?.allPlaylist != null) {
            if (mHwAudioQueueManager?.allPlaylist === playItemList) {
                mHwAudioPlayerManager?.play(position)
            } else {
                mHwAudioPlayerManager?.playList(playList, position, ZERO)
                mHwAudioPlayerManager?.playMode = ZERO
                mHwAudioQueueManager?.setPlaylist(playList)
            }
        }
    }

    private fun getOnlinePlaylist(): List<HwAudioPlayItem> {
        if (recordingList.size == ZERO) {
            for (i in eList.indices) {
                val audioPlayItem = HwAudioPlayItem()
                audioPlayItem.audioId = eList[i].id.toString()
                audioPlayItem.singer = eList[i].author
                audioPlayItem.onlinePath = eList[i].url
                audioPlayItem.setOnline(Constants.REQUEST_CODE_ONE)
                audioPlayItem.audioTitle = eList[i].title
                playItemList.add(audioPlayItem)
            }
        } else {
            for (i in recordingList.indices) {
                val audioPlayItem = HwAudioPlayItem()
                audioPlayItem.audioId = i.toString()
                audioPlayItem.filePath = recordingList[i].filePath
                audioPlayItem.audioTitle = recordingList[i].title
                playItemList.add(audioPlayItem)
            }
        }
        return playItemList
    }

    private fun doListenersAndNotifications() {
        Handler(Looper.getMainLooper()).post {
            for (listener in mTempListeners) {
                try {
                    mHwAudioManager.addPlayerStatusListener(listener)
                } catch (e: RemoteException) {
                }
            }
            mHwAudioConfigManager?.setSaveQueue(true)
            mHwAudioConfigManager?.setNotificationFactory { notificationConfig ->
                builder = NotificationCompat.Builder(application, getString(R.string.str_null))
                val remoteViews =
                    RemoteViews(application.packageName, R.layout.notification_player)
                builder?.setContent(remoteViews)
                builder?.setSmallIcon(R.drawable.ic_share)
                builder?.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                builder?.setCustomBigContentView(remoteViews)
                addChannel(application, NotificationUtils.NOTIFY_CHANNEL_ID_PLAY, builder!!)
                val isQueueEmpty = mHwAudioManager.queueManager.isQueueEmpty
                val isPlaying = mHwAudioManager.playerManager.isPlaying && !isQueueEmpty
                remoteViews.setImageViewResource(
                    R.id.image_toggle,
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                )
                val playItem = mHwAudioManager.queueManager.currentPlayItem
                remoteViews.setTextViewText(R.id.text_song, playItem.audioTitle)
                remoteViews.setTextViewText(R.id.text_artist, playItem.singer)
                remoteViews.setImageViewResource(R.id.image_last, R.drawable.ic_skip_previous)
                remoteViews.setImageViewResource(R.id.image_next, R.drawable.ic_skip_next)
                remoteViews.setOnClickPendingIntent(
                    R.id.image_last,
                    notificationConfig.prePendingIntent
                )
                remoteViews.setOnClickPendingIntent(
                    R.id.image_toggle,
                    notificationConfig.playPendingIntent
                )
                remoteViews.setOnClickPendingIntent(
                    R.id.image_next,
                    notificationConfig.nextPendingIntent
                )
                remoteViews.setOnClickPendingIntent(R.id.image_close, getCancelPendingIntent())
                remoteViews.setOnClickPendingIntent(R.id.layout_content, getMainIntent())
                builder?.build()
            }
        }
    }

    private fun getCancelPendingIntent(): PendingIntent {
        val closeIntent = Intent(CANCEL_NOTIFICATION)
        closeIntent.setPackage(application.packageName)
        return PendingIntent.getBroadcast(
            application, REQUEST_CODE_TWO,
            closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getMainIntent(): PendingIntent {
        val intent = Intent(getString(R.string.str_intent))
        intent.addCategory(getString(R.string.str_cat))
        intent.setClass(application.baseContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        return PendingIntent.getActivity(application, ZERO, intent, ZERO)
    }

    fun addListener(listener: HwAudioStatusListener) {
        if (mHwAudioManager != null) {
            try {
                mHwAudioManager.addPlayerStatusListener(listener)
            } catch (e: RemoteException) {
            }
        } else {
            mTempListeners.add(listener)
        }
    }

    /**
     * getBufferPercentage
     *
     * @return buffer percent
     */
    val bufferPercentage: Int
        get() = if (mHwAudioPlayerManager == null) {
            ZERO
        } else mHwAudioPlayerManager!!.bufferPercent

    /**
     * getPosition
     *
     * @return now playing position
     */
    fun getPosition(): Long {
        return if (mHwAudioPlayerManager == null) {
            0
        } else mHwAudioPlayerManager!!.offsetTime
    }

    val duration: Long
        get() = if (mHwAudioPlayerManager == null) {
            0
        } else mHwAudioPlayerManager!!.duration

    override fun onDestroy() {
        super.onDestroy()
        if (mBarrierReceiver != null) {
            unregisterReceiver(mBarrierReceiver)
        }
    }

    fun refresh() {
        if (isQueueEmpty) {
            return
        }
        refreshNow(true)
    }

    /**
     * is queue empty
     *
     * @return true:empty
     */
    private val isQueueEmpty: Boolean
        get() = mHwAudioQueueManager != null && mHwAudioQueueManager!!.isQueueEmpty

    private fun refreshNow(force: Boolean) {
        RefreshTask().execute(force)
    }

    private inner class RefreshTask : AsyncTask<Boolean?, Void?, Boolean?>() {
        var curPosString: String? = null
        var durationString: String? = null
        var progressValue = ZERO
        override fun doInBackground(vararg params: Boolean?): Boolean? {
            val force = params[ZERO]
            val percent = bufferPercentage
            var position = if (mPosOverride < ZERO) getPosition() else mPosOverride
            val duration = duration
            if (!force!! && mTempPosition == position && mDuration == duration && percent == mCurrentBufferPercent) {
                return false
            }
            mTempPosition = position
            mDuration = duration
            mCurrentBufferPercent = percent
            if (duration > ZERO) {
                if (position > duration - FIVE_HUNDRED) {
                    position = duration
                }
                val currentTime = position.toDouble() / THOUSAND
                var timePassed =
                    if (currentTime - currentTime.toLong() > 0.8) currentTime.toLong() + 1 else currentTime.toLong()
                val totalTime = duration / THOUSAND
                if (timePassed > totalTime) {
                    timePassed = totalTime
                }
                curPosString = localeString(this@PlayAudioActivity, timePassed)
                durationString = localeString(this@PlayAudioActivity, totalTime)
                progressValue = if (0L == totalTime) ZERO else (THOUSAND * timePassed / totalTime).toInt()
            } else {
                curPosString = localeString(getString(R.string.zero_duration))
                durationString = curPosString
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            queueNextRefresh(if (result != null && result) doFresh() else FIVE_HUNDRED)
        }

        private fun doFresh(): Long {
            mHandler.removeCallbacks(mClearTask)
            val remaining = THOUSAND - mTempPosition % THOUSAND
            totalDurationTextView.text = durationString
            progressTextView.text = curPosString
            setProgressValue(progressValue)
            musicSeekBar.secondaryProgress = mCurrentBufferPercent * TEN
            return remaining
        }
    }

    private fun queueNextRefresh(delay: Long) {
        if (isPlaying) {
            val msg = mHandler.obtainMessage(MSG_REFRESH)
            mHandler.removeMessages(MSG_REFRESH)
            mHandler.sendMessageDelayed(msg, delay)
        }
    }

    public override fun onStop() {
        mHandler.removeMessages(MSG_REFRESH)
        super.onStop()
    }

    private val mClearTask = Runnable {
        totalDurationTextView.text = localeString(getString(R.string.zero_duration))
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (MSG_REFRESH == msg.what) {
                refreshNow(false)
            }
        }
    }

    /**
     * check is playing
     *
     * @return playing
     */
    private val isPlaying: Boolean
        get() = mHwAudioPlayerManager != null && mHwAudioPlayerManager!!.isPlaying

    /**
     * Stop
     */
    fun stop() {
        if (mHwAudioPlayerManager == null) {
            return
        }
        mHwAudioPlayerManager?.stop()
    }

    companion object {
        private const val KEEPING_BARRIER_LABEL = "keeping barrier label"
        private lateinit var mHwAudioManager: HwAudioManager
    }
}
