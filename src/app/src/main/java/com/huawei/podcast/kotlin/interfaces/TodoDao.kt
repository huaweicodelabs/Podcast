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
package com.huawei.podcast.kotlin.interfaces

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.huawei.podcast.kotlin.data.model.PodCastList
import com.huawei.podcast.kotlin.data.model.SubscribeModel
import com.huawei.podcast.kotlin.utils.Constants

@Dao
interface TodoDao {
    /*playback History*/
    @Query(Constants.QUERY_PLAYBACKLIST)
    fun getAll(): List<PodCastList>
    @Query(Constants.QUERY_PLAYBACK_LIST_CONDITION)
    fun findByTitle(title: String): PodCastList
    @Insert
    fun insertAll(vararg todo: PodCastList)
    @Insert
    fun insertAllEpisodes(vararg todo: PodCastList)

    /*subscribe*/
    @Query(Constants.QUERY_SUBSCRIBE_LIST)
    fun getAllSubscribe(): LiveData<List<SubscribeModel>>
    @Query(Constants.QUERY_SUBSCRIBE_LIST_CONDITION)
    fun findBySubscribeTitle(topic: String): SubscribeModel
    @Insert
    fun insertSubscribeList(vararg todo: SubscribeModel)
    @Delete
    fun deleteSubscribe(todo: SubscribeModel)
}
