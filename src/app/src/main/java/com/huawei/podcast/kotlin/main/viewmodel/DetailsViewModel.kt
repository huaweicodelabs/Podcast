
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
package com.huawei.podcast.kotlin.main.viewmodel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.huawei.agconnect.applinking.AppLinking
import com.huawei.agconnect.applinking.ShortAppLinking
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.analytics.HiAnalyticsTools
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.utils.Constants

class DetailsViewModel : ViewModel() {

    private var instance: HiAnalyticsInstance? = null

    fun onShareClick(view: View) {
        instance = HiAnalytics.getInstance(view.context)
        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog()
        val builder = AppLinking.Builder().setUriPrefix(Constants.domainUriPrefix)
            .setDeepLink(Uri.parse(Constants.deepLink))
            .setAndroidLinkInfo(AppLinking.AndroidLinkInfo.Builder().build())

        builder.buildShortAppLinking()
            .addOnSuccessListener { shortAppLinking: ShortAppLinking ->
                shareLink(shortAppLinking.shortUrl.toString(), view)
            }.addOnFailureListener {
                Toast.makeText(
                    view.context,
                    view.context.getString(R.string.app_link),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Sharing App link
     */
    private fun shareLink(agcLink: String?, view: View) {
        if (agcLink != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = Constants.type
                putExtra(Intent.EXTRA_TEXT, agcLink)
            }
            view.context.startActivity(intent)
            val bundle = Bundle()
            bundle.putString(view.context.getString(R.string.share), view.context.getString(R.string.count))
            instance?.onEvent(view.context.getString(R.string.count), bundle)
        }
    }
}
