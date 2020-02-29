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
import com.lukasanda.aismobile.data.db.entity.Course
import com.lukasanda.aismobile.data.db.entity.FullCourse
import com.lukasanda.aismobile.data.db.entity.Sheet
import com.lukasanda.aismobile.data.db.entity.Teacher

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheets(sheets: List<Sheet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    @Query("DELETE FROM sheet")
    suspend fun deleteSheets()

    @Query("DELETE FROM course")
    suspend fun deleteCourses()

    @Query("DELETE FROM teacher")
    suspend fun deleteTeachers()

    @Query("SELECT * FROM COURSE WHERE id = :courseId")
    @Transaction
    fun getCourse(courseId: String): LiveData<FullCourse>

    @Query("SELECT * FROM COURSE")
    @Transaction
    fun getCourses(): LiveData<List<FullCourse>>

    @Query("SELECT DISTINCT semester FROM COURSE")
    @Transaction
    fun getSemesters(): LiveData<List<String>>

    @Transaction
    suspend fun update(courses: List<Course>, sheets: List<Sheet>, teachers: List<Teacher>) {
        deleteSheets()
        deleteCourses()
        deleteTeachers()
        insertCourses(courses)
        insertSheets(sheets)
        insertTeachers(teachers)
    }

    @Transaction
    suspend fun updateSingle(courses: List<Course>, sheets: List<Sheet>, teachers: List<Teacher>) {
        insertCourses(courses)
        insertSheets(sheets)
        insertTeachers(teachers)
    }

}