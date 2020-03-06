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
import de.adorsys.android.securestoragelibrary.SecurePreferences

private const val EMAIL_KEY = "EMAIL_KEY"
private const val PASSWORD_KEY = "PASSWORD_KEY"
private const val SESSION_COOKIE_KEY = "SESSION_COOKIE_KEY"

class SafePrefs(private val context: Context) {

    var email: String
        get() = SecurePreferences.getStringValue(context, EMAIL_KEY, "") ?: ""
        set(value) = SecurePreferences.setValue(context, EMAIL_KEY, value)

    var password: String
        get() = SecurePreferences.getStringValue(context, PASSWORD_KEY, "") ?: ""
        set(value) = SecurePreferences.setValue(context, PASSWORD_KEY, value)

    var sessionCookie: String
        get() = SecurePreferences.getStringValue(context, SESSION_COOKIE_KEY, "") ?: ""
        set(value) = SecurePreferences.setValue(context, SESSION_COOKIE_KEY, value)
}