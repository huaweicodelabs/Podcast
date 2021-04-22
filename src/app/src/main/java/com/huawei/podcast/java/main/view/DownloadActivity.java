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

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.main.adapter.DownLoadAdapter;
import com.huawei.podcast.java.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class DownloadActivity extends AppCompatActivity {
    ArrayList<String> fileList = new ArrayList<>();
    DownLoadAdapter downLoadAdapter;
    TextView txt_no_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        setUpUI();
    }

    private void setUpUI() {
        TextView txt_title = (findViewById(R.id.txt_title));
        txt_no_data = (findViewById(R.id.txt_no_data));
        txt_title.setText(getString(R.string.str_downloads));
        txt_no_data.setText(getString(R.string.no_downloads));
        ImageView img_back_arrow = findViewById(R.id.img_back_arrow);
        RecyclerView recyclerView = findViewById(R.id.rv_fav);
        img_back_arrow.setOnClickListener(v -> onBackPressed());
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
        if (folder.listFiles() != null && Objects.requireNonNull(folder.listFiles()).length != Constants.ZERO) {
            for (File val : Objects.requireNonNull(folder.listFiles())) {
                fileList.add(val.getName());
            }
            downLoadAdapter = new DownLoadAdapter(this, fileList);
            recyclerView.setAdapter(downLoadAdapter);
            txt_no_data.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            txt_no_data.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    public void updateText() {
        txt_no_data.setVisibility(View.VISIBLE);
    }
}
