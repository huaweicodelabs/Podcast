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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.utils.Constants.INTENT_FLAG
import kotlinx.android.synthetic.main.activity_record.*
import kotlinx.android.synthetic.main.include_header.*

class AddToPodCastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        initViews()
    }

    private fun initViews() {
        checkPermissions()
        img_back_arrow.setOnClickListener {
            onBackPressed()
        }
        txt_title.text = getString(R.string.recording)
        img_record.setOnClickListener {
            val i = Intent(this@AddToPodCastActivity, RecordingActivity::class.java)
            startActivity(i)
        }
    }

    private fun checkPermissions() {
        if (!hasPermission()) {
            startRequestPermission()
        }
    }

    private fun hasPermission(): Boolean {
        for (permission in PERMISSIONS) {
            val result: Int = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun startRequestPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, INTENT_FLAG)
    }

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK)
    }
}
