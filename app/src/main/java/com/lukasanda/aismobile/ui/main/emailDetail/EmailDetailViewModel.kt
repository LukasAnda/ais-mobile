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

package com.lukasanda.aismobile.ui.main.emailDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.data.repository.EmailRepository
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel
import com.lukasanda.dataprovider.data.EmailDetail
import kotlinx.coroutines.launch

class EmailDetailViewModel(
    private val emailRepository: EmailRepository,
    private val handle: SavedStateHandle
) : BaseViewModel(handle) {

    private val _emailDetail = MutableLiveData<EmailDetail?>()
    fun emailDetail(): LiveData<EmailDetail?> = _emailDetail

    fun setEmail(email: Email) {
        handle[EMAIL] = email
    }

    fun getEmail(): Email? = handle[EMAIL]

    override fun logToCrashlytics(e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    fun getEmailDetail(email: Email) = viewModelScope.launch(coroutineExceptionHandler) {
        _emailDetail.postValue(emailRepository.getEmailDetail(email))
    }

    companion object {
        const val EMAIL = "email"
    }
}