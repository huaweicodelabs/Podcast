/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.java.main.view;

import static com.huawei.podcast.java.utils.Constants.KEEPING_BARRIER_LABEL;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;

import com.huawei.hms.api.bean.HwAudioPlayItem;
import com.huawei.hms.api.config.NotificationConfig;
import com.huawei.hms.audiokit.player.callback.HwAudioConfigCallBack;
import com.huawei.hms.audiokit.player.manager.HwAudioConfigManager;
import com.huawei.hms.audiokit.player.manager.HwAudioManager;
import com.huawei.hms.audiokit.player.manager.HwAudioManagerFactory;
import com.huawei.hms.audiokit.player.manager.HwAudioPlayerConfig;
import com.huawei.hms.audiokit.player.manager.HwAudioPlayerManager;
import com.huawei.hms.audiokit.player.manager.HwAudioQueueManager;
import com.huawei.hms.audiokit.player.manager.HwAudioStatusListener;
import com.huawei.hms.audiokit.player.manager.INotificationFactory;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.HeadsetBarrier;
import com.huawei.hms.kit.awareness.status.HeadsetStatus;
import com.huawei.podcast.R;
import com.huawei.podcast.databinding.ActivityPlayAudioBinding;
import com.huawei.podcast.java.data.model.RecordingList;
import com.huawei.podcast.java.database.model.PodCasts;
import com.huawei.podcast.java.receiver.HeadsetBarrierReceiver;
import com.huawei.podcast.java.services.AudioNotificationService;
import com.huawei.podcast.java.utils.Constants;
import com.huawei.podcast.java.utils.NotificationUtils;
import com.huawei.podcast.java.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayAudioActivity extends AppCompatActivity implements View.OnClickListener {
    private static HwAudioManager mHwAudioManager;
    private HwAudioPlayerManager mHwAudioPlayerManager;
    private HwAudioConfigManager mHwAudioConfigManager;
    private HwAudioQueueManager mHwAudioQueueManager;
    NotificationCompat.Builder builder;
    private List<HwAudioStatusListener> mTempListeners = new CopyOnWriteArrayList<>();
    boolean isReallyPlaying = true;
    private List<HwAudioPlayItem> playList;
    List<HwAudioPlayItem> playItemList = new ArrayList<>();
    int position;
    private ArrayList<PodCasts> eList = new ArrayList<>();
    private ArrayList<RecordingList> recordingList = new ArrayList<>();
    ActivityPlayAudioBinding binding;
    HeadsetBarrierReceiver mBarrierReceiver;
    private long mPosOverride = Constants.MINUS_ONE;
    private long mDuration = Constants.MINUS_ONE;
    private boolean mFromTouch = false;
    private long mTempPosition = Constants.LONG_MINUS_FIVE;
    private int mCurrentBufferPercent = Constants.ZERO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_audio);
        Intent intent = new Intent(this, AudioNotificationService.class);
        startService(intent);
        binding.playButtonImageView.setOnClickListener(this);
        binding.nextSongImageView.setOnClickListener(this);
        binding.previousSongImageView.setOnClickListener(this);
        binding.playButtonImageView.setOnClickListener(this);
        binding.include.imgBackArrow.setOnClickListener(this);
        /* seekBar */
        binding.musicSeekBar.setOnSeekBarChangeListener(mSeekListener);
        binding.musicSeekBar.setMax(Constants.THOUSAND);
        binding.musicSeekBar.setProgress(Constants.ZERO);
        binding.musicSeekBar.setSecondaryProgress(Constants.ZERO);
        /* podCast episode list */
        Bundle bundle=getIntent().getExtras();
        if (getIntent().hasExtra(getString(R.string.episode_list))) {
            eList = (ArrayList<PodCasts>) bundle.getSerializable(getString(R.string.episode_list));
            position = getIntent().getIntExtra(getString(R.string.position), position);
        }
        /* recording list */
        if (getIntent().hasExtra(getString(R.string.recording_list))) {
            recordingList =
                    (ArrayList<RecordingList>) getIntent().getSerializableExtra(getString(R.string.recording_list));
            position = getIntent().getIntExtra(getString(R.string.position), position);
        }

        /* Audio play */
        new AudioPlayTask().execute();
        if (mHwAudioQueueManager != null) {
            updateSongName(mHwAudioQueueManager.getCurrentPlayItem());
        }
        /* notification and cancel notification */
        MyReceiver receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.PUSH_NOTIFICATION);
        filter.addAction(Constants.CANCEL_NOTIFICATION);
        PlayAudioActivity.this.registerReceiver(receiver, filter);
        /*
         * <<<<<<<Awareness Headset>>>>>>>>>>>>>>>>
        */
        String barrierReceiverAction = getApplication().getPackageName() + getString(R.string.headset_action);
        Intent intentBarrier = new Intent(barrierReceiverAction);
        // This depends on what action you want Awareness Kit to trigger when the barrier status changes.
        PendingIntent mPendingIntent =
                PendingIntent.getBroadcast(this, Constants.ZERO, intentBarrier, PendingIntent.FLAG_UPDATE_CURRENT);
        // Register a broadcast receiver to receive the broadcast sent by Awareness Kit when the barrier status changes.
        mBarrierReceiver = new HeadsetBarrierReceiver();
        registerReceiver(mBarrierReceiver, new IntentFilter(barrierReceiverAction));
        AwarenessBarrier keepingConnectedBarrier = HeadsetBarrier.keeping(HeadsetStatus.CONNECTED);
        Utils.addBarrier(this, KEEPING_BARRIER_LABEL, keepingConnectedBarrier, mPendingIntent);
    }

    @Override
    public void onClick(View v) {
        final Drawable drawablePlay = getDrawable(R.drawable.ic_play_arrow);
        final Drawable drawablePause = getDrawable(R.drawable.ic_pause);
        switch (v.getId()) {
            case R.id.playButtonImageView:
                if (binding.playButtonImageView
                        .getDrawable()
                        .getConstantState()
                        .equals(drawablePlay.getConstantState())) {
                    if (mHwAudioPlayerManager != null) {
                        mHwAudioPlayerManager.play();
                        binding.playButtonImageView.setImageDrawable(getDrawable(R.drawable.ic_pause));
                        isReallyPlaying = true;
                    }
                } else if (binding.playButtonImageView
                        .getDrawable()
                        .getConstantState()
                        .equals(drawablePause.getConstantState())) {
                    if (mHwAudioPlayerManager != null) {
                        mHwAudioPlayerManager.pause();
                        binding.playButtonImageView.setImageDrawable(getDrawable(R.drawable.ic_play_arrow));
                        isReallyPlaying = false;
                    }
                }
                break;
            case R.id.nextSongImageView:
                if (mHwAudioPlayerManager != null) {
                    mHwAudioPlayerManager.playNext();
                    isReallyPlaying = true;
                }
                break;
            case R.id.previousSongImageView:
                if (mHwAudioPlayerManager != null) {
                    mHwAudioPlayerManager.playPre();
                    isReallyPlaying = true;
                }
                break;
            case R.id.img_back_arrow:
                onBackPressed();
                break;
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.PUSH_NOTIFICATION.equals(action)) {
            }
            if (Constants.CANCEL_NOTIFICATION.equals(action)) {
                if (mHwAudioPlayerManager != null) {
                    mHwAudioPlayerManager.stop();
                }
            }
        }
    }

    private HwAudioStatusListener mPlayListener =
            new HwAudioStatusListener() {
                @Override
                public void onSongChange(HwAudioPlayItem song) {
                    updateSongName(song);
                }

                @Override
                public void onQueueChanged(List<HwAudioPlayItem> infos) {
                    if (mHwAudioPlayerManager != null && infos.size() != 0 && !isReallyPlaying) {
                        mHwAudioPlayerManager.play();
                        isReallyPlaying = true;
                        binding.playButtonImageView.setImageDrawable(getDrawable(R.drawable.ic_pause));
                    }
                }

                @Override
                public void onBufferProgress(int percent) {}

                @Override
                public void onPlayProgress(long currPos, long duration) {}

                @Override
                public void onPlayCompleted(boolean isStopped) {
                    if (mHwAudioPlayerManager != null && isStopped) {
                        mHwAudioPlayerManager.playNext();
                    }
                    isReallyPlaying = !isStopped;
                }

                @Override
                public void onPlayError(int errorCode, boolean isUserForcePlay) {
                    Toast.makeText(PlayAudioActivity.this, getString(R.string.can_not_play), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPlayStateChange(boolean isPlaying, boolean isBuffering) {
                    refresh();
                    if (isPlaying || isBuffering) {
                        binding.playButtonImageView.setImageDrawable(getDrawable(R.drawable.ic_pause));
                        isReallyPlaying = true;
                    } else {
                        binding.playButtonImageView.setImageDrawable(getDrawable(R.drawable.ic_play_arrow));
                        isReallyPlaying = false;
                        if (builder != null) builder.setOngoing(false); // probably not working as intended
                    }
                }
            };

    /**
     * [seekbar listener]<BR>
     */
    private SeekBar.OnSeekBarChangeListener mSeekListener =
            new SeekBar.OnSeekBarChangeListener() {
                private int mProgress;

                @Override
                public void onStartTrackingTouch(SeekBar bar) {
                    mFromTouch = true;
                }

                @Override
                public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                    if (!fromuser) {
                        return;
                    }
                    mProgress = progress;
                }

                @Override
                public void onStopTrackingTouch(SeekBar bar) {
                    mPosOverride = mDuration * mProgress / Constants.THOUSAND;
                    seek(mPosOverride);
                    if (!mFromTouch) {
                        refreshNow(true);
                    }
                    mFromTouch = false;
                    mPosOverride = -1;
                    if (mDuration == -1 && mTempPosition == -1) {
                        setProgressValue(Constants.ZERO);
                    }
                }
            };

    private void setProgressValue(int progressValue) {
        binding.musicSeekBar.setProgress(progressValue);
    }

    private void updateSongName(HwAudioPlayItem song) {
        if (song != null) {
            binding.txtTitle.setText(song.getAudioTitle());
            binding.include.txtHeaderTitle.setText(song.getAudioTitle());
            binding.txtAuthor.setText(song.getSinger());
        } else {
            binding.txtTitle.setText(getString(R.string.can_not_play));
        }
    }

    /**
     * seek
     *
     * @param pos pos
     */
    public void seek(long pos) {
        if (mHwAudioPlayerManager == null) {
            return;
        }
        mHwAudioPlayerManager.seekTo((int) pos);
    }

    public class AudioPlayTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HwAudioPlayerConfig hwAudioPlayerConfig = new HwAudioPlayerConfig(PlayAudioActivity.this);
            HwAudioManagerFactory.createHwAudioManager(
                    hwAudioPlayerConfig,
                    new HwAudioConfigCallBack() {
                        @Override
                        public void onSuccess(HwAudioManager hwAudioManager) {
                            try {
                                mHwAudioManager = hwAudioManager;
                                mHwAudioPlayerManager = hwAudioManager.getPlayerManager();
                                mHwAudioConfigManager = hwAudioManager.getConfigManager();
                                mHwAudioQueueManager = hwAudioManager.getQueueManager();
                                playList = getOnlinePlaylist();
                                if (playList.size() > Constants.ZERO) {
                                    play();
                                }
                                doListenersAndNotifications();
                            } catch (Exception e) {
                            }
                        }

                        @Override
                        public void onError(int i) {}
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // execution of result of Long time consuming operation
            new Handler(Looper.getMainLooper())
                    .postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    addListener(mPlayListener);
                                }
                            },
                            Constants.HUNDRED);
        }
    }

    private void play() {
        if (mHwAudioPlayerManager != null
                && mHwAudioQueueManager != null
                && mHwAudioQueueManager.getAllPlaylist() != null) {
            if (mHwAudioQueueManager.getAllPlaylist() == playItemList) {
                mHwAudioPlayerManager.play(position);
            } else {
                mHwAudioPlayerManager.playList(playList, position, Constants.ZERO);
                mHwAudioPlayerManager.setPlayMode(Constants.ZERO);
                mHwAudioQueueManager.setPlaylist(playList);
            }
        }
    }

    public List<HwAudioPlayItem> getOnlinePlaylist() {
        if (recordingList.size() == Constants.ZERO) {
            for (int i = Constants.ZERO; i < eList.size(); i++) {
                HwAudioPlayItem audioPlayItem = new HwAudioPlayItem();
                audioPlayItem.setAudioId(eList.get(i).getId().toString());
                audioPlayItem.setSinger(eList.get(i).getAuthor());
                audioPlayItem.setOnlinePath(eList.get(i).getUrl());
                audioPlayItem.setOnline(Constants.REQUEST_CODE_ONE);
                audioPlayItem.setAudioTitle(eList.get(i).getTitle());
                playItemList.add(audioPlayItem);
            }
        }
        for (int j = Constants.ZERO; j < recordingList.size(); j++) {
            HwAudioPlayItem audioPlayItem = new HwAudioPlayItem();
            audioPlayItem.setAudioId(String.valueOf(j));
            audioPlayItem.setFilePath(recordingList.get(j).getFilePath());
            audioPlayItem.setAudioTitle(recordingList.get(j).getTitle());
            audioPlayItem.setAudioType(recordingList.get(j).getTitle());
            playItemList.add(audioPlayItem);
        }
        return playItemList;
    }

    private void doListenersAndNotifications() {
        new Handler(Looper.getMainLooper())
                .post(
                        new Runnable() {
                            @Override
                            public void run() {
                                for (HwAudioStatusListener listener : mTempListeners) {
                                    try {
                                        mHwAudioManager.addPlayerStatusListener(listener);
                                    } catch (RemoteException e) {
                                    }
                                }
                                mHwAudioConfigManager.setSaveQueue(true);
                                mHwAudioConfigManager.setNotificationFactory(
                                        new INotificationFactory() {
                                            @Override
                                            public Notification createNotification(
                                                    NotificationConfig notificationConfig) {
                                                builder = new NotificationCompat.Builder(getApplication(), null);
                                                RemoteViews remoteViews =
                                                        new RemoteViews(
                                                                getApplication().getPackageName(),
                                                                R.layout.notification_player);
                                                builder.setContent(remoteViews);
                                                builder.setSmallIcon(R.drawable.ic_share);
                                                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                                                builder.setCustomBigContentView(remoteViews);
                                                NotificationUtils.addChannel(
                                                        getApplication(),
                                                        NotificationUtils.NOTIFY_CHANNEL_ID_PLAY,
                                                        builder);
                                                boolean isQueueEmpty = mHwAudioManager.getQueueManager().isQueueEmpty();
                                                boolean isPlaying =
                                                        mHwAudioManager.getPlayerManager().isPlaying() && !isQueueEmpty;
                                                remoteViews.setImageViewResource(
                                                        R.id.image_toggle,
                                                        isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
                                                HwAudioPlayItem playItem =
                                                        mHwAudioManager.getQueueManager().getCurrentPlayItem();
                                                remoteViews.setTextViewText(R.id.text_song, playItem.getAudioTitle());
                                                remoteViews.setTextViewText(R.id.text_artist, playItem.getSinger());
                                                remoteViews.setImageViewResource(
                                                        R.id.image_last, R.drawable.ic_skip_previous);
                                                remoteViews.setImageViewResource(
                                                        R.id.image_next, R.drawable.ic_skip_next);
                                                remoteViews.setOnClickPendingIntent(
                                                        R.id.image_last, notificationConfig.getPrePendingIntent());
                                                remoteViews.setOnClickPendingIntent(
                                                        R.id.image_toggle, notificationConfig.getPlayPendingIntent());
                                                remoteViews.setOnClickPendingIntent(
                                                        R.id.image_next, notificationConfig.getNextPendingIntent());
                                                remoteViews.setOnClickPendingIntent(
                                                        R.id.image_close, getCancelPendingIntent());
                                                remoteViews.setOnClickPendingIntent(
                                                        R.id.layout_content, getMainIntent());
                                                return builder.build();
                                            }
                                        });
                            }
                        });
    }

    private PendingIntent getCancelPendingIntent() {
        Intent closeIntent = new Intent(Constants.CANCEL_NOTIFICATION);
        closeIntent.setPackage(getApplication().getPackageName());
        return PendingIntent.getBroadcast(
                getApplication(), Constants.REQUEST_CODE_TWO, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getMainIntent() {
        Intent intent = new Intent(getString(R.string.str_intent));
        intent.addCategory(getString(R.string.str_cat));
        intent.setClass(getApplication().getBaseContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return PendingIntent.getActivity(getApplication(), 0, intent, 0);
    }

    public void addListener(HwAudioStatusListener listener) {
        if (mHwAudioManager != null) {
            try {
                mHwAudioManager.addPlayerStatusListener(listener);
            } catch (RemoteException e) {
            }
        } else {
            mTempListeners.add(listener);
        }
    }

    /**
     * getBufferPercentage
     *
     * @return buffer percent
     */
    public int getBufferPercentage() {
        if (mHwAudioPlayerManager == null) {
            return Constants.ZERO;
        }
        return mHwAudioPlayerManager.getBufferPercent();
    }

    /**
     * getPosition
     *
     * @return now playing position
     */
    public long getPosition() {
        if (mHwAudioPlayerManager == null) {
            return Constants.ZERO;
        }
        return mHwAudioPlayerManager.getOffsetTime();
    }

    /**
     * getDuration
     *
     * @return duration
     */
    public long getDuration() {
        if (mHwAudioPlayerManager == null) {
            return Constants.ZERO;
        }
        return mHwAudioPlayerManager.getDuration();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBarrierReceiver != null) {
            unregisterReceiver(mBarrierReceiver);
        }
    }

    public void refresh() {
        if (isQueueEmpty()) {
            return;
        }
        refreshNow(true);
    }

    private void refreshNow(boolean force) {
        new RefreshTask().execute(force);
    }

    /**
     * [RefreshTask]<BR>
     */
    @SuppressLint("StaticFieldLeak")
    private class RefreshTask extends AsyncTask<Boolean, Void, Boolean> {
        String curPosString;

        String durationString;

        int progressValue;

        @Override
        protected Boolean doInBackground(Boolean... params) {
            boolean force = params[Constants.ZERO];
            int percent = getBufferPercentage();
            long position = mPosOverride < Constants.ZERO ? getPosition() : mPosOverride;
            long duration = getDuration();
            if (!force && mTempPosition == position && mDuration == duration && percent == mCurrentBufferPercent) {
                return false;
            }
            mTempPosition = position;
            mDuration = duration;
            mCurrentBufferPercent = percent;
            if (duration > Constants.ZERO) {
                if (position > (duration - Constants.FIVE_HUNDRED)) {
                    position = duration;
                }
                double currentTime = ((double) position) / Constants.THOUSAND;
                long timePassed =
                        (currentTime - (long) currentTime) > 0.8 ? (long) currentTime + Constants.REQUEST_CODE_ONE : (long) currentTime;
                long totalTime = duration / Constants.THOUSAND;
                if (timePassed > totalTime) {
                    timePassed = totalTime;
                }
                curPosString = Utils.localeString(PlayAudioActivity.this, timePassed);
                durationString = Utils.localeString(PlayAudioActivity.this, totalTime);
                progressValue =
                        Constants.ZERO == totalTime
                                ? Constants.ZERO
                                : (int) (Constants.THOUSAND * timePassed / totalTime);
            } else {
                curPosString = Utils.localeString(getString(R.string.zero_duration));
                durationString = curPosString;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            queueNextRefresh(result != null && result ? doFresh() : Constants.FIVE_HUNDRED);
        }

        private long doFresh() {
            mHandler.removeCallbacks(mClearTask);
            long remaining = Constants.THOUSAND - (mTempPosition % Constants.THOUSAND);
            binding.totalDurationTextView.setText(durationString);
            binding.progressTextView.setText(curPosString);
            setProgressValue(progressValue);
            binding.musicSeekBar.setSecondaryProgress(mCurrentBufferPercent * Constants.TEN);
            return remaining;
        }
    }

    private void queueNextRefresh(long delay) {
        if (isPlaying()) {
            Message msg = mHandler.obtainMessage(Constants.REQUEST_CODE_ONE);
            mHandler.removeMessages(Constants.REQUEST_CODE_ONE);
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    public boolean isQueueEmpty() {
        if (mHwAudioQueueManager != null && mHwAudioQueueManager.isQueueEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(Constants.REQUEST_CODE_ONE);
    }

    private Runnable mClearTask =
            new Runnable() {
                @Override
                public void run() {
                    binding.totalDurationTextView.setText(Utils.localeString(getString(R.string.zero_duration)));
                }
            };

    /**
     * check is playing
     *
     * @return playing
     */
    public boolean isPlaying() {
        return mHwAudioPlayerManager != null && mHwAudioPlayerManager.isPlaying();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler =
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (Constants.REQUEST_CODE_ONE == msg.what) {
                        refreshNow(false);
                    }
                }
            };
}
