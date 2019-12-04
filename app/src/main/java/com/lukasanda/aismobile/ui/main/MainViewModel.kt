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

package com.lukasanda.aismobile.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasanda.aismobile.core.BaseViewModel
import com.lukasanda.aismobile.core.State
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.CourseDao
import com.lukasanda.aismobile.data.db.dao.ProfileDao
import com.lukasanda.aismobile.data.db.entity.Course
import com.lukasanda.aismobile.data.db.entity.Profile
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.extensions.singleZip
import com.lukasanda.aismobile.extensions.with
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Days
import sk.lukasanda.dataprovider.Parser

class MainViewModel(
    private val courseDao: CourseDao,
    private val service: AISApi,
    private val prefs: Prefs,
    private val profileDao: ProfileDao
) :
    BaseViewModel() {

    private val _courses = MutableLiveData<State<List<List<Course>>,Nothing>>()
    private val _days = MutableLiveData<Int>()
    private val _profile = MutableLiveData<Profile>()

    private val allCourses = mutableListOf<Course>()
    internal var actualDay = DateTime.now().dayOfWeek
    val days = listOf("Pondelok", "Utorok", "Streda", "Štvrtok", "Piatok", "Sobota", "Nedeľa")

    fun courses(): LiveData<State<List<List<Course>>,Nothing>> = _courses
    fun days(): LiveData<Int> = _days
    fun profile(): LiveData<Profile> = _profile

    fun getProfile() {
//        launchRx {
            profileDao.getProfile().with().subscribe {
                Log.d("TAG", "Got profile from DB")
                try {
                    _profile.postValue(it.first())
                } catch (e: NoSuchElementException) {

                }
            }
//        }
    }


    fun getCourses() {
        _courses.postValue(State.Loading)
//        launchRx {
            courseDao.getAll().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                allCourses.clear()
                allCourses.addAll(it)
                filterCourses()
                _days.postValue(actualDay - 1)
                Log.d("TAG", "Pulling from db")
            }, {
                Log.e("DB Error", "Error", it)
            })
//        }
    }

    fun nextDay() {
        actualDay += 1

        if (actualDay >= 8) {
            actualDay = 1
        }

        _days.postValue(actualDay - 1)
    }

    fun previousDay() {
        actualDay -= 1

        if (actualDay <= 0) {
            actualDay = 7
        }

        _days.postValue(actualDay - 1)
    }

    fun setDay(day: Int){
        if(day < 0 || day > 6) return
        actualDay = day
        _days.postValue(actualDay)
    }

    private fun filterCourses() {
        val week = allCourses.groupBy { it.dayOfWeek }.values.toList()
        val realWeek = Array<List<Course>>(7) { emptyList() }
        week.forEach {
            realWeek[it.first().dayOfWeek - 1] = it.sortedBy { it.startTime }
        }
        _courses.postValue(State.Success(realWeek.toList()))
    }
}