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
package com.huawei.podcast.java.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ProgressBar;

import com.huawei.podcast.R;

public class ProgressDialog {
    // progress bar handling
    public static Dialog showProgress(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.dialog_progress);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        progressBar
                .getIndeterminateDrawable()
                .setColorFilter(activity.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }
}
