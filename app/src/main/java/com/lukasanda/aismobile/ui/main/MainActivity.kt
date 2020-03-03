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
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewOutlineProvider
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.Observer
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.data.db.entity.Teacher
import com.lukasanda.aismobile.databinding.ActivityMainBinding
import com.lukasanda.aismobile.databinding.DrawerHeaderViewBinding
import com.lukasanda.aismobile.ui.login.LoginActivity
import com.lukasanda.aismobile.ui.main.composeEmail.ComposeEmailHandler
import com.lukasanda.aismobile.ui.main.email.EmailFragmentDirections
import com.lukasanda.aismobile.ui.main.email.EmailFragmentHandler
import com.lukasanda.aismobile.ui.main.emailDetail.EmailDetailFragmentDirections
import com.lukasanda.aismobile.ui.main.emailDetail.EmailDetailHandler
import com.lukasanda.aismobile.ui.main.subjectDetail.SubjectDetailFragmentDirections
import com.lukasanda.aismobile.ui.main.subjectDetail.SubjectDetailHandler
import com.lukasanda.aismobile.ui.main.subjects.SubjectsFragmentDirections
import com.lukasanda.aismobile.ui.main.subjects.SubjectsFragmentHandler
import com.lukasanda.aismobile.ui.main.timetable.TimetableFragmentDirections
import com.lukasanda.aismobile.ui.main.timetable.TimetableFragmentHandler
import com.lukasanda.aismobile.util.createLiveData
import com.lukasanda.aismobile.util.show
import com.lukasanda.aismobile.util.startWorker
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import sk.lukasanda.base.helpers.navigateSafe
import sk.lukasanda.base.ui.activity.BaseActivityViews
import sk.lukasanda.base.ui.activity.BaseUIActivity


class MainActivity : BaseUIActivity<MainViewModel, MainActivity.Views, ActivityMainBinding>(),
        TimetableFragmentHandler, SubjectsFragmentHandler, EmailFragmentHandler, EmailDetailHandler,
        ComposeEmailHandler, SubjectDetailHandler {

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var drawerHeaderBinding: DrawerHeaderViewBinding

    override val viewModel by viewModel<MainViewModel> { parametersOf(Bundle()) }
    private val prefs by inject<Prefs>()

    inner class Views : BaseActivityViews {

        override fun modifyViews() {
            drawerHeaderBinding = DrawerHeaderViewBinding.bind(binding.navigationView.getHeaderView(0))
            this@MainActivity.navController?.let {
                NavigationUI.setupWithNavController(binding.bottomMenu, it)
            }


            if (prefs.sessionCookie.isEmpty()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            } else {
                startWorker(applicationContext)
            }

            viewModel.profile().observe(this@MainActivity, Observer {
                if (it == null) return@Observer

                val builder = LazyHeaders.Builder().addHeader("Cookie", prefs.sessionCookie)

                val url = GlideUrl("https://is.stuba.sk/auth/lide/foto.pl?id=${it.id}", builder.build())

                Glide.with(this@MainActivity).load(url).listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        drawerHeaderBinding.profile.show()
                        drawerHeaderBinding.aisId.show()
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean) = false

                }).into(drawerHeaderBinding.profile)
                drawerHeaderBinding.aisId.text = it.id.toString()
                drawerHeaderBinding.username.text = it.email
                drawerHeaderBinding.password.text = it.password
            })

            navController?.addOnDestinationChangedListener { controller, destination, arguments ->
                binding.windowTitle.text = destination.label
            }

            createLiveData<Int>(prefs.prefs, prefs.NEW_EMAIL_COUNT).observe(this@MainActivity, Observer {
                if (it > 0) {
                    binding.bottomMenu.getOrCreateBadge(R.id.emailFragment).apply {
                        badgeTextColor = Color.WHITE
                        this.number = it
                        backgroundColor = Color.RED
                    }
                }
            })
        }

        override fun setNavigationGraph() = R.id.homeNavigationContainer
    }

    override fun setDrawer() = binding.drawer

    override fun setToolbar() = binding.toolbar

    override fun createViews(): Views = Views()

    override fun setBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun setAppBarConfig() = AppBarConfiguration.Builder(R.id.scheduleFragment, R.id.subjectsFragment, R.id.emailFragment).setDrawerLayout(binding.drawer).build()

    override fun lowerToolbar() {
        binding.appbar.outlineProvider = null
    }

    override fun showDetailFromSubjects(courseId: String) {
        navController?.navigateSafe(SubjectsFragmentDirections.actionSubjectsFragmentToSubjectDetailFragment(courseId))
    }

    override fun showDetailFromTimetable(courseId: String) {
        navController?.navigateSafe(TimetableFragmentDirections.actionScheduleFragmentToSubjectDetailFragment(courseId))
    }

    override fun riseToolbar() {
        binding.appbar.outlineProvider = ViewOutlineProvider.BOUNDS
    }

    override fun showEmailDetail(email: Email) {
        navController?.navigateSafe(EmailFragmentDirections.actionEmailFragmentToEmailDetailFragment(email))
    }

    override fun composeEmail() {
        navController?.navigateSafe(EmailFragmentDirections.actionEmailFragmentToComposeEmailFragment())
    }

    override fun closeFragment() {
        navController?.popBackStack()
    }

    override fun reply(email: Email) {
        navController?.navigateSafe(EmailDetailFragmentDirections.actionEmailDetailFragmentToComposeEmailFragment(email = email))
    }

    override fun writeToTeacher(teacher: Teacher) {
        navController?.navigateSafe(SubjectDetailFragmentDirections.actionSubjectDetailFragmentToComposeEmailFragment(teacher = teacher))
    }
}