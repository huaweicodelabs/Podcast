/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.huawei.podcast.kotlin.utils

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.huawei.podcast.R

object NotificationUtils {
    /**
     * The id of the channel.
     */
    const val NOTIFY_CHANNEL_ID_PLAY = Constants.NOTIFICATION_PLAY

    /**
     * add channel
     *
     * @param channelId channelId
     * @param builder Notification builder
     */
    @JvmStatic
    @TargetApi(Build.VERSION_CODES.O)
    fun addChannel(
        application: Application,
        channelId: String,
        builder: NotificationCompat.Builder
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        createNotificationChannel(application, channelId, builder)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        application: Application,
        channelId: String,
        builder: NotificationCompat.Builder
    ) {
        val notificationChannel = NotificationChannel(
            channelId,
            Strings.get(R.string.str_play),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.enableVibration(false)
        notificationChannel.setSound(null, null)
        val notificationManager =
            application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        builder.setGroup(channelId)
        builder.setChannelId(channelId)
    }
}
