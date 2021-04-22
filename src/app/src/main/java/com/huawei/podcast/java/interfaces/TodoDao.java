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
package com.huawei.podcast.java.interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.huawei.podcast.java.data.model.PodCastList;
import com.huawei.podcast.java.data.model.SubscribeModel;
import com.huawei.podcast.java.utils.Constants;

import java.util.List;

@Dao
public interface TodoDao {
    @Query(Constants.QUERY_PLAYBACKLIST)
    List<PodCastList> getAll();

    @Query(Constants.QUERY_PLAYBACK_LIST_CONDITION)
    PodCastList findByTitle(String title);

    @Insert
    void insertAll(PodCastList todo);

    @Insert
    void insertAllEpisodes(PodCastList todo);

    /* subscribe */
    @Query(Constants.QUERY_SUBSCRIBE_LIST)
    LiveData<List<SubscribeModel>> getAllSubscribe();

    @Query(Constants.QUERY_SUBSCRIBE_LIST_CONDITION)
    SubscribeModel findBySubscribeTitle(String topic);

    @Insert
    void insertSubscribeList(SubscribeModel todo);

    @Delete
    void deleteSubscribe(SubscribeModel subscribeModel);
}
