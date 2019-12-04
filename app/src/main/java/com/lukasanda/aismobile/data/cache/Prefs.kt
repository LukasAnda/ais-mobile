/*
 * Copyright 2019 Lukáš Anda. All rights reserved.
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

package com.lukasanda.aismobile.data.cache

import android.content.Context
import android.content.SharedPreferences
import com.lukasanda.aismobile.BuildConfig
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class Prefs(context: Context) {
    private val PREFS_FILENAME = "${BuildConfig.APPLICATION_ID}.prefs"
    private val SESSION_EXPIRATION = "session_expiration_date"
    private val COOKIES = "cookies"
    private val USERNAME = "username"
    private val PASSWORD = "password"
    private val AISID = "aisid"

    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var sessionCookie: String
        get() = prefs.getString(COOKIES, "") ?: ""
        set(value) = prefs.edit().putString(COOKIES, value).apply()

    var expiration: DateTime
        get() = DateTime.parse(
            prefs.getString(
                SESSION_EXPIRATION,
                DateTime.now().minusDays(1).toString(DateTimeFormat.forPattern("dd.MM.yyyy hh:mm"))
            ), DateTimeFormat.forPattern("dd.MM.yyyy hh:mm")
        )
        set(value) = prefs.edit().putString(
            SESSION_EXPIRATION,
            value.toString(DateTimeFormat.forPattern("dd.MM.yyyy hh:mm"))
        ).apply()

    var username: String
        get() = prefs.getString(USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(USERNAME, value).apply()

    var password: String
        get() = prefs.getString(PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(PASSWORD, value).apply()

    var id: Int
        get() = prefs.getInt(AISID, 0)
        set(value) = prefs.edit().putInt(AISID, value).apply()
}