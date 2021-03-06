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

package com.lukasanda.aismobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lukasanda.aismobile.data.db.AppDatabase.Companion.DB_VERSION
import com.lukasanda.aismobile.data.db.dao.*
import com.lukasanda.aismobile.data.db.entity.*

@Database(
    entities = [TimetableItem::class, Profile::class, Course::class, Sheet::class, Email::class, Teacher::class, Document::class],
    version = DB_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getTimetableDao(): TimetableDao
    abstract fun getProfileDao(): ProfileDao
    abstract fun getCourseDao(): CourseDao
    abstract fun getEmailDao(): EmailDao
    abstract fun getDocumentDao(): DocumentDao

    companion object {
        const val DB_VERSION = 1
        private const val DB_NAME = "aisdb.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}