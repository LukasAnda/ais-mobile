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

import com.google.gson.GsonBuilder
import com.lukasanda.aismobile.BuildConfig
import com.lukasanda.aismobile.core.TLSSocketFactoryCompat
import com.lukasanda.aismobile.util.AuthInterceptor
import com.lukasanda.aismobile.util.EncodingInterceptor
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT = 15L
private const val WRITE_TIMEOUT = 15L
private const val READ_TIMEOUT = 15L

val networkModule = module {
    single { Cache(androidApplication().cacheDir, 10L * 1024 * 1024) }

    single { GsonBuilder().setLenient().create() }

    single {
        OkHttpClient.Builder().apply {
            cache(get())
            sslSocketFactory(TLSSocketFactoryCompat())
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            connectionSpecs(
                listOf(
                    ConnectionSpec.CLEARTEXT,
                    ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .allEnabledTlsVersions()
                        .allEnabledCipherSuites()
                        .build()
                )
            )
            followSslRedirects(false)
            followRedirects(false)
            retryOnConnectionFailure(true)
            addInterceptor(EncodingInterceptor())
            addInterceptor(AuthInterceptor(get()))
            addInterceptor(get())
            addInterceptor(HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            })
        }.build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://is.stuba.sk/")
            .addConverterFactory(GsonConverterFactory.create(get()))
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(get())
            .build()
    }

    single {
        Interceptor { chain ->
            chain.proceed(chain.request().newBuilder().apply {
                addHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36"
                )
                addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"
                )
                addHeader("Accept-Encoding", "gzip, deflate, br")
                addHeader("Accept-Language", "sk-SK,sk;q=0.9,cs;q=0.8,en-US;q=0.7,en;q=0.6")
                addHeader("Cache-Control", "max-age=0")
                addHeader("Connection", "keep-alive")
                addHeader("Host", "is.stuba.sk")
                addHeader("Origin", "https://is.stuba.sk")
                addHeader("Upgrade-Insecure-Requests", "1")
            }.build())
        }
    }
}