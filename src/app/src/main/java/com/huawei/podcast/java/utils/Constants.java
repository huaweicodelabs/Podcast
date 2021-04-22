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

public class Constants {
    /* ==========AppLinking================ */
    public static final String domainUriPrefix = "https://huaweipodcastapp.dra.agconnect.link";
    public static final String deepLink = "https://huaweipodcastapp.dra.agconnect.link/iB5V";
    public static final String type = "text/plain";
    public static final String MyService = "MyService";
    public static final String PLAYBACK_HISTORY = "playback_history";
    public static final String SUBSCRIBE_LIST = "subscribe_list";
    public static final String TOPIC = "topic";
    public static final boolean TRUE = true;
    public static final boolean FALSE = false;
    public static final String PODCAST_CLOUD = "Podcast";
    public static final int SAMPLE_RATE = 44100;
    public static final int DEFAULT_IMAGE_ALPHA = 255;
    public static final String RECORDING = "Recording";
    public static final String FORMAT = "%02d:%02d:%02d";
    public static final String SCALE_FORMAT = "ScaleType %s not supported.";
    public static final String UPDTAED_AT = "updatedAt";
    public static final String CODELABS_ACTION = "com.huawei.codelabpush.action";
    public static final String ON_MESSAGE_SENT = "onMessageSent";
    public static final String ON_MESSAGE_CALLED = "onMessageSent called, Message id:";
    public static final String ON_SEND_ERROR = "onSendError";
    public static final String ON_SEND_ERROR_CALLED = "onSendError called, message id:";
    public static final String DESCRIPTION = ", description:";
    public static final String ERROR = ", ErrCode:";
    public static final String PAYLOAD = ", payload data:";
    public static final String EXCEPTION = "adjustViewBounds not supported.";
    public static final String PLAY = "Play";
    public static final String DELETE_QUERY_FAILED = "Remove podcast failed";
    public static final String ADDED = "Added to favourites";
    public static final String ADDED_FAILED = "Insert Podcast info failed";
    public static final String QUERY_FAILED = "Query failed";

    public static final int REQUEST_CODE_ONE = 1;
    public static final int REQUEST_CODE_TWO = 2;
    public static final int SIXTY = 60;
    public static final int ZERO = 0;
    public static final int TEN = 10;
    public static final int ONE = 1;
    public static final int NINETY_TWO = 92;
    public static final int REFRESH_TIME = 30;
    public static final int FIVE_HUNDRED = 500;
    public static final int THOUSAND = 1000;
    public static final int HUNDRED = 100;
    public static final int SIGN_CODE = 9901;
    public static final String CANCEL_NOTIFICATION = "com.huawei.hms.mediacenter.cancel_notification";
    public static final String PUSH_NOTIFICATION = "com.menes.audiokittryoutapp.ON_NEW_TOKEN";
    public static final String KEEPING_BARRIER_LABEL = "keeping barrier label";
    public static final String DATE_FORMAT = "mmddyyyyhhmmss";
    public static final String FAV_STATUS = "isFavourite";
    public static final String NOTIFY_CHANNEL_ID_PLAY = "music_notify_channel_id_play";
    public static final String TOKEN = "com.menes.huawei.ON_NEW_TOKEN";
    public static final String ON_NEW_TOKEN_CALLED = "onNewToken called, token: ";
    public static final String ON_MESSAGE_RECEIVED = "onMessageReceived";
    public static final String ON_MESSAGE_RECEIVED_CALLED = "onMessageReceived called, message id:";
    public static final String MSG = "msg";
    public static final String METHOD = "method";
    public static final String ON_NEW_TOKEN = "onNewToken";
    public static final String USER_ID = "user_id";
    public static final String URL = "url";
    public static final String COUNT = "count";
    public static final String LOGIN = "login";
    public static final String AUTHOR = "author";
    public static final String DATE = "date";
    public static final String TITLE = "title";
    public static final String CATEGORY = "category";
    public static final String POSITION = "position";
    public static final String DB = "podCast.db";
    public static final String CloudDBZoneWrapper = "CloudDBZoneWrapper";
    public static final String RECORD_FILE_NAME = "ADD-TO-POD-CAST";
    public static final String RECORD_FILE_PATH = "/ADD-TO-POD-CAST";
    public static final String LIB_NAME = "native-audio-jni";
    public static final String PREFS_NAME = "Pod_Cast";
    public static final String EMPTY_STRING = "";
    public static final String REGEX_STRING = "\\s";
    public static final String QUERY_PLAYBACK_LIST_CONDITION = "SELECT * FROM playback_history WHERE title LIKE :title";
    public static final String QUERY_PLAYBACKLIST = "SELECT * FROM playback_history";
    public static final String QUERY_SUBSCRIBE_LIST_CONDITION = "SELECT * FROM subscribe_list WHERE topic LIKE :topic";
    public static final String QUERY_SUBSCRIBE_LIST = "SELECT * FROM subscribe_list";
    public static final String CONNECTING_BARRIER_LABEL = "connecting barrier label";
    public static final String DISCONNECTING_BARRIER_LABEL = "disconnecting barrier label";
    public static final int INTENT_FLAG = 123;
    // Ad display timeout interval, in milliseconds.
    public static final int AD_TIMEOUT = 5000;
    public static final long LONG_MINUS_FIVE = -5;
    public static final int MINUS_ONE = -1;

    // Ad display timeout message flag.
    public static final int MSG_AD_TIMEOUT = 1001;
}
