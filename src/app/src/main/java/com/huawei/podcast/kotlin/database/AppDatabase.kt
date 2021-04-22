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
package com.huawei.podcast.kotlin.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.data.model.PodCastList
import com.huawei.podcast.kotlin.data.model.SubscribeModel
import com.huawei.podcast.kotlin.interfaces.TodoDao
import com.huawei.podcast.kotlin.utils.Constants
import com.huawei.podcast.kotlin.utils.Strings

@Database(
    entities = [PodCastList::class, SubscribeModel::class],
    version = Constants.REQUEST_CODE_ONE
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, Strings.get(R.string.database_name)
        )
            .build()
    }
}
