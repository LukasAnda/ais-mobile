/*
 * Copyright 2019 Lukáš Anda. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lukasanda.aismobile.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lukasanda.aismobile.data.db.converter.DateConverter
import org.joda.time.DateTime

@Entity(tableName = "Session")
@TypeConverters(DateConverter::class)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "validUntil") val validUntil: DateTime
)
//) {
//    companion object {
//        fun to(repository: Repository): Bookmark {
//            return Bookmark(
//                name = repository.name,
//                description = repository.description,
//                language = repository.language,
//                stargazersCount = repository.stargazersCount,
//                created = Date()
//            )
//        }
//    }
//}