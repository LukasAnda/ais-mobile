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

package com.lukasanda.aismobile.ui.main.timetable

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.repository.TimetableRepository
import sk.lukasanda.base.ui.viewmodel.BaseViewModel

class TimetableViewModel(
    private val context: Application,
    private val timetableRepository: TimetableRepository,
    private val handle: SavedStateHandle
) : BaseViewModel(handle) {
    val days = context.resources.getStringArray(R.array.days).toList()

    fun timetable() = timetableRepository.get()
    fun days(): LiveData<String> = timetableRepository.days()
    fun setDay(position: Int) = timetableRepository.setDay(position)
    fun getCurrentDay() = timetableRepository.getCurrentDay()
    fun getActualDay() = timetableRepository.actualDay
}