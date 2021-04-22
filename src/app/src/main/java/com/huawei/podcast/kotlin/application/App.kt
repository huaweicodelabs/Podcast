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
package com.huawei.podcast.kotlin.application

import android.app.Application
import androidx.room.Room
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.database.AppDatabase
import com.huawei.podcast.kotlin.database.CloudDBZoneWrapper
import com.huawei.podcast.kotlin.preference.SharedPreference
import com.huawei.podcast.kotlin.main.viewmodel.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        /*shared preference*/
        SharedPreference.init(this)
        /*koin dependency injection*/
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    viewModelModule
                )
            )
        }
        /*data base*/
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, getString(R.string.database_name)
        ).build()

        CloudDBZoneWrapper.initAGConnectCloudDB(this)
    }
    companion object {
        lateinit var instance: App private set
    }
}
