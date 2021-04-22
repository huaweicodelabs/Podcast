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
package com.huawei.podcast.java.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.huawei.hms.kit.awareness.barrier.BarrierStatus;
import com.huawei.podcast.R;
import com.huawei.podcast.java.utils.Constants;

public class HeadsetBarrierReceiver extends BroadcastReceiver {
    private static final String KEEPING_BARRIER_LABEL = Constants.KEEPING_BARRIER_LABEL;
    private static final String CONNECTING_BARRIER_LABEL = Constants.CONNECTING_BARRIER_LABEL;
    private static final String DISCONNECTING_BARRIER_LABEL = Constants.DISCONNECTING_BARRIER_LABEL;

    @Override
    public void onReceive(Context context, Intent intent) {
        BarrierStatus barrierStatus = BarrierStatus.extract(intent);
        String label = barrierStatus.getBarrierLabel();
        int barrierPresentStatus = barrierStatus.getPresentStatus();
        switch (label) {
            case KEEPING_BARRIER_LABEL:
                if (barrierPresentStatus == BarrierStatus.TRUE) {
                    showToast(context,context.getResources().getString(R.string.headset_connected));
                } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                    showToast(context,context.getResources().getString(R.string.headset_disConnected));
                } else {
                    showToast(context,context.getResources().getString(R.string.headset_unknown));
                }
                break;

            case CONNECTING_BARRIER_LABEL:
                if (barrierPresentStatus == BarrierStatus.TRUE) {
                    showToast(context,context.getResources().getString(R.string.headset_connecting));
                } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                    showToast(context,context.getResources().getString(R.string.headset_not_connecting));

                } else {
                    showToast(context,context.getResources().getString(R.string.headset_unknown));
                }
                break;

            case DISCONNECTING_BARRIER_LABEL:
                if (barrierPresentStatus == BarrierStatus.TRUE) {
                    showToast(context,context.getResources().getString(R.string.headset_disconnecting));
                } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                    showToast(context,context.getResources().getString(R.string.headset_not_disconnecting));
                } else {
                    showToast(context,context.getResources().getString(R.string.headset_unknown));
                }
                break;

            default:
                break;
        }
    }
    private void showToast(Context context,String msg){
        Toast.makeText(context, msg,
                Toast.LENGTH_SHORT).show();
    }
}
