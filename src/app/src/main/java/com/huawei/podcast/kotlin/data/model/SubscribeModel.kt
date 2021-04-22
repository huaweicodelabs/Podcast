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
package com.huawei.podcast.kotlin.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.huawei.podcast.kotlin.utils.Constants

@Entity(tableName = Constants.SUB_TABLE_NAME)
data class SubscribeModel(
    @PrimaryKey(autoGenerate = true)
    var prim_id: Int,
    @ColumnInfo(name = Constants.TOPIC)
    var topic: String = Constants.EMPTY_STRING,
    @ColumnInfo(name = Constants.UPDATED_AT)
    var updatedAt: String = Constants.EMPTY_STRING

)
