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

package com.lukasanda.aismobile.data.repository

import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.CourseDao
import com.lukasanda.aismobile.data.db.dao.DocumentDao
import com.lukasanda.aismobile.data.db.entity.Course
import com.lukasanda.aismobile.data.db.entity.Document
import com.lukasanda.aismobile.data.db.entity.Sheet
import com.lukasanda.aismobile.data.db.entity.Teacher
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.data.repository.CourseRepository.UpdateType.FETCH
import com.lukasanda.aismobile.data.repository.CourseRepository.UpdateType.NEWEST
import com.lukasanda.aismobile.util.*
import com.lukasanda.dataprovider.Parser
import com.lukasanda.dataprovider.data.Semester
import com.snakydesign.livedataextensions.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import timber.log.Timber

class CourseRepository(
    private val aisApi: AISApi,
    private val courseDao: CourseDao,
    private val documentDao: DocumentDao,
    private val prefs: Prefs
) {

    fun get() = courseDao.getCourses().map { it.groupBy { it.course.semester } }.map { it.values.toList() }

    fun get(courseId: String) = courseDao.getCourse(courseId)

    suspend fun update2(handler: CourseUpdateHandler? = null): ResponseResult = withContext(Dispatchers.IO) {
        Timber.d("Saved semester update time: ${prefs.courseExpiration}, is afternow : ${prefs.courseExpiration.isAfterNow}")

        if (prefs.courseExpiration.isAfterNow) return@withContext ResponseResult.Authenticated


        val updateType = if (prefs.fullCourseExpiration.isAfterNow) NEWEST else FETCH

        prefs.courseExpiration = DateTime.now().plusMinutes(10)
        prefs.fullCourseExpiration = DateTime.now().plusWeeks(1)

        runCatching {
            repeatIfException(5, 2000) { aisApi.semesters().authenticatedOrThrow2() } // Get semester response
                ?.let { Parser.getSemesters(it) } // Map it to semester list
                ?.lastOrAll(updateType == NEWEST) // If we update the newest, just take the last one, otherwise take all of them
                ?.also { handler?.onSemesterCount(it.size) } // Notify the handler about the size of our semester list
                ?.mapIndexed { index: Int, semester: Semester ->
                    handler?.onSemesterStartDownloading(index, semester.name) //Notify the handler we are working with current semester
                    Pair(repeatIfException(5, 2000) {  // Get courses response for every semester
                        aisApi.subjects("${semester.studiesId};obdobi=${semester.id}").authenticatedOrThrow2()
                    }, semester)
                }
                ?.filter { it.first != null } // Pretty self explanatory
                ?.map { Pair(Parser.getCourses(it.first!!), it.second) } // Map the responses to courses
                ?.filter { it.first != null } // Again, self explanatory
                ?.flatMap { pair ->  // Transform List<Pair<List<Course>, Semester>> into List<List<Pair<Course, Semester>>>
                    pair.first?.map {
                        Pair(it, pair.second)
                    } ?: emptyList()
                }
                ?.map { courseToDb(it.first, it.second) } // Map downloaded courses to db objects
                ?.also {
                    it.map { Pair(it.semester, it.semesterId) } // Map semesters
                        .toSet() // Make unique sets, because we have repetitions
                        .map { Document("__${it.second})", it.first, "", "", false) } // Map to db object
                        .also { documentDao.insertDocuments(it) } // Insert them to db
                }
                ?.also {
                    it.map { Document(it.documentsId, it.courseName.substringAfter(" "), "", "__${it.semesterId})", false) }
                        .filterNot { it.id.isEmpty() } // Filter out the documents with empty id
                        .also { documentDao.insertDocuments(it) } // Insert them to db
                }
                ?.also { courseDao.insertCourses(it) } // Insert the courses to db
                ?.groupBy { it.semesterId }
                ?.maxBy { it.key }
                ?.let { it.value }
                ?.forEach { getSpecificCourse(it) }
        }.exceptionOrNull()?.takeIf { it is AuthException }?.let {
            return@withContext ResponseResult.AuthError
        }

        return@withContext ResponseResult.Authenticated
    }

    suspend fun deleteAll() {
        courseDao.deleteTeachers()
        courseDao.deleteCourses()
        courseDao.deleteSheets()
    }

    private suspend fun getSpecificCourse(course: Course) {
        val dbTeachers = mutableListOf<Teacher>()
        val dbSheets = mutableListOf<Sheet>()

        aisApi.getCourseDetail(course.id).authenticatedOrReturn2 { courseDetailResponse ->
            val teachers = Parser.getTeachers(courseDetailResponse)?.map { teachersToDb(course, it) } ?: emptyList()
            dbTeachers.addAll(teachers)
            ResponseResult.Authenticated
        }

        aisApi.subjectSheets("${course.study};obdobi=${course.semesterId};predmet=${course.id};zobraz_prubezne=1").authenticatedOrReturn2 { sheetResponse ->
            val serverSheets = Parser.getSheets(sheetResponse) ?: mutableListOf()
            dbSheets.addAll(serverSheets.map { sheetToDb(it, course) })

            delay(3000)
            ResponseResult.Authenticated
        }

        val allSheets = courseDao.getAllSheets().filter { it.courseId == course.id }

        allSheets.forEach {
            courseDao.deleteSheet(it)
        }

        courseDao.insertTeachers(dbTeachers)
        courseDao.insertSheets(dbSheets)
    }

    suspend fun getSpecificCourse(courseId: String) {
        val course = courseDao.getAllCourses().find { it.id == courseId } ?: return
        getSpecificCourse(course)
    }

    private fun teachersToDb(course: Course, teacher: com.lukasanda.dataprovider.data.Teacher) = Teacher(name = teacher.name, id = teacher.id, courseId = course.id)

    private fun courseToDb(course: com.lukasanda.dataprovider.data.Course, semester: Semester) =
        Course(
            id = course.courseId,
            courseName = course.courseName,
            coursePresence = course.coursePresence,
            seminarPresence = course.seminarPresence,
            documentsId = course.documentsId,
            hasAISTests = course.hasAISTests,
            hasSheets = course.hasSheets,
            study = semester.studiesId,
            semester = semester.name,
            semesterId = semester.id.ifEmpty { "0" }.toInt(),
            finalMark = "-"
        )

    private fun sheetToDb(sheet: com.lukasanda.dataprovider.data.Sheet, course: Course) = Sheet(
        course.id + sheet.name,
        courseId = course.id,
        name = sheet.name,
        comment = sheet.comment,
        headers = sheet.headers,
        values = sheet.values
    )

    enum class UpdateType {
        FETCH, NEWEST
    }

    interface CourseUpdateHandler {
        suspend fun onSemesterCount(semesterCount: Int)
        suspend fun onSemesterStartDownloading(semesterIndex: Int, semesterName: String)
    }
}