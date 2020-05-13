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

package com.lukasanda.aismobile.ui.main.peopleDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.ProfileInfo
import com.lukasanda.aismobile.data.db.entity.ProfileInfoItem
import com.lukasanda.aismobile.data.db.entity.Suggestion
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel
import com.lukasanda.aismobile.util.authenticatedOrThrow2
import com.lukasanda.dataprovider.Parser
import kotlinx.coroutines.launch

class PeopleDetailViewModel(private val handle: SavedStateHandle, private val aisApi: AISApi) : BaseViewModel(handle) {
    private val _profileInfo = MutableLiveData<ProfileInfo>()

    fun profileInfo(): LiveData<ProfileInfo> = _profileInfo

    override fun logToCrashlytics(e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    fun getProfileInfo(suggestion: Suggestion) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val profileResponse = aisApi.getPersonInfo(suggestion.id).authenticatedOrThrow2()
            val emails = Parser.getProfileEmails(profileResponse).toSet()

            val returnList = mutableListOf<Pair<Int, String>>()
            returnList.add(Pair(R.string.profile_name, suggestion.name))
            returnList.add(Pair(R.string.profile_id, suggestion.id))
            emails.forEach {
                returnList.add(Pair(R.string.profile_email, it))
            }

            _profileInfo.postValue(ProfileInfo(suggestion.id, suggestion.name, returnList.map { ProfileInfoItem(it) }))
        }
    }
}