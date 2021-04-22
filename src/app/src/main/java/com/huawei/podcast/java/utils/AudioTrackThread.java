/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.podcast.java.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * 	function description
 */
public class AudioTrackThread extends Thread {

    private static final int DEFAULT_INT_TYPE = Constants.ZERO;

    private static final int SAMPLE_RATE = Constants.SAMPLE_RATE;

    private boolean mIsRunning = true;

    private AudioTrack mAudioTrack = null;

    @Override
    public void run() {
        setPriority(Thread.MAX_PRIORITY);
        int buffsize =
                AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        try {
            mAudioTrack =
                    new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            buffsize,
                            AudioTrack.MODE_STREAM);
        } catch (IllegalThreadStateException e) {
        }
        if (mAudioTrack == null) {
            return;
        }
        audioTrackWrite(buffsize);
        mAudioTrack.release();
    }

    private void audioTrackWrite(int buffsize) {
        short[] samples = new short[buffsize];
        if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
        } else {
            mAudioTrack.play();
            try {
                while (mIsRunning) {
                    mAudioTrack.write(samples, DEFAULT_INT_TYPE, buffsize);
                }
            } catch (IllegalThreadStateException e) {
            }
            mAudioTrack.stop();
        }
    }

    /**
     * thread destroy
     */
    public void destroy() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
        }
        mIsRunning = false;
    }
}
