/*
 * Copyright 2019 Lukáš Anda. All rights reserved.
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

package com.lukasanda.aismobile.ui.main

import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.ui.main.fragments.ScheduleFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_header_drawer.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bumptech.glide.load.model.LazyHeaders
import com.lukasanda.aismobile.data.cache.Prefs
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    private val viewModel by viewModel<MainViewModel>()
    private val prefs by inject<Prefs>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)


        toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        windowTitle.text = "Rozvrh"

        val badge = bottomMenu.getOrCreateBadge(R.id.notifications)
        badge.number = 5
        badge.backgroundColor = ContextCompat.getColor(this, R.color.color_error)

        supportFragmentManager.beginTransaction().replace(R.id.container, ScheduleFragment())
            .commit()

        bottomMenu.setOnNavigationItemSelectedListener {
            return@setOnNavigationItemSelectedListener when (it.itemId) {
                R.id.timetable -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ScheduleFragment()).commit()
                    true
                }
                else -> {
                    false
                }
            }
        }

        viewModel.profile().observe(this, Observer {
            val builder = LazyHeaders.Builder()
                .addHeader("Cookie", prefs.sessionCookie)

            val url = GlideUrl("https://is.stuba.sk/auth/lide/foto.pl?id=${it.id}", builder.build())


            Glide.with(this@MainActivity).load(url).circleCrop().into(profile)
            aisId.text = "AIS ID: ${it.id}"
            wifiName.text = "Wifi username: ${it.email}"
            wifiPassword.text = "Wifi password: ${it.password}"
        })

        viewModel.getProfile()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }
}