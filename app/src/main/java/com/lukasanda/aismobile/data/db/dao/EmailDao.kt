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
import com.lukasanda.aismobile.data.db.entity.Email

@Dao
interface EmailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<Email>)

    @Query("DELETE FROM email")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteSingle(email: Email)

    @Query("SELECT * FROM email ")
    fun getEmails(): LiveData<List<Email>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(email: Email)

    @Transaction
    suspend fun update(emails: List<Email>) {
        deleteAll()
        insertEmails(emails)
    }
}