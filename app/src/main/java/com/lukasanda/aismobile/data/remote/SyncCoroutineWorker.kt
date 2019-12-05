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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.CourseDao
import com.lukasanda.aismobile.data.db.dao.ProfileDao
import com.lukasanda.aismobile.data.db.entity.Course
import com.lukasanda.aismobile.data.db.entity.Profile
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.util.getSessionId
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response
import sk.lukasanda.dataprovider.Parser

class SyncCoroutineWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val courseDao: CourseDao by inject()
    private val service: AISApi by inject()
    private val prefs: Prefs by inject()
    private val profileDao: ProfileDao by inject()


    override suspend fun doWork(): Result {
        return try {

            if (runAttemptCount > 3) {
                return Result.failure()
            }

            val scheduleResponse =
                service.schedule("1?zobraz=1;format=json;rozvrh_student=${prefs.id}")
                    .authenticatedOrThrow()
            saveCourses(scheduleResponse)
            delay(1000)

            val educationResponse = service.educationInfo().authenticatedOrThrow()
            delay(1000)
            val wifiResponse = service.wifiInfo().authenticatedOrThrow()
            saveProfile(listOf(educationResponse, wifiResponse))
            Result.success()
        } catch (e: Exception) {
            if (e is AuthException) {
                val response = service.login(login = prefs.username, password = prefs.password)
                saveCookie(prefs.username, prefs.password, response)
            }

            Result.retry()
        }
    }


    private fun Response<ResponseBody>.authenticatedOrThrow(): Response<ResponseBody> =
        if (this.isSuccessful) this else if (this.code() == 403) throw AuthException() else throw HTTPException()

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

    private suspend fun saveCourses(response: Response<ResponseBody>) {
        val schedule =
            Parser.getSchedule(response.body()?.string() ?: "") ?: return

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
        }

        courses?.let { courseDao.update(it) }
    }

    private suspend fun saveProfile(responses: List<Response<ResponseBody>>) {
        val items = responses.filter { it.isSuccessful }.map { it.body() }.filterNotNull()
            .map { it.string() }

        val aisId = Parser.getId(items[0]) ?: return
        prefs.id = aisId

        val wifiInfo = Parser.getWifiInfo(items[1]) ?: return

        Log.d("TAG", "AIS id: $aisId wifiInfo: $wifiInfo")
        profileDao.insertProfile(Profile(aisId, wifiInfo.username, wifiInfo.password))
    }

}

class AuthException : Exception()
class HTTPException : Exception()