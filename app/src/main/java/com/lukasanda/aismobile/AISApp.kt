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

package com.lukasanda.aismobile

import android.app.Application
import android.content.Context
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.di.*
import com.lukasanda.aismobile.util.ThemeHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext

@Suppress("Unused")
class AISApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initializeSSLContext(applicationContext)

        startKoin {
            androidContext(this@AISApp)
            modules(
                listOf(
                    networkModule,
                    apiModule,
                    roomModule,
                    viewModelModule,
                    prefsModule,
                    repositoryModule
                )
            )
            androidLogger()
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        ThemeHelper.applyTheme(Prefs(applicationContext).theme)
    }

}

fun initializeSSLContext(mContext: Context) {
    try {
        ProviderInstaller.installIfNeeded(mContext)
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, null, null)
        sslContext.createSSLEngine()
    } catch (e: GooglePlayServicesRepairableException) {
        e.printStackTrace()
    } catch (e: GooglePlayServicesNotAvailableException) {
        e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: KeyManagementException) {
        e.printStackTrace()
    }

}