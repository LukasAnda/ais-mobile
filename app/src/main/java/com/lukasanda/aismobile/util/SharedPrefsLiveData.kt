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

package com.lukasanda.aismobile.util

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import kotlin.reflect.KClass

class SharedPrefsLiveData<T : Any>(
    private val preferences: SharedPreferences,
    private val key: String,
    private val type: KClass<T>
) :
    LiveData<T>() {
    private val listener =
        SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
            if (key == this.key) {
                value = when (type) {
                    String::class -> preferences.getString(key, "") as T
                    Boolean::class -> preferences.getBoolean(key, true) as T
                    Float::class -> preferences.getFloat(key, 0f) as T
                    Int::class -> preferences.getInt(key, 0) as T
                    Long::class -> preferences.getLong(key, 0L) as T
                    else -> error("Invalid type")
                }
            }
        }

    override fun onActive() {
        super.onActive()
        value = when (type) {
            String::class -> preferences.getString(key, "") as T
            Boolean::class -> preferences.getBoolean(key, true) as T
            Float::class -> preferences.getFloat(key, 0f) as T
            Int::class -> preferences.getInt(key, 0) as T
            Long::class -> preferences.getLong(key, 0L) as T
            else -> error("Invalid type")
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}

inline fun <reified T : Any> createLiveData(preferences: SharedPreferences, key: String) =
    SharedPrefsLiveData(preferences, key, T::class)