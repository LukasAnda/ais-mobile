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
import android.util.Log
import androidx.annotation.StringRes
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.cache.SafePrefs
import com.lukasanda.aismobile.data.db.dao.ProfileDao
import com.lukasanda.aismobile.data.db.entity.Profile
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.data.repository.CourseRepository
import com.lukasanda.aismobile.data.repository.EmailRepository
import com.lukasanda.aismobile.data.repository.TimetableRepository
import com.lukasanda.aismobile.util.*
import com.lukasanda.dataprovider.Parser
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response

private const val NOTIFICATION_DEBUG = 0
private const val NOTIFICATION_TIMETABLE = 1
private const val NOTIFICATION_EMAIL = 2

class SyncCoroutineWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val timetableRepository: TimetableRepository by inject()
    private val courseRepository: CourseRepository by inject()
    private val service: AISApi by inject()
    private val prefs: Prefs by inject()
    private val safePrefs: SafePrefs by inject()
    private val profileDao: ProfileDao by inject()

    private val emailRepository: EmailRepository by inject()


    override suspend fun doWork(): Result {
        if (runAttemptCount > 3) {
            startSingleWorker(applicationContext)
            return Result.failure()
        }

        Log.d("TAG", "Starting worker")

//        if (BuildConfig.DEBUG) {
//            sendNotification(applicationContext, "Zapinam workera", NOTIFICATION_DEBUG)
//        }

        setProgress(workDataOf(PROGRESS to 0, PROGRESS_MESSAGE to R.string.downloading_timetable))
        runCatching {
            val result = timetableRepository.update().throwOnAuthError()
            if (result is ResponseResult.AuthenticatedWithResult<*>) {
                sendNotification(applicationContext, result.result.parseMessage(), NOTIFICATION_TIMETABLE)
            }
        }.getOrElse {
            if (it is AuthException) {
                reLogin()
                return Result.retry()
            } else {
                FirebaseCrashlytics.getInstance().recordException(it)
                it.printStackTrace()
                ResponseResult.NetworkError
            }
        }
        Log.d("TAG", "Timetable downloaded")
        delay(1000)

        setProgress(workDataOf(PROGRESS to 25, PROGRESS_MESSAGE to R.string.downloading_profile))
        runCatching {
            service.educationInfo().authenticatedOrReturn { educationResponse ->
                delay(1000)
                return@authenticatedOrReturn service.wifiInfo().authenticatedOrReturn { wifiResponse ->
                    saveProfile(educationResponse, wifiResponse)
                    ResponseResult.Authenticated
                }
            }.throwOnAuthError()
        }.getOrElse {
            if (it is AuthException) {
                reLogin()
                return Result.retry()
            } else {
                FirebaseCrashlytics.getInstance().recordException(it)
                it.printStackTrace()
                ResponseResult.NetworkError
            }
        }

        val previousEmailCount = prefs.newEmailCount
        setProgress(workDataOf(PROGRESS to 50, PROGRESS_MESSAGE to R.string.downloading_emails))
        runCatching { emailRepository.update().throwOnAuthError() }.getOrElse {
            if (it is AuthException) {
                reLogin()
                return Result.retry()
            } else {
                FirebaseCrashlytics.getInstance().recordException(it)
                it.printStackTrace()
                ResponseResult.NetworkError
            }
        }

        val newEmailCount = prefs.newEmailCount

        if (newEmailCount != previousEmailCount) {
            val text = applicationContext.resources.getQuantityString(R.plurals.new_emails_notficiation, newEmailCount, newEmailCount)
            sendNotification(applicationContext, text, NOTIFICATION_EMAIL)
        }

        Log.d("TAG", "Downloading semesters")

        //setProgress(workDataOf(PROGRESS to 75, PROGRESS_MESSAGE to R.string.downloading_courses))
        runCatching {
            var actualSemesters = 0
            courseRepository.update(object : CourseRepository.CourseUpdateHandler {
                override suspend fun onSemesterCount(semesterCount: Int) {
                    actualSemesters = semesterCount
                }

                override suspend fun onSemesterStartDownloading(semesterIndex: Int, semesterName: String) {
                    if (actualSemesters > 0) {
                        setProgress(
                            workDataOf(
                                PROGRESS to 75 + semesterIndex * (25 / actualSemesters),
                                PROGRESS_SPECIAL_MESSAGE to "Sťahujem dokumenty a údaje o predmetoch pre semester: $semesterName "
                            )
                        )
                    }
                }

            }).throwOnAuthError()
        }.getOrElse {
            it.printStackTrace()
            if (it is AuthException) {
                reLogin()
                return Result.retry()
            } else {
                FirebaseCrashlytics.getInstance().recordException(it)
                it.printStackTrace()
                ResponseResult.NetworkError
            }
        }

        Log.d("TAG", "Semesters downloaded")

        setProgress(workDataOf(PROGRESS to 100, PROGRESS_MESSAGE to R.string.downloading_complete))

        delay(prefs.updateInterval.toLong() * 60 * 1000)

        startSingleWorker(applicationContext)
        return Result.success()
    }

    private suspend fun reLogin() {
        val response = service.login(login = safePrefs.email, password = safePrefs.password)
        saveCookie(safePrefs.email, safePrefs.password, response)
    }

    private fun saveCookie(
        name: String,
        password: String,
        response: Response<ResponseBody>
    ): Boolean {
        val cookies = response.headers().get("Set-Cookie") ?: return false

        safePrefs.sessionCookie = getSessionId(cookies)
        prefs.expiration = DateTime.now().plusDays(1)

        safePrefs.email = name
        safePrefs.password = password

        return true
    }

    private suspend fun saveProfile(educationResponse: String, wifiResponse: String) {

        val aisId = Parser.getId(educationResponse) ?: return
        prefs.id = aisId

        val wifiInfo = Parser.getWifiInfo(wifiResponse) ?: return

        profileDao.update(Profile(aisId, wifiInfo.username, wifiInfo.password))
    }

    companion object {
        const val PROGRESS = "Progress"
        const val PROGRESS_MESSAGE = "Progress Message"
        const val PROGRESS_SPECIAL_MESSAGE = "Progress Special Message"
    }

}

data class Progress(val value: Int, @StringRes val text: Int)

class AuthException : Exception()
class HTTPException : Exception()