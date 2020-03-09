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
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.data.repository.CourseRepository.UpdateType.FETCH
import com.lukasanda.aismobile.data.repository.CourseRepository.UpdateType.NEWEST
import com.lukasanda.aismobile.util.ResponseResult
import com.lukasanda.aismobile.util.authenticatedOrReturn
import com.lukasanda.aismobile.util.authenticatedOrThrow
import com.lukasanda.dataprovider.Parser
import com.lukasanda.dataprovider.data.Semester
import com.snakydesign.livedataextensions.map
import kotlinx.coroutines.delay
import org.joda.time.DateTime

class CourseRepository(
    private val aisApi: AISApi,
    private val courseDao: CourseDao,
    private val documentDao: DocumentDao,
    private val prefs: Prefs
) {

    fun get() = courseDao.getCourses().map { it.groupBy { it.course.semester } }.map { it.values.toList() }

    fun get(courseId: String) = courseDao.getCourse(courseId)

    suspend fun update(): ResponseResult {

        if (prefs.courseExpiration.plusHours(1).isAfterNow) return ResponseResult.Authenticated

        val updateType = if (prefs.fullCourseExpiration.plusWeeks(1).isAfterNow) NEWEST else FETCH

        val semestersResponse = aisApi.semesters().authenticatedOrThrow()

        var semesters = Parser.getSemesters(semestersResponse)

        if (updateType == NEWEST) semesters = semesters?.takeLast(1)

        delay(1000)

        val dbCourses = mutableListOf<Course>()
        val dbSheets = mutableListOf<Sheet>()
        val dbTeachers = mutableListOf<Teacher>()

        val responses = semesters?.map { semester ->
            aisApi.subjects(semester.studiesId, semester.id).authenticatedOrReturn { coursesResponse ->
                val coursesServer = Parser.getCourses(coursesResponse) ?: mutableListOf()
                val courses = coursesServer.map { courseToDb(it, semester) }

                dbCourses.addAll(courses)

                delay(1000)

                val responses = courses.map { course ->

                    aisApi.getCourseDetail(course.id).authenticatedOrReturn { courseDetailResponse ->
                        val teachers = Parser.getTeachers(courseDetailResponse)?.map { teachersToDb(course, it) } ?: emptyList()
                        dbTeachers.addAll(teachers)

                        aisApi.subjectSheets(semester.studiesId, semester.id, course.id).authenticatedOrReturn { sheetResponse ->
                            val serverSheets = Parser.getSheets(sheetResponse) ?: mutableListOf()
                            dbSheets.addAll(serverSheets.map { sheetToDb(it, course) })

                            ResponseResult.Authenticated
                        }

                        delay(1000)
                        ResponseResult.Authenticated
                    }
                }

                val result = if (responses.all { it == ResponseResult.Authenticated }) {
                    ResponseResult.Authenticated
                } else if (responses.contains(ResponseResult.AuthError)) {
                    ResponseResult.AuthError
                } else {
                    ResponseResult.NetworkError
                }

                result
            }
        } ?: listOf(ResponseResult.NetworkError)

        val dbDocuments = dbCourses.map { Document(it.documentsId, it.courseName.substringAfter(" "), "", "", false) }.filterNot { it.id.isEmpty() }


        when (updateType) {
            FETCH -> {

                documentDao.updateFolder("", dbDocuments)
                courseDao.update(dbCourses, dbSheets, dbTeachers)
            }
            NEWEST -> {
                documentDao.insertDocuments(dbDocuments)
                courseDao.updateSingle(dbCourses, dbSheets, dbTeachers)
            }
        }

        prefs.courseExpiration = DateTime.now()
        prefs.fullCourseExpiration = DateTime.now()

        return if (responses.all { it == ResponseResult.Authenticated }) {
            ResponseResult.Authenticated
        } else if (responses.contains(ResponseResult.AuthError)) {
            ResponseResult.AuthError
        } else {
            ResponseResult.NetworkError
        }
    }

    suspend fun deleteAll() {
        courseDao.deleteTeachers()
        courseDao.deleteCourses()
        courseDao.deleteSheets()
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
}