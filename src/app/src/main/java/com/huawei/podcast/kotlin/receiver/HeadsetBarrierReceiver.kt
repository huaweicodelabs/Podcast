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
package com.huawei.podcast.kotlin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.huawei.hms.kit.awareness.barrier.BarrierStatus
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.utils.Constants

class HeadsetBarrierReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val barrierStatus = BarrierStatus.extract(intent)
        val label = barrierStatus.barrierLabel
        val barrierPresentStatus = barrierStatus.presentStatus
        when (label) {
            KEEPING_BARRIER_LABEL -> when (barrierPresentStatus) {
                BarrierStatus.TRUE -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_connected), Toast.LENGTH_LONG).show()
                }
                BarrierStatus.FALSE -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_disConnected), Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_unknown), Toast.LENGTH_LONG).show()
                }
            }
            CONNECTING_BARRIER_LABEL -> when (barrierPresentStatus) {
                BarrierStatus.TRUE -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_connecting), Toast.LENGTH_LONG).show()
                }
                BarrierStatus.FALSE -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_not_connecting), Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_unknown), Toast.LENGTH_LONG).show()
                }
            }
            DISCONNECTING_BARRIER_LABEL -> when (barrierPresentStatus) {
                BarrierStatus.TRUE -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_disconnecting), Toast.LENGTH_LONG).show()
                }
                BarrierStatus.FALSE -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_not_disconnecting), Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, context.resources.getString(R.string.headset_unknown), Toast.LENGTH_LONG).show()
                }
            }
            else -> {
            }
        }
    }

    companion object {
        private const val KEEPING_BARRIER_LABEL = Constants.KEEPING_BARRIER_LABEL
        private const val CONNECTING_BARRIER_LABEL = Constants.CONNECTING_BARRIER_LABEL
        private const val DISCONNECTING_BARRIER_LABEL = Constants.DISCONNECTING_BARRIER_LABEL
    }
}
