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

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.huawei.podcast.R
import java.text.NumberFormat
import java.util.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.locks.ReentrantLock

fun isNetworkConnected(context: Context): Boolean {
    var result = false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        connectivityManager.run {
            connectivityManager.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
    }

    return result
}

private val LOCK = ReentrantLock()

/**
 * [get formatted string]<BR></BR>
 *
 * @param context context
 * @param secs time in second
 * @return formatted string
 */
fun makeTimeString(context: Context, secs: Long): String {
    val sFormatBuilder = StringBuilder()
    val sFormatter = Formatter(sFormatBuilder, Locale.getDefault())
    LOCK.lock()
    val sTime: String
    try {
        val durationFormat = context.getString(if (secs >= Constants.VALUE) R.string.durationformatlong else R.string.durationformatshort)
        val timeArgs = arrayOfNulls<Any>(5)
        timeArgs[0] = secs / Constants.ThreeTHOUSAND
        timeArgs[1] = secs / Constants.SIXTY
        timeArgs[2] = secs / Constants.SIXTY % Constants.SIXTY
        timeArgs[3] = secs
        timeArgs[4] = secs % Constants.SIXTY
        sTime = sFormatter.format(durationFormat, *timeArgs).toString()
    } finally {
        LOCK.unlock()
        sFormatter.close()
    }
    return sTime
}

/**
 * [local time]<BR></BR>
 *
 * @param context context
 * @param secs time in second
 * @return local time string
 */
fun localeString(context: Context, secs: Long): String {
    val s = makeTimeString(context, secs)
    return localeString(s)
}

/**
 * [local time]<BR></BR>
 *
 * @param s time info
 * @return local time string
 */

fun localeString(s: String): String {
    val zero = 0
    val nf = NumberFormat.getIntegerInstance()
    nf.isGroupingUsed = false
    val localeZero = nf.format(zero.toLong())[0]
    if (localeZero != '0') {
        val length = s.length
        val offsetToLocalizedDigits = localeZero - '0'
        val result = StringBuilder(length)
        for (i in 0 until length) {
            var ch = s[i]
            if (ch in '0'..'9') {
                ch += offsetToLocalizedDigits.toChar().toInt()
            }
            result.append(ch)
        }
        return result.toString()
    }
    return s
}
