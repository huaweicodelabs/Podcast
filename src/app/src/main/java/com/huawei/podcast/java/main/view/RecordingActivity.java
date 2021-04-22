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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.multimedia.audiokit.config.ResultCode;
import com.huawei.multimedia.audiokit.interfaces.HwAudioKit;
import com.huawei.multimedia.audiokit.interfaces.IAudioKitCallback;
import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.RecordingList;
import com.huawei.podcast.java.main.adapter.RecordingsAdapter;
import com.huawei.podcast.java.utils.AudioTrackThread;
import com.huawei.podcast.java.utils.Constants;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordingActivity extends AppCompatActivity implements View.OnClickListener, IAudioKitCallback {
    HwAudioKit mHwAudioKit;
    private final boolean mSupportLowLatencyRecording = true;
    private boolean mIsLowLatencyRecording = false;
    String mResultType = Constants.EMPTY_STRING;
    private boolean mIsRecording = false;
    private final Object mRecordingLock = new Object();
    /**
     * media recorder
     */
    private MediaRecorder mMediaRecorder = null;
    /**
     * record file
     */
    private File mRecordFile;

    private AudioTrackThread mAudioTrackThread;
    String filePath;
    String audioFile;
    CountDownTimer countDownTimer;
    int second = Constants.MINUS_ONE;
    int minute = Constants.ZERO;
    int hour = Constants.ZERO;
    ArrayList<RecordingList> audioArrayList;
    RecordingsAdapter recordingsAdapter;
    private static final String TAG = Constants.RECORDING;
    LinearLayout lin_record, lin_stop, lin_list;
    TextView txt_title;
    TextView txt_no_recordings, txt_recording;
    RecyclerView rv_recording_list;
    View include_recording, include_recording_list;

    private static final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        initViews();
    }

    private void initViews() {
        txt_title = (findViewById(R.id.txt_title));
        lin_record = (findViewById(R.id.lin_record));
        lin_stop = (findViewById(R.id.lin_stop));
        lin_list = (findViewById(R.id.lin_list));
        txt_recording = findViewById(R.id.txt_recording);
        txt_no_recordings = findViewById(R.id.txt_no_recordings);
        rv_recording_list = (findViewById(R.id.rv_recording_list));
        include_recording = (findViewById(R.id.include_recording));
        include_recording_list = findViewById(R.id.include_recording_list);
        ImageView img_back_arrow = (findViewById(R.id.img_back_arrow));
        txt_title.setText(getString(R.string.recording));
        lin_record.setOnClickListener(this);
        img_back_arrow.setOnClickListener(this);
        lin_stop.setOnClickListener(this);
        lin_list.setOnClickListener(this);
        lin_stop.isEnabled();
        /* recording list */
        audioArrayList = new ArrayList<>();
        rv_recording_list.setItemAnimator(new DefaultItemAnimator());
        createEngine();
        mHwAudioKit = new HwAudioKit(this, this);
        mHwAudioKit.initialize();
    }

    private boolean hasPermission() {
        for (String permission : PERMISSIONS) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, Constants.INTENT_FLAG);
    }

    private void startRecord() {
        if (!hasPermission()) {
            startRequestPermission();
            return;
        }
        showTimer();
        synchronized (mRecordingLock) {
            if (mSupportLowLatencyRecording) {
                startLowLatencyRecord();
                startAudioTrackThread();
                return;
            }
            // if already created, just return
            if (mMediaRecorder != null && mIsRecording) {
                return;
            }

            try {
                mMediaRecorder = new MediaRecorder();
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecordFile = new File(getExternalCacheDir(), Constants.RECORD_FILE_NAME);
                if (!mRecordFile.exists()) {
                    mRecordFile.mkdirs();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
                String date = dateFormat.format(new Date());
                audioFile = getString(R.string.rec) + date;
                filePath = mRecordFile.getCanonicalPath() + File.separator + audioFile;
                mMediaRecorder.setOutputFile(filePath);
                mMediaRecorder.prepare();
                mMediaRecorder.start();
                startAudioTrackThread();
                mIsRecording = true;
            } catch (IOException | IllegalStateException e) {
            }
        }
    }

    private void startLowLatencyRecord() {
        if (mIsLowLatencyRecording) {
            return;
        }
        try {
            mRecordFile = new File(getExternalCacheDir(), Constants.RECORD_FILE_NAME);
            if (!mRecordFile.exists()) {
                mRecordFile.mkdirs();
            }
            DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            String date = dateFormat.format(new Date());
            audioFile = getString(R.string.rec) + date;
            filePath = mRecordFile.getCanonicalPath() + File.separator + audioFile;
            createAudioRecorder(filePath);
            startRecording();
            mIsLowLatencyRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAudioTrackThread() {
        try {
            if (mAudioTrackThread != null) {
                mAudioTrackThread.destroy();
                mAudioTrackThread = null;
            }
            mAudioTrackThread = new AudioTrackThread();
            mAudioTrackThread.start();

        } catch (IllegalThreadStateException e) {
        }
    }

    private void stopRecord() {
        if (countDownTimer != null) countDownTimer.cancel();
        txt_recording.setText(getString(R.string.three_zero_duration));
        synchronized (mRecordingLock) {
            if (mSupportLowLatencyRecording) {
                stopRecording();
                mIsLowLatencyRecording = false;
                return;
            }

            if (mMediaRecorder != null && mIsRecording) {
                try {
                    stopAudioTrackThread();
                    mMediaRecorder.pause();
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                    mIsRecording = false;
                } catch (IllegalStateException e) {
                }
            } else {
            }
        }
    }

    private void stopAudioTrackThread() {
        try {
            if (mAudioTrackThread != null) {
                mAudioTrackThread.destroy();
                mAudioTrackThread = null;
            }
        } catch (IllegalThreadStateException e) {
        }
    }

    private void showTimer() {
        countDownTimer =
                new CountDownTimer(Long.MAX_VALUE, Constants.THOUSAND) {
                    public void onTick(long millisUntilFinished) {
                        second++;
                        txt_recording.setText(recorderTime());
                    }

                    public void onFinish() {}
                }.start();
    }

    private String recorderTime() {
        if (second == Constants.SIXTY) {
            minute++;
            second = Constants.ZERO;
        }
        if (minute == Constants.SIXTY) {
            hour++;
            minute = Constants.ZERO;
        }
        return String.format(Constants.FORMAT, hour, minute, second);
    }

    /* * Native methods, implemented in jni folder */
    public static native void createEngine();

    // public static native void createBufferQueueAudioPlayer(int sampleRate, int samplesPerBuf);
    public static native boolean createUriAudioPlayer(String uri);

    // public static native void setPlayingUriAudioPlayer(boolean isPlaying);
    public static native boolean createAudioRecorder(String path);

    public static native void startRecording();

    public static native void stopRecording();

    public static native void shutdown();

    /* * Load jni .so on initialization */
    static {
        System.loadLibrary(Constants.LIB_NAME);
    }

    @Override
    public void onResult(int resultType) {
        mResultType = Constants.EMPTY_STRING;
        switch (resultType) {
            case ResultCode.VENDOR_NOT_SUPPORTED:
                mResultType = getResources().getString(R.string.notInstallAudioKitService);
                break;
            case ResultCode.AUDIO_KIT_SERVICE_DISCONNECTED:
                mResultType = getResources().getString(R.string.audioKitServiceDisconnect);
                break;
            case ResultCode.AUDIO_KIT_SERVICE_DIED:
                mResultType = getResources().getString(R.string.audioKitServiceDied);
                break;
            default:
                break;
        }
        mResultType = mResultType + resultType;
        Toast.makeText(this, mResultType, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lin_stop:
                stopRecord();
                lin_stop.setEnabled(false);
                lin_record.setEnabled(true);
                break;
            case R.id.img_back_arrow:
                onBackPressed();
                break;
            case R.id.lin_list:
                getAudioRecordings();
                include_recording.setVisibility(View.GONE);
                include_recording_list.setVisibility(View.VISIBLE);
                break;
            case R.id.lin_record:
                second = Constants.MINUS_ONE;
                minute = Constants.ZERO;
                hour = Constants.ZERO;
                lin_stop.setEnabled(true);
                startRecord();
                lin_record.setEnabled(false);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    private void getAudioRecordings() {
        txt_title.setText(getString(R.string.txt_record));
        File folder = new File(getExternalCacheDir() + Constants.RECORD_FILE_PATH);
        if (folder.listFiles() != null) {
            for (File i : folder.listFiles()) {
                RecordingList recordingList = new RecordingList();
                recordingList.setTitle(i.getName());
                try {
                    recordingList.setFilePath(i.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                recordingList.setPlay(false);
                audioArrayList.add(recordingList);
            }
            txt_no_recordings.setVisibility(View.GONE);
            rv_recording_list.setVisibility(View.VISIBLE);
        } else {
            txt_no_recordings.setVisibility(View.VISIBLE);
            rv_recording_list.setVisibility(View.GONE);
        }
        if (audioArrayList.size() > Constants.ZERO) {
            recordingsAdapter = new RecordingsAdapter(audioArrayList, this);
            rv_recording_list.setAdapter(recordingsAdapter);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mHwAudioKit != null) {
            mHwAudioKit.destroy();
        }
        if (mSupportLowLatencyRecording) {
            shutdown();
        }
    }
}
