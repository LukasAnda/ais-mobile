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

package com.lukasanda.aismobile.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasanda.aismobile.core.State
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.cache.SafePrefs
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel
import com.lukasanda.aismobile.util.getSessionId
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.joda.time.DateTime
import retrofit2.Response

class LoginViewModel(
    private val service: AISApi,
    private val prefs: Prefs,
    private val safePrefs: SafePrefs,
    private val context: Application,
    private val handle: SavedStateHandle
) : BaseViewModel(handle) {
    private val _state = MutableLiveData<State<Int, ErrorState>>()
    val state: LiveData<State<Int, ErrorState>> = _state


    fun login(name: String, password: String) {
        _state.postValue(State.Loading)

        if (prefs.expiration.isBeforeNow || name != safePrefs.email || password != safePrefs.password) {
            //Need new token
            requestNewCookie(name, password)
        } else {
            _state.postValue(State.Success(1))
        }
    }

    private fun requestNewCookie(name: String, password: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                val response = service.login(login = name, password = password)
                if (response.code() == 302) {
                    if (saveCookie(name, password, response)) {
                        _state.postValue(State.Success(1))
                    } else {
                        _state.postValue(State.Failure(ErrorState.Auth))
                    }
                } else {
                    _state.postValue(State.Failure(ErrorState.Auth))
                }
            }.onFailure {
                _state.postValue(State.Failure(ErrorState.Network))
                Log.e("TAG", "network error", it)
            }
        }
    }

    private fun saveCookie(
        name: String,
        password: String,
        response: Response<ResponseBody>
    ): Boolean {
        val cookies = response.headers().get("Set-Cookie") ?: return false

        prefs.expiration = DateTime.now().plusDays(1)

        safePrefs.sessionCookie = getSessionId(cookies)
        safePrefs.email = name
        safePrefs.password = password

        return true
    }

    enum class ErrorState {
        Auth, Network
    }
}