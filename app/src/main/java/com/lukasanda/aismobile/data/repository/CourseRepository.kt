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

import android.util.Log
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
import com.lukasanda.aismobile.util.authenticatedOrReturn2
import com.lukasanda.aismobile.util.authenticatedOrThrow2
import com.lukasanda.dataprovider.Parser
import com.lukasanda.dataprovider.data.Semester
import com.snakydesign.livedataextensions.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

class CourseRepository(
    private val aisApi: AISApi,
    private val courseDao: CourseDao,
    private val documentDao: DocumentDao,
    private val prefs: Prefs
) {

    fun get() = courseDao.getCourses().map { it.groupBy { it.course.semester } }.map { it.values.toList() }

    fun get(courseId: String) = courseDao.getCourse(courseId)

    suspend fun update(handler: CourseUpdateHandler? = null): ResponseResult {

        Log.d("TAG", "ACTUAL semesters: ${prefs.courseExpiration.toString()}")

//        if (prefs.courseExpiration.isAfterNow) return ResponseResult.Authenticated


        val updateType = if (prefs.fullCourseExpiration.isAfterNow) FETCH else FETCH

        prefs.courseExpiration = DateTime.now().plusHours(2)
        prefs.fullCourseExpiration = DateTime.now().plusWeeks(1)

        Log.d("TAG", "NEW semesters: ${prefs.courseExpiration.toString()}")

        val semestersResponse = aisApi.semesters().authenticatedOrThrow2()

        var semesters = Parser.getSemesters(semestersResponse)

        if (updateType == NEWEST) semesters = semesters?.takeLast(1)

        handler?.onSemesterCount(semesters?.size ?: 0)

        delay(3000)

        val dbCourses = mutableListOf<Course>()
//        val dbSheets = mutableListOf<Sheet>()
//        val dbTeachers = mutableListOf<Teacher>()

        val responses = semesters?.mapIndexed { index, semester ->
            handler?.onSemesterStartDownloading(index, semester.name)
            withContext(Dispatchers.IO) {
                aisApi.subjects("${semester.studiesId};obdobi=${semester.id}").authenticatedOrReturn2 { coursesResponse ->
                    val coursesServer = Parser.getCourses(coursesResponse) ?: mutableListOf()
                    val courses = coursesServer.map { courseToDb(it, semester) }

                    println(courses)

                    dbCourses.addAll(courses)

                    delay(3000)

                    ResponseResult.Authenticated

//                val responses = courses.map { course ->
//
//                    aisApi.getCourseDetail(course.id).authenticatedOrReturn2 { courseDetailResponse ->
//                        val teachers = Parser.getTeachers(courseDetailResponse)?.map { teachersToDb(course, it) } ?: emptyList()
//                        dbTeachers.addAll(teachers)
//
//                        aisApi.subjectSheets("${semester.studiesId};obdobi=${semester.id};predmet=${course.id};zobraz_prubezne=1").authenticatedOrReturn2 { sheetResponse ->
//                            val serverSheets = Parser.getSheets(sheetResponse) ?: mutableListOf()
//                            dbSheets.addAll(serverSheets.map { sheetToDb(it, course) })
//
//                            ResponseResult.Authenticated
//                        }
//
//                        delay(3000)
//                        ResponseResult.Authenticated
//                    }
//                }

//                val result = if (responses.all { it == ResponseResult.Authenticated }) {
//                    ResponseResult.Authenticated
//                } else if (responses.contains(ResponseResult.AuthError)) {
//                    ResponseResult.AuthError
//                } else {
//                    ResponseResult.NetworkError
//                }

//                result
                }
            }
        } ?: listOf(ResponseResult.NetworkError)

        val dbSemesterDocuments = dbCourses.map { Pair(it.semester, it.semesterId) }.toSet().map {
            Document(
                "__${it.second})",
                it.first,
                "",
                "",
                false
            )
        }

        val dbDocuments = dbCourses.map {
            Document(
                it.documentsId,
                it.courseName.substringAfter(" "),
                "",
                "__${it.semesterId})",
                false
            )
        }.filterNot { it.id.isEmpty() }

//        val savedSheets = courseDao.getAllSheets().groupBy { it.courseId }
//        val mappedSheets = dbSheets.groupBy { it.courseId }
//
//        val updatedCourseIds = mutableListOf<String>()
//
//        savedSheets.forEach {
//            val sheets = it.value.map { Triple(it.name, it.comments(), it.getColumnPairs()) }
//            val otherSheets = mappedSheets[it.key]?.map { Triple(it.name, it.comments(), it.getColumnPairs()) } ?: emptyList()
//
//            if (!sheets.containsAll(otherSheets) && otherSheets.isNotEmpty()) {
//                updatedCourseIds.add(it.key)
//            }
//        }

        //TODO post a notification about updated courses

        val allCourses = courseDao.getAllCourses()


        when (updateType) {
            FETCH -> {
                documentDao.updateFolder("", dbSemesterDocuments)
                dbDocuments.groupBy { it.parentFolderId }.forEach {
                    documentDao.updateFolder(it.key, it.value)
                }
                courseDao.update(dbCourses)
            }
            NEWEST -> {
                documentDao.insertDocuments(dbSemesterDocuments)
                documentDao.insertDocuments(dbDocuments)
                courseDao.updateSingle(dbCourses)
            }
        }

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

    suspend fun getSpecificCourse(courseId: String) {
        val course = courseDao.getAllCourses().find { it.id == courseId } ?: return
        val dbTeachers = mutableListOf<Teacher>()
        val dbSheets = mutableListOf<Sheet>()

        aisApi.getCourseDetail(courseId).authenticatedOrReturn2 { courseDetailResponse ->
            val teachers = Parser.getTeachers(courseDetailResponse)?.map { teachersToDb(course, it) } ?: emptyList()
            dbTeachers.addAll(teachers)
            ResponseResult.Authenticated
        }

        aisApi.subjectSheets("${course.study};obdobi=${course.semesterId};predmet=${course.id};zobraz_prubezne=1").authenticatedOrReturn2 { sheetResponse ->
            val serverSheets = Parser.getSheets(sheetResponse) ?: mutableListOf()
            dbSheets.addAll(serverSheets.map { sheetToDb(it, course) })

            ResponseResult.Authenticated
        }

        courseDao.insertTeachers(dbTeachers)
        courseDao.insertSheets(dbSheets)
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