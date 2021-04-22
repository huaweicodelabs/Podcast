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
package com.huawei.podcast.java.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.huawei.podcast.java.utils.Constants;

public class SharedPreference {

    private static SharedPreferences sharedPref;

    public static void init(Context context) {
        sharedPref = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void save(String KEY_NAME, String text) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_NAME, text);
        editor.apply();
    }

    public static Boolean contains(String KEY_NAME) {
        return sharedPref.contains(KEY_NAME);
    }

    public static String getValueString(String KEY_NAME) {
        return sharedPref.getString(KEY_NAME, null);
    }

    public static void removeValue(String KEY_NAME) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(KEY_NAME);
        editor.apply();
    }
}
