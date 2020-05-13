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

package com.lukasanda.aismobile.ui.settings

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.databinding.SettingsActivityBinding
import com.lukasanda.aismobile.util.ThemeHelper
import com.lukasanda.aismobile.util.createLiveData
import com.lukasanda.aismobile.util.startSingleWorker
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding
    private val prefs: Prefs by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingsActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferences, SettingsFragment())
            .commit()



        createLiveData<String>(prefs.prefs, prefs.THEME).observe(this, Observer {
            Log.d("TAG", "Actual value: $it")
            ThemeHelper.applyTheme(it.toInt())
            delegate.localNightMode = ThemeHelper.getLocalTheme(it.toInt())
        })

        createLiveData<Int>(prefs.prefs, prefs.UPDATE_INTERVAL).observe(this, Observer {
            println(" Actual value: $it")
            startSingleWorker(applicationContext)
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}