/*
 * Copyright 2020 Lukáš Anda. All rights reserved.
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

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_entry")
data class TimetableItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: String = "",
    val name: String = "",
    val place: String = "",
    val teacher: String = "",
    val acronym: String = "",
    val dayOfWeek: Int = 0,
    val startTime: String = "",
    val endTime: String = "",
    val isSeminar: Boolean = false
) {
    @Ignore
    var isNext: Boolean = false

    fun next() = this.copy().apply { isNext = true }
}