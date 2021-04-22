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
package com.huawei.podcast.java.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.huawei.podcast.java.utils.Constants;

@Entity(tableName = Constants.SUBSCRIBE_LIST)
public class SubscribeModel {
    public int getPrim_id() {
        return prim_id;
    }

    public void setPrim_id(int prim_id) {
        this.prim_id = prim_id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrimaryKey(autoGenerate = true)
    int prim_id;

    @ColumnInfo(name = Constants.TOPIC)
    String topic;

    @ColumnInfo(name = Constants.UPDTAED_AT)
    String updatedAt;
}
