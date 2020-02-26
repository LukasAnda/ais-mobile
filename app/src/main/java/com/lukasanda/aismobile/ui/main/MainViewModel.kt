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

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.lukasanda.aismobile.data.db.dao.ProfileDao
import com.lukasanda.aismobile.data.db.entity.Profile
import sk.lukasanda.base.ui.viewmodel.BaseViewModel

class MainViewModel(
    private val profileDao: ProfileDao,
    private val handle: SavedStateHandle
) :
    BaseViewModel(handle) {
    fun profile(): LiveData<Profile?> = profileDao.getProfile()

}