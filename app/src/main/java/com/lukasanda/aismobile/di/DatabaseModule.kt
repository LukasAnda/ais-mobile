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

import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val roomModule = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single(createdAtStart = false) { get<AppDatabase>().getCourseDao() }
    single(createdAtStart = false) { get<AppDatabase>().getProfileDao() }
    single(createdAtStart = false) { get<AppDatabase>().getTimetableDao() }
    single(createdAtStart = false) { get<AppDatabase>().getEmailDao() }
}

val prefsModule = module {
    single { Prefs(androidApplication()) }
}