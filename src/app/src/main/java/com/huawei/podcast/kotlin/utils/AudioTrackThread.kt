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
package com.huawei.podcast.kotlin.utils

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

/**
 * function description
 */
class AudioTrackThread : Thread() {
    private var mIsRunning = true
    private var mAudioTrack: AudioTrack? = null

    override fun run() {
        priority = MAX_PRIORITY
        val buffSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        try {
            mAudioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, buffSize, AudioTrack.MODE_STREAM
            )
        } catch (e: IllegalThreadStateException) {
        }
        if (mAudioTrack == null) {
            return
        }
        audioTrackWrite(buffSize)
        mAudioTrack?.release()
    }

    private fun audioTrackWrite(buffSize: Int) {
        val samples = ShortArray(buffSize)
        if (mAudioTrack?.state != AudioTrack.STATE_INITIALIZED) {
        } else {
            mAudioTrack?.play()
            try {
                while (mIsRunning) {
                    mAudioTrack?.write(samples, DEFAULT_INT_TYPE, buffSize)
                }
            } catch (e: IllegalThreadStateException) {
            }
            mAudioTrack?.stop()
        }
    }

    /**
     * thread destroy
     */
    override fun destroy() {
        if (mAudioTrack != null) {
            mAudioTrack?.stop()
        }
        mIsRunning = false
    }

    companion object {
        private const val DEFAULT_INT_TYPE = 0
        private const val SAMPLE_RATE = 44100
    }
}
