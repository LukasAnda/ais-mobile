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

//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.google.gson.GsonBuilder
import com.lukasanda.aismobile.BuildConfig
import com.lukasanda.aismobile.util.AuthInterceptor
import com.lukasanda.aismobile.util.EncodingInterceptor
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

private const val CONNECT_TIMEOUT = 60L
private const val WRITE_TIMEOUT = 60L
private const val READ_TIMEOUT = 60L

val networkModule = module {
//    single { Cache(androidApplication().cacheDir, 10L * 1024 * 1024) }

    single { GsonBuilder().setLenient().create() }

    single {
        OkHttpClient.Builder().apply {
            connectionPool(ConnectionPool(10, 1, TimeUnit.MINUTES))
            this.dispatcher(Dispatcher(Executors.newSingleThreadExecutor()))
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            followSslRedirects(true)
            followRedirects(false)
            retryOnConnectionFailure(true)
            addInterceptor(EncodingInterceptor())
            addInterceptor(AuthInterceptor(get()))
            addInterceptor(Interceptor { chain ->
                chain.proceed(chain.request().newBuilder().apply {
                    removeHeader("User-Agent")
                    addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                    addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                    addHeader("Accept-Encoding", "gzip, deflate, br")
                    addHeader("Accept-Language", "sk-SK,sk;q=0.9,cs;q=0.8,en-US;q=0.7,en;q=0.6")
                    addHeader("Cache-Control", "max-age=0")
                    addHeader("Connection", "keep-alive")
                    addHeader("Host", "is.stuba.sk")
                    addHeader("Origin", "https://is.stuba.sk")
                    addHeader("Upgrade-Insecure-Requests", "1")
                }.build())
            })

            hostnameVerifier(HostnameVerifier { hostname, session -> true })
//            hostnameVerifier { hostname: String?, session: SSLSession? -> true }
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
            //.addConverterFactory(GsonConverterFactory.create(get()))
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(get())
            .build()
    }
}