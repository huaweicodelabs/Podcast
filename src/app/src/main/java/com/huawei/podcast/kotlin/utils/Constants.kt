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

object Constants {
    /*==========AppLinking================*/
    const val domainUriPrefix = "https://huaweipodcastapp.dra.agconnect.link"
    const val deepLink = "https://huaweipodcastapp.dra.agconnect.link/iB5V"
    const val type = "text/plain"
    const val TABLE_NAME = "playback_history"
    const val URL = "url"
    const val AUTHOR = "author"
    const val DATE = "date"
    const val TITLE = "title"
    const val CATEGORY = "category"
    const val POSITION = "position"
    const val SUB_TABLE_NAME = "subscribe_list"
    const val TOPIC = "topic"
    const val UPDATED_AT = "updatedAt"
    const val REQUEST_CODE_ONE = 1
    const val EMPTY_STRING = ""
    const val SPACE_STRING = " "
    const val REGEX_STRING = "\\s"
    const val FORMAT = "%02d:%02d:%02d"
    const val RECORD_FILE_NAME = "ADD-TO-POD-CAST"
    const val NOTIFICATION_PLAY = "music_notify_channel_id_play"
    const val RECORD_FILE_PATH = "/ADD-TO-POD-CAST"
    const val PREFS_NAME = "Pod_Cast"
    const val TOKEN = "com.menes.huawei.ON_NEW_TOKEN"
    const val DELETE_QUERY_FAILED = "Remove podcast failed"
    const val ADDED = "Added to favourites"
    const val ADDED_FAILED = "Insert Podcast info failed"
    const val QUERY_FAILED = "Query failed"
    const val KEEPING_BARRIER_LABEL = "keeping barrier label"
    const val CONNECTING_BARRIER_LABEL = "connecting barrier label"
    const val DISCONNECTING_BARRIER_LABEL = "disconnecting barrier label"
    const val SIGNIN = 9901
    const val ZERO = 0
    const val TWO = 2
    const val SCOPE_PROFILE = "https://www.huawei.com/auth/account/base.profile"
    const val SCOPE_EMAIL = "email"
    const val VALUE = 216000
    const val ThreeTHOUSAND = 3600
    const val SIXTY = 60
    const val USER_ID = "user_id"
    const val INTENT_FLAG = 123
    const val FAV_STATUS = "isFavourite"
    const val MSG_REFRESH = 1
    const val CANCEL_NOTIFICATION = "com.huawei.hms.mediacenter.cancel_notification"
    const val PUSH_TOKEN = "com.menes.audiokittryoutapp.ON_NEW_TOKEN"
    const val THOUSAND = 1000
    const val TEN = 10
    private const val DATE_FORMAT = "mmddyyyyhhmmss"
    const val FIVE_HUNDRED: Long = 500
    const val HUNDRED: Long = 100
    const val REQUEST_CODE_TWO = 2
    const val ONE = 1
    const val NINETY_TWO = 92
    const val QUERY_PLAYBACK_LIST_CONDITION =
        "SELECT * FROM playback_history WHERE title LIKE :title"
    const val QUERY_PLAYBACKLIST = "SELECT * FROM playback_history"
    const val QUERY_SUBSCRIBE_LIST_CONDITION =
        "SELECT * FROM subscribe_list WHERE topic LIKE :topic"
    const val QUERY_SUBSCRIBE_LIST = "SELECT * FROM subscribe_list"
}
