
/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.kotlin.main.view

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.multimedia.audiokit.config.ResultCode
import com.huawei.multimedia.audiokit.interfaces.HwAudioKit
import com.huawei.multimedia.audiokit.interfaces.IAudioKitCallback
import com.huawei.podcast.R
import com.huawei.podcast.java.utils.Constants.DATE_FORMAT
import com.huawei.podcast.kotlin.data.model.RecordingList
import com.huawei.podcast.kotlin.main.adapter.RecordingsAdapter
import com.huawei.podcast.kotlin.utils.AudioTrackThread
import com.huawei.podcast.kotlin.utils.Constants
import com.huawei.podcast.kotlin.utils.Constants.INTENT_FLAG
import com.huawei.podcast.kotlin.utils.Constants.SIXTY
import com.huawei.podcast.kotlin.utils.Constants.THOUSAND
import kotlinx.android.synthetic.main.activity_recording.*
import kotlinx.android.synthetic.main.include_header.*
import kotlinx.android.synthetic.main.include_recording.*
import kotlinx.android.synthetic.main.include_recording_list.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecordingActivity : AppCompatActivity(), View.OnClickListener, IAudioKitCallback {
    private lateinit var mHwAudioKit: HwAudioKit
    private val mSupportLowLatencyRecording = true
    private var mIsLowLatencyRecording = false
    private var mResultType = Constants.EMPTY_STRING
    private var mIsRecording = false

    // protects recording
    private val mRecordingLock = Any()

    /**
     * media recorder
     */
    private lateinit var mMediaRecorder: MediaRecorder

    /**
     * record file
     */
    private lateinit var mRecordFile: File
    private var mAudioTrackThread: AudioTrackThread? = null

    private lateinit var filePath: String
    private lateinit var audioFile: String
    private var countDownTimer: CountDownTimer? = null
    private var second = -1
    private var minute = Constants.ZERO
    private var hour = Constants.ZERO
    private lateinit var audioArrayList: ArrayList<RecordingList>
    private lateinit var adapter: RecordingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)
        initViews()
    }

    private fun initViews() {
        /*recording*/
        txt_title.text = getString(R.string.recording)
        lin_record.setOnClickListener(this)
        img_back_arrow.setOnClickListener(this)
        lin_stop.setOnClickListener(this)
        lin_list.setOnClickListener(this)
        lin_stop.isEnabled = false
        /*recording list*/
        audioArrayList = ArrayList()
        rv_recording_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_recording_list.itemAnimator = DefaultItemAnimator()
        // initialize native audio system
        createEngine()

        mHwAudioKit = HwAudioKit(this, this)
        mHwAudioKit.initialize()
    }

    private fun hasPermission(): Boolean {
        for (permission in PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun startRequestPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, INTENT_FLAG)
    }

    private fun startRecord() {
        if (!hasPermission()) {
            startRequestPermission()
            return
        }
        showTimer()
        synchronized(mRecordingLock) {
            if (mSupportLowLatencyRecording) {
                startLowLatencyRecord()
                startAudioTrackThread()
                return
            }

            // if already created, just return
            if (mMediaRecorder != null && mIsRecording) {
                Log.i(TAG, getString(R.string.str_create_record))
                return
            }
            try {
                mMediaRecorder = MediaRecorder()
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mRecordFile = File(externalCacheDir, RECORD_FILE_NAME)
                if (!mRecordFile.exists()) {
                    mRecordFile.mkdirs()
                }
                val dateFormat = SimpleDateFormat(DATE_FORMAT)
                val date = dateFormat.format(Date())
                audioFile = getString(R.string.rec) + date
                filePath = mRecordFile.absolutePath + File.separator + audioFile
                mMediaRecorder.setOutputFile(filePath)
                mMediaRecorder.prepare()
                mMediaRecorder.start()
                startAudioTrackThread()
                mIsRecording = true
            } catch (e: IOException) {
                Log.e(TAG, getString(R.string.start_record))
            } catch (e: IllegalStateException) {
                Log.e(TAG, getString(R.string.start_record))
            }
        }
    }

    private fun startLowLatencyRecord() {
        if (mIsLowLatencyRecording) {
            Log.i(TAG, getString(R.string.al_recording))
            return
        }
        try {
            mRecordFile = File(externalCacheDir, RECORD_FILE_NAME)
            if (!mRecordFile.exists()) {
                mRecordFile.mkdirs()
            }
            val dateFormat = SimpleDateFormat(DATE_FORMAT)
            val date = dateFormat.format(Date())
            audioFile = getString(R.string.rec) + date
            filePath = mRecordFile.absolutePath + File.separator + audioFile
            createAudioRecorder(filePath)
            startRecording()
            mIsLowLatencyRecording = true
        } catch (e: Exception) {
            Log.e(TAG, getString(R.string.start_low_exception))
        }
    }

    private fun startAudioTrackThread() {
        try {
            if (mAudioTrackThread != null) {
                mAudioTrackThread?.destroy()
            }
            mAudioTrackThread = AudioTrackThread()
            mAudioTrackThread?.start()
            Log.i(TAG, getString(R.string.thread_start))
        } catch (e: IllegalThreadStateException) {
            Log.e(TAG, getString(R.string.thread_ill))
        }
    }

    private fun stopRecord() {
        Log.i(TAG, getString(R.string.stop))
        countDownTimer?.cancel()
        txt_recording.text = getString(R.string.three_zero_duration)
        synchronized(mRecordingLock) {
            if (mSupportLowLatencyRecording) {
                Log.i(TAG, getString(R.string.str_stop))
                stopRecording()
                mIsLowLatencyRecording = false
                return
            }
            if (mMediaRecorder != null && mIsRecording) {
                try {
                    stopAudioTrackThread()
                    mMediaRecorder.pause()
                    mMediaRecorder.stop()
                    mMediaRecorder.release()
                    mIsRecording = false
                } catch (e: IllegalStateException) {
                }
            } else {
            }
        }
    }

    private fun stopAudioTrackThread() {
        try {
            if (mAudioTrackThread != null) {
                mAudioTrackThread?.destroy()
            }
        } catch (e: IllegalThreadStateException) {
        }
    }

    // display recording time
    private fun showTimer() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, THOUSAND.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                second++
                txt_recording!!.text = recorderTime()
            }

            override fun onFinish() {}
        }
        countDownTimer?.start()
    }

    // recorder time
    fun recorderTime(): String {
        if (second == SIXTY) {
            minute++
            second = Constants.ZERO
        }
        if (minute == SIXTY) {
            hour++
            minute = Constants.ZERO
        }
        return String.format(Constants.FORMAT, hour, minute, second)
    }

    companion object {
        private var TAG = RecordingActivity::class.java.simpleName
        private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK)



        /**
         * file name
         */
        private const val RECORD_FILE_NAME = Constants.RECORD_FILE_NAME

        /** Native methods, implemented in jni folder  */
        external fun createEngine()
        external fun createAudioRecorder(path: String?): Boolean
        external fun startRecording()
        external fun stopRecording()
        external fun shutdown()

        /** Load jni .so on initialization  */
        init {
            System.loadLibrary("native-audio-jni")
        }
    }

    override fun onResult(resultType: Int) {
        mResultType = ""
        when (resultType) {
            ResultCode.VENDOR_NOT_SUPPORTED -> mResultType = resources.getString(R.string.notInstallAudioKitService)
            ResultCode.AUDIO_KIT_SERVICE_DISCONNECTED -> mResultType = resources.getString(R.string.audioKitServiceDisconnect)
            ResultCode.AUDIO_KIT_SERVICE_DIED -> mResultType = resources.getString(R.string.audioKitServiceDied)
            else -> {
            }
        }
        mResultType += resultType
        Toast.makeText(this, mResultType, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lin_stop ->
                {
                    stopRecord()
                    lin_stop.isEnabled = false
                    lin_record.isEnabled = true
                }
            R.id.img_back_arrow -> onBackPressed()
            R.id.lin_list -> {
                getAudioRecordings()
                include_recording.visibility = View.GONE
                include_recording_list.visibility = View.VISIBLE
            }
            R.id.lin_record -> {
                second = -1
                minute = 0
                hour = 0
                lin_stop.isEnabled = true
                startRecord()
                lin_record.isEnabled = false
            }
        }
    }

    private fun getAudioRecordings() {
        txt_title.text = getString(R.string.txt_record)
        val externalStorageDirectory = externalCacheDir
        val folder = File(externalStorageDirectory?.absolutePath + "/$RECORD_FILE_NAME")
        if (folder.listFiles() != null) {
            for (i in folder.listFiles().indices) {
                val recordingList = RecordingList(
                    folder.listFiles()[i].name,
                    folder.listFiles()[i].absolutePath,
                    false
                )
                audioArrayList.add(recordingList)
            }
            txt_no_recordings.visibility = View.GONE
            rv_recording_list.visibility = View.VISIBLE
        } else {
            txt_no_recordings.visibility = View.VISIBLE
            rv_recording_list.visibility = View.GONE
        }
        if (audioArrayList.size > Constants.ZERO) {
            adapter = RecordingsAdapter(this, audioArrayList)
            rv_recording_list.adapter = adapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mHwAudioKit != null) {
            mHwAudioKit.destroy()
        }
        if (mSupportLowLatencyRecording) {
            shutdown()
        }
    }
}
