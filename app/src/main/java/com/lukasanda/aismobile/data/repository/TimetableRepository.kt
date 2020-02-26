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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.TimetableDao
import com.lukasanda.aismobile.data.db.entity.TimetableItem
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.HTTPException
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.util.authenticatedOrThrow
import com.snakydesign.livedataextensions.map
import org.joda.time.DateTime
import sk.lukasanda.dataprovider.Parser

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
        var pageToSelect = Int.MAX_VALUE / 2
        while (pageToSelect % dayNames.size != actualDay) {
            pageToSelect++
        }
        return pageToSelect
    }

    fun get() = timetableDao.getAll().map { mapCourses(it) }

    @Throws(AuthException::class, HTTPException::class)
    suspend fun update() {
        val scheduleResponse =
            aisApi.schedule("1?zobraz=1;format=json;rozvrh_student=${prefs.id}")
                .authenticatedOrThrow()
        saveCourses(scheduleResponse)
    }

    private suspend fun saveCourses(response: String) {
        val schedule =
            Parser.getSchedule(response) ?: return

        val courses = schedule.periodicLessons?.map {
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
        }

        courses?.let { timetableDao.update(it) }
    }

    fun setDay(position: Int) {
        actualDay = position % dayNames.size
        dayNamesLiveData.postValue(dayNames[actualDay])
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
                    if (actualDay == 7) {
                        //Special case for sunday
                        day[index] = item.next()
                        return@days
                    }
                    if (getTimeFromCourse(item).isAfterNow) {
                        day[index] = item.next()
                        return@days
                    }
                }
            }
        }


        return realWeek.toList()
    }

    private fun getTimeFromCourse(item: TimetableItem): DateTime {
        return DateTime.now().withHourOfDay(
            item.startTime.substringBefore(
                ":"
            ).toInt()
        ).withMinuteOfHour(item.startTime.substringAfter(":").toInt()).withDayOfWeek(item.dayOfWeek)
    }

}