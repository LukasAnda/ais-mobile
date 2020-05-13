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

package com.lukasanda.aismobile.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lukasanda.aismobile.data.db.entity.TimetableItem

@Dao
interface TimetableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TimetableItem>)

    @Query("DELETE FROM timetable_entry")
    suspend fun deleteAll()

    @Query("SELECT * FROM timetable_entry")
    fun getAll(): LiveData<List<TimetableItem>>

    @Query("SELECT * FROM timetable_entry")
    suspend fun getAllSuspend(): List<TimetableItem>

    @Transaction
    suspend fun update(items: List<TimetableItem>) {
        deleteAll()
        insertAll(items)
    }
}