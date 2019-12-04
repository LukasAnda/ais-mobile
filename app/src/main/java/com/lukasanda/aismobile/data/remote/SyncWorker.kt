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

package com.lukasanda.aismobile.data.remote

import android.content.Context
import android.util.Log
import androidx.work.*
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.CourseDao
import com.lukasanda.aismobile.data.db.dao.ProfileDao
import com.lukasanda.aismobile.data.db.entity.Course
import com.lukasanda.aismobile.data.db.entity.Profile
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.extensions.singleZip
import com.lukasanda.aismobile.extensions.with
import com.lukasanda.aismobile.util.getSessionId
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response
import sk.lukasanda.dataprovider.Parser
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    workerParameters: WorkerParameters
) : RxWorker(context, workerParameters), KoinComponent {

    private val courseDao: CourseDao by inject()
    private val service: AISApi by inject()
    private val prefs: Prefs by inject()
    private val profileDao: ProfileDao by inject()


    override fun createWork(): Single<Result> {
        return getSchedule()
    }

    private fun getSchedule(): Single<Result> {
        return service.schedule("1?zobraz=1;format=json;rozvrh_student=${prefs.id}")
            .delay(1, TimeUnit.SECONDS).flatMap {
                if (it.code() == 403 || !it.isSuccessful) {
                    authenticateAndRetry()
                } else {
                    saveCourses(it).flatMap {
                        Log.d("TAG", "Got here")
                        getProfile()
                    }
                }
            }
    }

    private fun getProfile(): Single<Result> {
        return listOf(
            service.educationInfo(),
            service.wifiInfo()
        ).singleZip().delay(1, TimeUnit.SECONDS).flatMap {
            if (it.any { it.code() == 403 || !it.isSuccessful }) {
                authenticateAndRetry()
            } else {
                saveProfile(it).flatMap { Single.just(Result.success()) }
            }
        }
    }

    private fun authenticateAndRetry(): Single<Result> {
        return service.login(login = prefs.username, password = prefs.password).doOnSuccess {
            saveCookie(prefs.username, prefs.password, it)
        }.flatMap {
            Single.just(Result.retry())
        }
    }

    private fun saveCourses(response: Response<ResponseBody>): Single<Unit> {
        val schedule =
            Parser.getSchedule(response.body()?.string() ?: "") ?: return Single.fromCallable {
                {}()
            }

        val courses = schedule.periodicLessons?.map {
            Course(
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
        } ?: return Single.fromCallable {
            {}()
        }

        return courseDao.deleteAll().andThen(courseDao.insertAll(courses)).flatMap {
            Single.fromCallable {
                {}()
            }
        }
    }

    private fun saveProfile(responses: List<Response<ResponseBody>>): Single<Unit> {
        val items = responses.filter { it.isSuccessful }.map { it.body() }.filterNotNull()
            .map { it.string() }

        val aisId = Parser.getId(items[0]) ?: return Single.fromCallable {
            {}()
        }
        prefs.id = aisId

        val wifiInfo = Parser.getWifiInfo(items[1]) ?: return Single.fromCallable {
            {}()
        }

        Log.d("TAG", "AIS id: $aisId wifiInfo: $wifiInfo")

        return profileDao.deleteAll().andThen(profileDao.insertProfile(Profile(aisId, wifiInfo.username, wifiInfo.password))).flatMap {
            Single.fromCallable {
                {}()
            }
        }
    }

    private fun saveCookie(
        name: String,
        password: String,
        response: Response<ResponseBody>
    ): Boolean {
        val cookies = response.headers().get("Set-Cookie") ?: return false

        prefs.sessionCookie = getSessionId(cookies)
        prefs.expiration = DateTime.now().plusDays(1)

        prefs.username = name
        prefs.password = password

        return true
    }
}