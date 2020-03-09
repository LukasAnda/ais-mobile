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

package com.lukasanda.aismobile.di

import androidx.lifecycle.SavedStateHandle
import com.lukasanda.aismobile.ui.login.LoginViewModel
import com.lukasanda.aismobile.ui.main.MainViewModel
import com.lukasanda.aismobile.ui.main.composeEmail.ComposeEmailViewModel
import com.lukasanda.aismobile.ui.main.documents.DocumentsViewModel
import com.lukasanda.aismobile.ui.main.email.EmailViewModel
import com.lukasanda.aismobile.ui.main.emailDetail.EmailDetailViewModel
import com.lukasanda.aismobile.ui.main.loading.LoadingViewModel
import com.lukasanda.aismobile.ui.main.subjectDetail.SubjectDetailViewModel
import com.lukasanda.aismobile.ui.main.subjects.SubjectsViewModel
import com.lukasanda.aismobile.ui.main.timetable.TimetableViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { (handle: SavedStateHandle) -> LoginViewModel(get(), get(), get(), get(), handle) }
    viewModel { (handle: SavedStateHandle) -> MainViewModel(get(), get(), get(), get(), get(), get(), get(), get(), handle) }
    viewModel { (handle: SavedStateHandle) -> SubjectsViewModel(get(), handle) }
    viewModel { (handle: SavedStateHandle) -> TimetableViewModel(get(), get(), handle) }
    viewModel { (handle: SavedStateHandle) -> EmailViewModel(get(), handle) }
    viewModel { (handle: SavedStateHandle) -> EmailDetailViewModel(get(), handle) }
    viewModel { (handle: SavedStateHandle) -> ComposeEmailViewModel(get(), handle) }
    viewModel { (handle: SavedStateHandle) -> SubjectDetailViewModel(get(), handle) }
    viewModel { (handle: SavedStateHandle) -> DocumentsViewModel(get(), handle) }
    viewModel { (handle: SavedStateHandle) -> LoadingViewModel(handle, get()) }
}