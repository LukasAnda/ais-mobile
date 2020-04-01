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

package com.lukasanda.aismobile.ui.main.subjects

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lukasanda.aismobile.data.repository.CourseRepository
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel
import kotlin.math.max

class SubjectsViewModel(
    private val courseRepository: CourseRepository,
    private val handle: SavedStateHandle
) : BaseViewModel(handle) {

    override fun logToCrashlytics(e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    fun courses() = courseRepository.get()

    val semesters = mutableListOf<String>()

    fun getSemesterName(position: Int): String {
        if (semesters.isEmpty()) return ""
        val realPosition = if (position % semesters.size == 0) {
            semesters.size - 1
        } else {
            (position % semesters.size) - 1
        }
        return if (semesters.isNotEmpty()) semesters[realPosition] else ""
    }

    fun setPage(position: Int) {
        handle.set(PAGE, position)
    }

    fun getPage(): Int = handle.get<Int>(PAGE) ?: max(0, semesters.size - 1)

    companion object {
        const val PAGE = "page"
    }
}