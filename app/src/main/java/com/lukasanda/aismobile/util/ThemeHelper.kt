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

import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    const val lightMode = 0
    const val darkMode = 1
    const val default = 2
    const val batterySaverMode = 3

    fun applyTheme(theme: Int) {
        when (theme) {
            lightMode -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            darkMode -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            default -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            batterySaverMode -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
    }

    fun getLocalTheme(theme: Int) = when (theme) {
        lightMode -> AppCompatDelegate.MODE_NIGHT_NO
        darkMode -> AppCompatDelegate.MODE_NIGHT_YES
        default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        batterySaverMode -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        else -> AppCompatDelegate.MODE_NIGHT_NO
    }

}