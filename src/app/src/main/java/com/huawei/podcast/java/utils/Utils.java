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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.podcast.R;

import java.text.NumberFormat;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

public class Utils {
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static void addBarrier(
            Context context, final String label, AwarenessBarrier barrier, PendingIntent pendingIntent) {
        BarrierUpdateRequest.Builder builder = new BarrierUpdateRequest.Builder();
        // When the status of the registered barrier changes, pendingIntent is triggered.
        // label is used to uniquely identify the barrier. You can query a barrier by label and delete it.
        BarrierUpdateRequest request = builder.addBarrier(label, barrier, pendingIntent).build();
        Awareness.getBarrierClient(context)
                .updateBarriers(request)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showToast(context, context.getString(R.string.barrier_success));
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                showToast(context, context.getString(R.string.barrier_failed));
                            }
                        });
    }

    private static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return capabilities != null
                        && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }

        return false;
    }

    /**
     * [get formatted string]<BR>
     *
     * @param context context
     * @param secs time in second
     * @return formatted string
     */
    public static String makeTimeString(Context context, long secs) {
        StringBuilder sFormatBuilder = new StringBuilder();
        Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
        LOCK.lock();
        String sTime;
        try {
            String durationFormat =
                    context.getString(secs >= 216000 ? R.string.durationformatlong : R.string.durationformatshort);
            final Object[] timeArgs = new Object[5];
            timeArgs[0] = secs / 3600;
            timeArgs[1] = secs / 60;
            timeArgs[2] = (secs / 60) % 60;
            timeArgs[3] = secs;
            timeArgs[4] = secs % 60;
            sTime = sFormatter.format(durationFormat, timeArgs).toString();
        } finally {
            LOCK.unlock();
            sFormatter.close();
        }

        return sTime;
    }

    /**
     * [local time]<BR>
     *
     * @param context context
     * @param secs time in second
     * @return local time string
     */
    public static String localeString(Context context, long secs) {
        String s = makeTimeString(context, secs);
        return localeString(s);
    }

    /**
     * [local time]<BR>
     *
     * @param s time info
     * @return local time string
     */
    public static String localeString(String s) {
        int zero = Constants.ZERO;
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        char localeZero = nf.format(zero).charAt(Constants.ZERO);
        if (localeZero != '0') {
            int length = s.length();
            int offsetToLocalizedDigits = localeZero - '0';
            StringBuilder result = new StringBuilder(length);
            for (int i = Constants.ZERO; i < length; ++i) {
                char ch = s.charAt(i);
                if (ch >= '0' && ch <= '9') {
                    ch += offsetToLocalizedDigits;
                }
                result.append(ch);
            }
            return result.toString();
        }
        return s;
    }
    public static void shareLink(String agcLink, Context context, HiAnalyticsInstance instance) {
        if (agcLink != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(Constants.type);
            intent.putExtra(Intent.EXTRA_TEXT, agcLink);
            context.startActivity(intent);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.LOGIN, Constants.COUNT);
            instance.onEvent(Constants.COUNT, bundle);
        }
    }
}
