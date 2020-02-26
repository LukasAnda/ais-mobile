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

//import net.danlew.android.joda.JodaTimeAndroid
import android.app.Application
import com.lukasanda.aismobile.di.*
import com.lukasanda.aismobile.util.ThemeHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@Suppress("Unused")
class AISApp : Application() {

    override fun onCreate() {
        super.onCreate()


//        JodaTimeAndroid.init(this);

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

        ThemeHelper.applyTheme(ThemeHelper.default)
    }

}