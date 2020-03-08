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

package com.lukasanda.aismobile.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

abstract class BaseViewModel(private val handle: SavedStateHandle) : ViewModel() {

    fun setHandleValues(bundle: Bundle) {
        bundle.keySet().forEach { key ->
            handle.set(key, bundle[key])
        }
    }

    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        run {
            t.printStackTrace()
            logToCrashlytics(t)
        }
    }

    abstract fun logToCrashlytics(e: Throwable)

    fun <T> getValue(key: String): T? = handle[key]
    fun <T> setValue(key: String, value: T?) = handle.set(key, value)
}