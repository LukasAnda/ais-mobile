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

package com.lukasanda.aismobile.ui.main

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.cache.SafePrefs
import com.lukasanda.aismobile.data.db.dao.ProfileDao
import com.lukasanda.aismobile.data.db.entity.Document
import com.lukasanda.aismobile.data.db.entity.Profile
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.data.repository.CourseRepository
import com.lukasanda.aismobile.data.repository.DocumentRepository
import com.lukasanda.aismobile.data.repository.EmailRepository
import com.lukasanda.aismobile.data.repository.TimetableRepository
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel
import com.lukasanda.aismobile.util.getSessionId
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Func
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import retrofit2.Response
import java.io.File

class MainViewModel(
    private val profileDao: ProfileDao,
    private val safePrefs: SafePrefs,
    private val prefs: Prefs,
    private val service: AISApi,
    private val emailRepository: EmailRepository,
    private val courseRepository: CourseRepository,
    private val documentRepository: DocumentRepository,
    private val timetableRepository: TimetableRepository,
    private val handle: SavedStateHandle

) :
    BaseViewModel(handle) {

    override fun logToCrashlytics(e: Throwable) {
        if (e is AuthException) {
            viewModelScope.launch(coroutineExceptionHandler) {
                reLogin()
            }
        } else {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private suspend fun reLogin() {
        val response = service.login(login = safePrefs.email, password = safePrefs.password)
        saveCookie(safePrefs.email, safePrefs.password, response)
    }

    private fun saveCookie(
        name: String,
        password: String,
        response: Response<String>
    ): Boolean {
        val cookies = response.headers().get("Set-Cookie") ?: return false

        safePrefs.sessionCookie = getSessionId(cookies)
        prefs.expiration = DateTime.now().plusDays(1)

        safePrefs.email = name
        safePrefs.password = password

        return true
    }

    private val _fileHandle: MutableLiveData<Result<Pair<File, String>>?> = MutableLiveData<Result<Pair<File, String>>?>()
    fun fileHandle(): LiveData<Result<Pair<File, String>>?> = _fileHandle

    fun clearFileHandle() {
        _fileHandle.postValue(null)
    }

    private val _logoutLiveData = MutableLiveData<Boolean>()
    fun logoutData(): LiveData<Boolean> = _logoutLiveData

    fun profile(): LiveData<Profile?> = profileDao.getProfile()

    fun downloadFile(document: Document, context: Context) {
        Log.d("TAG", "Downloading file $document")

        viewModelScope.launch(coroutineExceptionHandler) {
            val infoResponse = service.getDocumentInfo("${document.parentFolderId};download=${document.id};z=1")
            val fileName = infoResponse.headers().get("Content-Disposition")?.substringAfter("filename=\"")?.substringBefore("\"") ?: "${document.name}.pdf"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

            val config = FetchConfiguration.Builder(context).setDownloadConcurrentLimit(3).build()
            val fetch = Fetch.Impl.getInstance(config)
            val request = Request("https://is.stuba.sk/auth/dok_server/slozka.pl?id=${document.parentFolderId};download=${document.id};z=1", file.path).apply {
                priority = Priority.HIGH
                networkType = NetworkType.ALL
                addHeader("Cookie", safePrefs.sessionCookie)
            }
            fetch.enqueue(request, func = Func {
                _fileHandle.postValue(Result.success(Pair(File(it.file), document.mimeType)))
//                _fileHandle.postValue(Pair(File(it.file), document.mimeType))
            }, func2 = Func {
                it.throwable?.let { error ->
                    _fileHandle.postValue(Result.failure(error))
                    error.printStackTrace()
                }
            })
        }
    }

    fun logout() {
        viewModelScope.launch(coroutineExceptionHandler) {
            emailRepository.deleteAll()
            courseRepository.deleteAll()
            documentRepository.deleteAll()
            timetableRepository.deleteAll()

            safePrefs.email = ""
            safePrefs.password = ""
            safePrefs.sessionCookie = ""

            prefs.nukeAll()

            _logoutLiveData.postValue(true)
        }
    }

}