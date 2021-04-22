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

import android.app.Activity
import android.app.Dialog
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ProgressBar
import com.huawei.podcast.R

object ProgressDialog {
    // progress bar handling
    fun showProgress(activity: Activity): Dialog {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(0)
        )
        dialog.setContentView(R.layout.dialog_progress)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.indeterminateDrawable.setColorFilter(activity.resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        dialog.setCancelable(true)
        dialog.show()
        return dialog
    }
}
