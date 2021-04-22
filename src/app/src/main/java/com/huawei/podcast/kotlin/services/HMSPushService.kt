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
package com.huawei.podcast.kotlin.services

import android.content.Intent
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.utils.Constants
import java.lang.Exception

class HMSPushService : HmsMessageService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToDisplay(token)
    }

    override fun onTokenError(e: Exception) {
        super.onTokenError(e)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
        }
        if (remoteMessage.notification != null) {
        }
    }

    override fun onMessageSent(s: String) {}

    private fun sendTokenToDisplay(token: String) {
        val intent = Intent(TOKEN)
        intent.putExtra(getString(R.string.token), token)
        sendBroadcast(intent)
    }

    companion object {

        private const val TOKEN = Constants.TOKEN
    }
}
