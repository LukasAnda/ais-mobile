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

package com.lukasanda.aismobile.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lukasanda.aismobile.data.db.entity.Profile

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Query("DELETE FROM profile")
    suspend fun deleteAll()

    @Query("SELECT * FROM profile LIMIT 1")
    fun getProfile(): LiveData<Profile?>

    @Transaction
    suspend fun update(profile: Profile) {
        deleteAll()
        insertProfile(profile)
    }
}