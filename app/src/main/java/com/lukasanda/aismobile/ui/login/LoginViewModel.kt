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

package com.lukasanda.aismobile.ui.login

import android.util.Log
import com.lukasanda.aismobile.core.BaseViewModel
import com.lukasanda.aismobile.data.db.dao.SessionDao
import com.lukasanda.aismobile.data.db.entity.Session
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.extensions.with
import com.lukasanda.aismobile.util.NotNullMutableLiveData
import com.lukasanda.aismobile.util.getSessionId
import com.lukasanda.aismobile.util.ioThread
import org.joda.time.DateTime

class LoginViewModel(private val service: AISApi, private val sessionDao: SessionDao) : BaseViewModel() {

    private val _refreshing = NotNullMutableLiveData(false)
    val refreshing: NotNullMutableLiveData<Boolean>
        get() = _refreshing

    fun login(name: String, password: String) {
        addToDisposable(sessionDao.findLatest()
            .with()
            .doOnSubscribe { _refreshing.value = true }
            .subscribe({
                if (it != null && DateTime.now().isBefore(it.validUntil)) {
                    println(it.key)
                    _refreshing.value = false
                    //We can safely continue
                } else {
                    requestNewCookie(name, password)
                }
            }, {
                requestNewCookie(name, password)
            })
        )

    }

    private fun requestNewCookie(name: String, password: String) {
        addToDisposable(service.login(login = name, password = password)
            .with()
            .doOnSubscribe { _refreshing.value = true }
            .doOnSuccess { _refreshing.value = false }
            .doOnError { _refreshing.value = false }
            .subscribe({
                val cookies = it.headers().get("Set-Cookie") ?: ""
                if (cookies.isNotEmpty()) {
                    val session = Session(key = getSessionId(cookies), validUntil = DateTime.now().plusDays(1))
                    ioThread {
                        sessionDao.insert(session)
                    }
                }
            }, {
                Log.e("TAG", "network error", it)
            })
        )
    }

}