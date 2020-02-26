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

package com.lukasanda.aismobile.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.ViewOutlineProvider
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.Observer
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.databinding.ActivityMainBinding
import com.lukasanda.aismobile.ui.login.LoginActivity
import com.lukasanda.aismobile.ui.main.composeEmail.ComposeEmailHandler
import com.lukasanda.aismobile.ui.main.email.EmailFragmentDirections
import com.lukasanda.aismobile.ui.main.email.EmailFragmentHandler
import com.lukasanda.aismobile.ui.main.emailDetail.EmailDetailHandler
import com.lukasanda.aismobile.ui.main.subjects.SubjectsFragmentHandler
import com.lukasanda.aismobile.ui.main.timetable.TimetableFragmentHandler
import com.lukasanda.aismobile.util.startWorker
import kotlinx.android.synthetic.main.item_header_drawer.*
import kotlinx.android.synthetic.main.item_header_drawer.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import sk.lukasanda.base.helpers.navigateSafe
import sk.lukasanda.base.ui.activity.BaseActivityViews
import sk.lukasanda.base.ui.activity.BaseUIActivity


class MainActivity : BaseUIActivity<MainViewModel, MainActivity.Views, ActivityMainBinding>(),
    TimetableFragmentHandler, SubjectsFragmentHandler, EmailFragmentHandler, EmailDetailHandler,
    ComposeEmailHandler {

    private lateinit var toggle: ActionBarDrawerToggle

    override val viewModel by viewModel<MainViewModel> { parametersOf(Bundle()) }
    private val prefs by inject<Prefs>()

    inner class Views : BaseActivityViews {

        override fun modifyViews() {
            this@MainActivity.navController?.let {
                binding.bottomMenu.setupWithNavController(it)
            }
//            setSupportActionBar(binding.toolbar)

            if (prefs.sessionCookie.isEmpty()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            } else {
                startWorker(applicationContext)
            }

            binding.windowTitle.text = "Rozvrh"

            viewModel.profile().observe(this@MainActivity, Observer {
                if (it == null) return@Observer

                val builder = LazyHeaders.Builder()
                    .addHeader("Cookie", prefs.sessionCookie)

                val url =
                    GlideUrl("https://is.stuba.sk/auth/lide/foto.pl?id=${it.id}", builder.build())

                if (profile == null) return@Observer


                Glide.with(this@MainActivity).load(url).circleCrop().into(binding.drawer.profile)
                binding.drawer.aisId.text = "AIS ID: ${it.id}"
                binding.drawer.wifiName.text = "Wifi username: ${it.email}"
                binding.drawer.wifiPassword.text = "Wifi password: ${it.password}"
            })
        }

        override fun setNavigationGraph() = R.id.homeNavigationContainer
    }

    override fun setDrawer() = binding.drawer

    override fun setToolbar() = binding.toolbar

    override fun createViews(): Views = Views()

    override fun setBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun lowerToolbar() {
        binding.appbar.outlineProvider = null
    }

    override fun riseToolbar() {
        binding.appbar.outlineProvider = ViewOutlineProvider.BOUNDS
    }

    override fun showEmailDetail(email: Email) {
        navController?.navigateSafe(
            EmailFragmentDirections.actionEmailFragmentToEmailDetailFragment(
                email
            )
        )
    }

    override fun composeEmail() {
        navController?.navigateSafe(EmailFragmentDirections.actionEmailFragmentToComposeEmailFragment())
    }
}