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
import com.lukasanda.aismobile.data.db.entity.Course
import com.lukasanda.aismobile.data.db.entity.Sheet
import com.lukasanda.aismobile.data.db.entity.Teacher
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.HTTPException
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.data.repository.CourseRepository.UpdateType.FETCH
import com.lukasanda.aismobile.data.repository.CourseRepository.UpdateType.NEWEST
import com.lukasanda.aismobile.util.authenticatedOrThrow
import com.snakydesign.livedataextensions.map
import kotlinx.coroutines.delay
import org.joda.time.DateTime
import sk.lukasanda.dataprovider.Parser
import sk.lukasanda.dataprovider.data.Semester

class CourseRepository(
    private val aisApi: AISApi,
    private val courseDao: CourseDao,
    private val prefs: Prefs
) {

    fun get() =
        courseDao.getCourses().map { it.groupBy { it.course.semester } }.map { it.values.toList() }

    fun get(courseId: String) = courseDao.getCourse(courseId)

    @Throws(AuthException::class, HTTPException::class)
    suspend fun update() {

        if (prefs.courseExpiration.isBeforeNow) return

        val updateType =
            if (prefs.fullCourseExpiration.isBeforeNow) NEWEST else FETCH //If there is no semester fetch, if there is one semester only FETCH == NEWEST

        val semestersResponse = aisApi.semesters().authenticatedOrThrow()

        val semesters = Parser.getSemesters(semestersResponse)

        delay(1000)

        val dbCourses = mutableListOf<Course>()
        val dbSheets = mutableListOf<Sheet>()
        val dbTeachers = mutableListOf<Teacher>()

        suspend fun updateSemester(semester: Semester) {
            val courses = parseCourses(semester)

            dbCourses.addAll(courses)
            delay(1000)

            courses.forEach { course ->

                val courseDetailResponse = aisApi.getCourseDetail(course.id).authenticatedOrThrow()
                val teachers =
                    Parser.getTeachers(courseDetailResponse)?.map { teachersToDb(course, it) }
                        ?: emptyList()
                dbTeachers.addAll(teachers)

                val newSheets = parseSheets(course, semester)

                dbSheets.addAll(newSheets)

                delay(1000)
            }
        }

        when (updateType) {
            FETCH -> {
                semesters?.forEach {
                    updateSemester(it)
                }

                println(dbSheets)
                courseDao.update(dbCourses, dbSheets, dbTeachers)
            }
            NEWEST -> {
                semesters?.last()?.let {
                    updateSemester(it)
                }
                courseDao.updateSingle(dbCourses, dbSheets, dbTeachers)
            }
        }

        prefs.courseExpiration = DateTime.now().plusMinutes(15)
        prefs.fullCourseExpiration = DateTime.now().plusWeeks(1)
    }


    private suspend fun parseCourses(semester: Semester): List<Course> {
        val coursesResponse =
            aisApi.subjects(semester.studiesId, semester.id).authenticatedOrThrow()
        val courses =
            Parser.getCourses(coursesResponse) ?: mutableListOf()

        return courses.map { courseToDb(it, semester) }
    }

    private suspend fun parseSheets(course: Course, semester: Semester): List<Sheet> {
        val sheetResponse =
            aisApi.subjectSheets(semester.studiesId, semester.id, course.id)
                .authenticatedOrThrow()
        val sheets =
            Parser.getSheets(sheetResponse) ?: mutableListOf()

//        println(sheets.joinToString("\n"))

        return sheets.map { sheetToDb(it, course) }
    }

    private fun teachersToDb(course: Course, teacher: sk.lukasanda.dataprovider.data.Teacher) =
        Teacher(name = teacher.name, id = teacher.id, courseId = course.id)

    private fun courseToDb(
        course: sk.lukasanda.dataprovider.data.Course,
        semester: Semester
    ) =
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

    private fun sheetToDb(
        sheet: sk.lukasanda.dataprovider.data.Sheet,
        course: Course
    ) = Sheet(
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