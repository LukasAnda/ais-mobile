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

import com.lukasanda.aismobile.data.cache.SafePrefs
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(val prefs: SafePrefs) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().newBuilder().apply {
            if (!chain.request().url.toUri().toString().contains("login.pl")) {
                addHeader("Cookie", prefs.sessionCookie)
            }
        }.build())
    }
}

class EncodingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.toString().replace("%3D", "=")
        return chain.proceed(chain.request().newBuilder().apply {
            url(url)
        }.build())
    }
}