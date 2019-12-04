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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasanda.aismobile.core.BaseViewModel
import com.lukasanda.aismobile.core.State
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.extensions.with
import com.lukasanda.aismobile.util.NotNullMutableLiveData
import com.lukasanda.aismobile.util.getSessionId
import okhttp3.ResponseBody
import org.joda.time.DateTime
import retrofit2.Response
import sk.lukasanda.dataprovider.Parser

class LoginViewModel(private val service: AISApi, private val prefs: Prefs) : BaseViewModel() {
    private val _state = MutableLiveData<State<Int, ErrorState>>()
    val state: LiveData<State<Int, ErrorState>> = _state


    fun login(name: String, password: String) {
        _state.postValue(State.Loading)

        if (prefs.expiration.isBeforeNow || name != prefs.username || password != prefs.password) {
            //Need new token
            requestNewCookie(name, password)
        } else {
            _state.postValue(State.Success(1))
        }
    }

    private fun requestNewCookie(name: String, password: String) {
        addToDisposable(service.login(login = name, password = password)
            .with()
            .subscribe({
                if(it.code() == 302){
                    if(saveCookie(name, password, it)){
                        _state.postValue(State.Success(1))
                    } else {
                        _state.postValue(State.Failure(ErrorState.Auth))
                    }
                } else {
                    _state.postValue(State.Failure(ErrorState.Auth))
                }

            }, {
                _state.postValue(State.Failure(ErrorState.Network))
                Log.e("TAG", "network error", it)
            })
        )
    }

    private fun saveCookie(name: String, password: String, response: Response<ResponseBody>): Boolean{
        val cookies = response.headers().get("Set-Cookie") ?: return false

        prefs.sessionCookie = getSessionId(cookies)
        prefs.expiration = DateTime.now().plusDays(1)

        prefs.username = name
        prefs.password = password

        return true
    }

    enum class ErrorState{
        Auth, Network
    }
}