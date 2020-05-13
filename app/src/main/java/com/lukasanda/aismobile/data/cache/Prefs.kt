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

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.lukasanda.aismobile.BuildConfig
import org.joda.time.DateTime

class Prefs(private val context: Context) {
    val PREFS_FILENAME = "${BuildConfig.APPLICATION_ID}.prefs"
    val SESSION_EXPIRATION = "session_expiration_date"
    val AISID = "aisid"
    val DID_SHOW_LOADING = "did_show_loading"
    val SENT_DIRECTORY_ID = "sent_directory_id"
    val NEW_EMAIL_COUNT = "new_email_count"
    val EMAIL_CACHE_EXPIRATION = "email_cache_expiration"
    val FULL_COURSE_CACHE_EXPIRATION = "full_course_cache_expiration"
    val COURSE_CACHE_EXPIRATION = "course_cache_expiration"
    val TIMETABLE_CACHE_EXPIRATION = "timetable_cache_expiration"

    //Settings prefs
    val UPDATE_INTERVAL = "update_interval"
    val THEME = "theme"

    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var expiration: DateTime = DateTime.now().minusWeeks(1)
        get() = DateTime(prefs.getLong(SESSION_EXPIRATION, 0))
        @SuppressLint("ApplySharedPref")
        set(value) {
            field = value
            prefs.edit().putLong(SESSION_EXPIRATION, value.millis).commit()
        }

    var timetableExpiration: DateTime = DateTime.now().minusWeeks(1)
        get() = DateTime(prefs.getLong(TIMETABLE_CACHE_EXPIRATION, 0))
        @SuppressLint("ApplySharedPref")
        set(value) {
            field = value
            prefs.edit().putLong(TIMETABLE_CACHE_EXPIRATION, value.millis).commit()
        }

    var emailExpiration: DateTime = DateTime.now().minusWeeks(1)
        get() = DateTime(prefs.getLong(EMAIL_CACHE_EXPIRATION, 0))
        @SuppressLint("ApplySharedPref")
        set(value) {
            field = value
            prefs.edit().putLong(EMAIL_CACHE_EXPIRATION, value.millis).commit()
        }

    var courseExpiration: DateTime = DateTime.now().minusWeeks(1)
        get() = DateTime(prefs.getLong(COURSE_CACHE_EXPIRATION, 0))
        @SuppressLint("ApplySharedPref")
        set(value) {
            field = value
            prefs.edit().putLong(COURSE_CACHE_EXPIRATION, value.millis).commit()
        }

    var fullCourseExpiration: DateTime = DateTime.now().minusWeeks(1)
        get() = DateTime(prefs.getLong(FULL_COURSE_CACHE_EXPIRATION, 0))
        @SuppressLint("ApplySharedPref")
        set(value) {
            field = value
            prefs.edit().putLong(FULL_COURSE_CACHE_EXPIRATION, value.millis).commit()
        }

    var id: Int
        get() = prefs.getInt(AISID, 0)
        set(value) = prefs.edit().putInt(AISID, value).apply()

    var didShowLoading: Boolean
        get() = prefs.getBoolean(DID_SHOW_LOADING, false)
        set(value) = prefs.edit().putBoolean(DID_SHOW_LOADING, value).apply()

    var sentDirectoryId: String
        get() = prefs.getString(SENT_DIRECTORY_ID, "") ?: ""
        set(value) = prefs.edit().putString(SENT_DIRECTORY_ID, value).apply()

    var newEmailCount: Int
        get() = prefs.getInt(NEW_EMAIL_COUNT, 0)
        set(value) = prefs.edit().putInt(NEW_EMAIL_COUNT, value).apply()

    var theme: Int
        get() {
            val entry = prefs.getString(THEME, "0") ?: "0"

            return entry.toInt()
        }
        set(value) = Unit

    var updateInterval: Int
        get() = prefs.getInt(UPDATE_INTERVAL, 1)
        set(value) = Unit

    fun nukeAll() {
        prefs.edit().remove(SESSION_EXPIRATION).remove(AISID).remove(DID_SHOW_LOADING).remove(SENT_DIRECTORY_ID).remove(NEW_EMAIL_COUNT).remove(EMAIL_CACHE_EXPIRATION)
            .remove(FULL_COURSE_CACHE_EXPIRATION).remove(COURSE_CACHE_EXPIRATION).remove(TIMETABLE_CACHE_EXPIRATION).apply()
    }
}