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
package com.huawei.podcast.java.services;

import android.content.Intent;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hms.push.SendException;
import com.huawei.podcast.java.utils.Constants;

public class HMSPushService extends HmsMessageService {
    private static final String CODELABS_ACTION = Constants.CODELABS_ACTION;
    /**
     * When an app calls the getToken method to apply for a token from the server,
     * if the server does not return the token during current method calling, the server can return the token through this method later.
     * This method callback must be completed in 10 seconds. Otherwise, you need to start a new Job for callback processing.
     *
     * @param token token
     */
    @Override
    public void onNewToken(String token) {
        Intent intent = new Intent();
        intent.setAction(CODELABS_ACTION);
        intent.putExtra(Constants.METHOD, Constants.ON_NEW_TOKEN);
        intent.putExtra(Constants.MSG, Constants.ON_NEW_TOKEN_CALLED + token);

        sendBroadcast(intent);
    }
    /**
     * This method is used to receive downstream data messages.
     * This method callback must be completed in 10 seconds. Otherwise, you need to start a new Job for callback processing.
     *
     * @param message RemoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Intent intent = new Intent();
        intent.setAction(CODELABS_ACTION);
        intent.putExtra(Constants.METHOD, Constants.ON_MESSAGE_RECEIVED);
        intent.putExtra(
                Constants.MSG,
                Constants.ON_MESSAGE_RECEIVED_CALLED + message.getMessageId() + Constants.PAYLOAD + message.getData());
        sendBroadcast(intent);
    }

    @Override
    public void onMessageSent(String msgId) {
        Intent intent = new Intent();
        intent.setAction(CODELABS_ACTION);
        intent.putExtra(Constants.METHOD, Constants.ON_MESSAGE_SENT);
        intent.putExtra(Constants.MSG, Constants.ON_MESSAGE_CALLED + msgId);
        sendBroadcast(intent);
    }

    @Override
    public void onSendError(String msgId, Exception exception) {
        Intent intent = new Intent();
        intent.setAction(CODELABS_ACTION);
        intent.putExtra(Constants.METHOD, Constants.ON_SEND_ERROR);
        intent.putExtra(
                Constants.MSG,
                Constants.ON_SEND_ERROR_CALLED
                        + msgId
                        + Constants.ERROR
                        + ((SendException) exception).getErrorCode()
                        + Constants.DESCRIPTION
                        + exception.getMessage());

        sendBroadcast(intent);
    }

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);
    }
}
