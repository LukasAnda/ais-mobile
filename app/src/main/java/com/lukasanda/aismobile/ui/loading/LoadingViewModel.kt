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

package com.lukasanda.aismobile.ui.loading

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel

class LoadingViewModel(handle: SavedStateHandle, private val context: Context) : BaseViewModel(handle) {
    override fun logToCrashlytics(e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    fun getWorkLiveData() = WorkManager.getInstance(context).getWorkInfosByTagLiveData("Sync")
}