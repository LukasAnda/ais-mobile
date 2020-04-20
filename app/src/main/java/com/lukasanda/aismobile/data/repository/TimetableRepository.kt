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

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.TimetableDao
import com.lukasanda.aismobile.data.db.entity.TimetableItem
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.util.Difference
import com.lukasanda.aismobile.util.ResponseResult
import com.lukasanda.aismobile.util.authenticatedOrReturn2
import com.lukasanda.aismobile.util.repeatIfException
import com.lukasanda.dataprovider.Parser
import com.lukasanda.dataprovider.data.Schedule
import com.snakydesign.livedataextensions.map
import org.joda.time.DateTime

class TimetableRepository(
    private val aisApi: AISApi,
    private val timetableDao: TimetableDao,
    private val prefs: Prefs,
    private val context: Context
) {

    var actualDay = DateTime.now().dayOfWeek - 1
    //val days = MutableLiveData<Int>(actualDay)

    private val dayNames = context.resources.getStringArray(R.array.days).toList()
    private val dayNamesLiveData = MutableLiveData<String>()

    fun days(): LiveData<String> = dayNamesLiveData


    fun getCurrentDay(): Int {
//        var pageToSelect = Int.MAX_VALUE / 2
//        while (pageToSelect % dayNames.size != actualDay) {
//            pageToSelect++
//        }
        return actualDay + 1
    }

    fun get() = timetableDao.getAll().map { mapCourses(it) }

    suspend fun update(): ResponseResult {
        Log.d("TAG", prefs.timetableExpiration.toString())
        if (prefs.timetableExpiration.isAfterNow) {
            return ResponseResult.Authenticated
        }
        prefs.timetableExpiration = DateTime.now().plusWeeks(1)
        return repeatIfException(3, 2000) {
            aisApi.schedule("1?zobraz=1;format=json;rozvrh_student=${prefs.id}").authenticatedOrReturn2 { scheduleResponse ->

                val schedule = parseResponse(scheduleResponse)
                val courses = parseCourses(schedule)

                val originalCourses = timetableDao.getAllSuspend().map { it.courseId }

                updateInDb(courses)

                return@authenticatedOrReturn2 if (originalCourses.isEmpty() || originalCourses.containsAll(courses.map { it.courseId })) {
                    ResponseResult.Authenticated
                } else {
                    ResponseResult.AuthenticatedWithResult(TimetableDifference(context.getString(R.string.new_timetable)))
                }
            }
        } ?: ResponseResult.NetworkError
    }

    class TimetableDifference(private val message: String) : Difference {
        override fun parseMessage(): String = message
    }

    suspend fun deleteAll() = timetableDao.deleteAll()

    private fun parseResponse(response: String) = Parser.getSchedule(response) ?: Schedule(emptyList())

    private fun parseCourses(courses: Schedule) = courses.periodicLessons?.map {
        TimetableItem(
            0,
            it.courseId,
            it.courseName,
            it.room,
            it.teachers?.first()?.fullName ?: "",
            it.courseCode,
            it.dayOfWeek.toInt(),
            it.startTime,
            it.endTime,
            it.isSeminar.toBoolean()
        )
    } ?: emptyList()

    private suspend fun updateInDb(courses: List<TimetableItem>) = timetableDao.update(courses)

    fun setDay(position: Int) {
        //actualDay = position % dayNames.size
        val realPosition = if (position % 7 == 0) {
            dayNames.size - 1
        } else {
            (position % 7) - 1
        }
        dayNamesLiveData.postValue(dayNames[realPosition])
    }

    private fun mapCourses(
        cours: List<TimetableItem>
    ): List<MutableList<TimetableItem>> {
        val week = cours.groupBy { it.dayOfWeek }.values.toList()
        val realWeek = Array<MutableList<TimetableItem>>(7) { mutableListOf() }
        week.forEach {
            realWeek[it.first().dayOfWeek - 1] =
                it.sortedBy { it.startTime }.toMutableList()
        }

        run days@{
            realWeek.forEach { day ->
                day.forEachIndexed { index, item ->
                    if (getStartTimeFromCourse(item).isBeforeNow && getEndTimeFromCourse(item).isAfterNow) {
                        day[index] = item.actual()
                        return@days
                    }
                }
            }
        }


        return realWeek.toList()
    }

    private fun getStartTimeFromCourse(item: TimetableItem): DateTime {
        return DateTime.now().withHourOfDay(
            item.startTime.substringBefore(
                ":"
            ).toInt()
        ).withMinuteOfHour(item.startTime.substringAfter(":").toInt()).withDayOfWeek(item.dayOfWeek)
    }

    private fun getEndTimeFromCourse(item: TimetableItem): DateTime {
        return DateTime.now().withHourOfDay(
            item.endTime.substringBefore(
                ":"
            ).toInt()
        ).withMinuteOfHour(item.endTime.substringAfter(":").toInt()).withDayOfWeek(item.dayOfWeek)
    }

}