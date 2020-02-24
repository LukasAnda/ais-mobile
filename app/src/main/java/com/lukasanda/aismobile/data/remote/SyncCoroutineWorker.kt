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

package com.lukasanda.aismobile.data.remote

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.ProfileDao
import com.lukasanda.aismobile.data.db.entity.Profile
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.data.repository.CourseRepository
import com.lukasanda.aismobile.data.repository.EmailRepository
import com.lukasanda.aismobile.data.repository.TimetableRepository
import com.lukasanda.aismobile.util.authenticatedOrThrow
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

    private val timetableRepository: TimetableRepository by inject()
    private val courseRepository: CourseRepository by inject()
    private val service: AISApi by inject()
    private val prefs: Prefs by inject()
    private val profileDao: ProfileDao by inject()

    private val emailRepository: EmailRepository by inject()


    override suspend fun doWork(): Result {
        return try {
            if (runAttemptCount > 3) {
                return Result.failure()
            }

            timetableRepository.update()
            delay(1000)

            val educationResponse = service.educationInfo().authenticatedOrThrow()
            delay(1000)
            val wifiResponse = service.wifiInfo().authenticatedOrThrow()
            saveProfile(educationResponse, wifiResponse)

//            courseRepository.update()

            //emailRepository.update()

            Result.success()
        } catch (e: Exception) {
            if (e is AuthException) {
                val response = service.login(login = prefs.username, password = prefs.password)
                saveCookie(prefs.username, prefs.password, response)
            }

            println(e.message)
            println(e.toString())
            e.printStackTrace()

            Result.retry()
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

    private suspend fun saveProfile(educationResponse: String, wifiResponse: String) {

        val aisId = Parser.getId(educationResponse) ?: return
        prefs.id = aisId

        val wifiInfo = Parser.getWifiInfo(wifiResponse) ?: return

        profileDao.update(Profile(aisId, wifiInfo.username, wifiInfo.password))
    }

}

class AuthException : Exception()
class HTTPException : Exception()