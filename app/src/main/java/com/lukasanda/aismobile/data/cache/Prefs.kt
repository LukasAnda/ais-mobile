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

package com.lukasanda.aismobile.data.cache

import android.content.Context
import android.content.SharedPreferences
import com.lukasanda.aismobile.BuildConfig
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class Prefs(context: Context) {
    val PREFS_FILENAME = "${BuildConfig.APPLICATION_ID}.prefs"
    val SESSION_EXPIRATION = "session_expiration_date"
    val COOKIES = "cookies"
    val USERNAME = "username"
    val PASSWORD = "password"
    val AISID = "aisid"
    val SENT_DIRECTORY_ID = "sent_directory_id"
    val NEW_EMAIL_COUNT = "new_email_count"
    val EMAIL_CACHE_EXPIRATION = "email_cache_expiration"
    val FULL_COURSE_CACHE_EXPIRATION = "full_course_cache_expiration"
    val COURSE_CACHE_EXPIRATION = "course_cache_expiration"

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

    var emailExpiration: DateTime
        get() = DateTime.parse(
            prefs.getString(
                EMAIL_CACHE_EXPIRATION,
                DateTime.now().minusDays(1).toString(DateTimeFormat.forPattern("dd.MM.yyyy hh:mm"))
            ), DateTimeFormat.forPattern("dd.MM.yyyy hh:mm")
        )
        set(value) = prefs.edit().putString(
            EMAIL_CACHE_EXPIRATION,
            value.toString(DateTimeFormat.forPattern("dd.MM.yyyy hh:mm"))
        ).apply()

    var courseExpiration: DateTime
        get() = DateTime.parse(
            prefs.getString(
                COURSE_CACHE_EXPIRATION,
                DateTime.now().minusDays(1).toString(DateTimeFormat.forPattern("dd.MM.yyyy hh:mm"))
            ), DateTimeFormat.forPattern("dd.MM.yyyy hh:mm")
        )
        set(value) = prefs.edit().putString(
            COURSE_CACHE_EXPIRATION,
            value.toString(DateTimeFormat.forPattern("dd.MM.yyyy hh:mm"))
        ).apply()

    var fullCourseExpiration: DateTime
        get() = DateTime.parse(
            prefs.getString(
                FULL_COURSE_CACHE_EXPIRATION,
                DateTime.now().minusDays(1).toString(DateTimeFormat.forPattern("dd.MM.yyyy hh:mm"))
            ), DateTimeFormat.forPattern("dd.MM.yyyy hh:mm")
        )
        set(value) = prefs.edit().putString(
            FULL_COURSE_CACHE_EXPIRATION,
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

    var sentDirectoryId: String
        get() = prefs.getString(SENT_DIRECTORY_ID, "") ?: ""
        set(value) = prefs.edit().putString(SENT_DIRECTORY_ID, value).apply()

    var newEmailCount: Int
        get() = prefs.getInt(NEW_EMAIL_COUNT, 0)
        set(value) = prefs.edit().putInt(NEW_EMAIL_COUNT, value).apply()
}