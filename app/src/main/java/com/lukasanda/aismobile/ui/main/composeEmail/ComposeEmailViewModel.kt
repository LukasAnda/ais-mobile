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

package com.lukasanda.aismobile.ui.main.composeEmail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasanda.aismobile.data.repository.EmailRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sk.lukasanda.base.ui.viewmodel.BaseViewModel
import sk.lukasanda.dataprovider.data.Suggestion

class ComposeEmailViewModel(
    private val emailRepository: EmailRepository,
    private val handle: SavedStateHandle
) : BaseViewModel(handle) {

    private val downloadJobs = mutableListOf<Job>()

    private val _sendMailLiveData = MutableLiveData<EmailSendState>(EmailSendState.Unknown)
    fun sentMailState(): LiveData<EmailSendState> = _sendMailLiveData

    private val _suggestionsLiveData = MutableLiveData<List<Suggestion>>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        run {
            t.printStackTrace()
        }
    }

    fun getSuggestions(query: String) {
        cancelJobs()
        val job = submitSuggestionRequest(query)
        downloadJobs.add(job)
    }

    fun cancelJobs() {
        downloadJobs.forEach {
            it.cancel()
        }
        downloadJobs.clear()
    }

    fun suggestions(): LiveData<List<Suggestion>> = _suggestionsLiveData

    fun sendMail(to: String, subject: String, message: String) =
        viewModelScope.launch(coroutineExceptionHandler) {
            val success = emailRepository.sendMail(
                to,
                subject,
                message
            )
            if (success) {
                _sendMailLiveData.postValue(EmailSendState.Success)
            } else {
                _sendMailLiveData.postValue(EmailSendState.Fail)
            }
        }

    private fun submitSuggestionRequest(query: String) =
        viewModelScope.launch(coroutineExceptionHandler) {
            delay(500)
            _suggestionsLiveData.postValue(emailRepository.getSuggestions(query))
        }

    enum class EmailSendState {
        Unknown, Success, Fail
    }
}