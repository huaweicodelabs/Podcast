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
package com.huawei.podcast.java.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.huawei.podcast.java.data.model.PodCastList;
import com.huawei.podcast.java.data.model.SubscribeModel;
import com.huawei.podcast.java.interfaces.TodoDao;
import com.huawei.podcast.java.utils.Constants;

@Database(
        entities = {PodCastList.class, SubscribeModel.class},
        version = Constants.REQUEST_CODE_ONE)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase mInstance;

    public abstract TodoDao todoDao();

    public static AppDatabase getDatabase(final Context context) {
        if (mInstance == null)
            synchronized (AppDatabase.class) {
                if (mInstance == null) {
                    mInstance =
                            Room.databaseBuilder(context, AppDatabase.class, Constants.DB)
                                    .fallbackToDestructiveMigration()
                                    .build();
                }
            }
        return mInstance;
    }
}
