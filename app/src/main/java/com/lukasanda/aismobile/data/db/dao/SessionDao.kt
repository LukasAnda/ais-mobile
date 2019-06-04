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

import androidx.room.*
import com.lukasanda.aismobile.data.db.entity.Session
import io.reactivex.Single

@Dao
interface SessionDao {

    @Query("SELECT * FROM Session ORDER BY id DESC LIMIT 1")
    fun findLatest(): Single<Session?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bookmark: Session)

    @Delete
    fun delete(bookmark: Session)

}