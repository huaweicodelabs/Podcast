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
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huawei.podcast.R;
import com.huawei.podcast.java.utils.Constants;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;

public class AddToPodCastActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initViews();
    }

    private void initViews() {
        boolean isAllowed = checkReadPermissionBoolean();
        TextView txt_title = (findViewById(R.id.txt_title));
        ImageView img_back_arrow = findViewById(R.id.img_back_arrow);
        ImageView img_record = findViewById(R.id.img_record);
        img_back_arrow.setOnClickListener(v -> onBackPressed());
        txt_title.setText(getString(R.string.recording));
        img_record.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isAllowed) {
                            Intent intent = new Intent(AddToPodCastActivity.this, RecordingActivity.class);
                            startActivity(intent);
                        } else {
                            requestPermission();
                        }
                    }
                });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                AddToPodCastActivity.this,
                new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK},
                Constants.REQUEST_CODE_ONE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_CODE_ONE) { // If request is cancelled, the result arrays are empty.
            if (grantResults.length > Constants.ZERO && grantResults[Constants.ZERO] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(this, RecordingActivity.class);
                startActivity(i);
                finish();
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(
                                AddToPodCastActivity.this,
                                getString(R.string.permission_denied),
                                Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private boolean checkReadPermissionBoolean() {
        int result = ContextCompat.checkSelfPermission(AddToPodCastActivity.this, Manifest.permission.RECORD_AUDIO);
        int result1 = ContextCompat.checkSelfPermission(AddToPodCastActivity.this, Manifest.permission.WAKE_LOCK);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
}
