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

import android.app.PendingIntent
import android.content.Context
import android.widget.Toast
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest
import com.huawei.podcast.R

object Utils {
    @JvmStatic
    fun addBarrier(
        context: Context,
        label: String?,
        barrier: AwarenessBarrier?,
        pendingIntent: PendingIntent?
    ) {
        val builder = BarrierUpdateRequest.Builder()
        // When the status of the registered barrier changes, pendingIntent is triggered.
        // label is used to uniquely identify the barrier. You can query a barrier by label and delete it.
        val request =
            builder.addBarrier(label!!, barrier!!, pendingIntent!!).build()
        Awareness.getBarrierClient(context).updateBarriers(request)
            .addOnSuccessListener {
                showToast(
                    context,
                    context.getString(R.string.barrier_success)
                )
            }
            .addOnFailureListener { e ->
                showToast(
                    context,
                    context.getString(R.string.barrier_failed)
                )
            }
    }

    private fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
