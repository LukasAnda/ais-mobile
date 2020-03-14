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

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewOutlineProvider
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.analytics.FirebaseAnalytics
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.core.*
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.cache.SafePrefs
import com.lukasanda.aismobile.data.db.entity.Document
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.data.db.entity.Teacher
import com.lukasanda.aismobile.databinding.ActivityMainBinding
import com.lukasanda.aismobile.databinding.DrawerHeaderViewBinding
import com.lukasanda.aismobile.helpers.navigateSafe
import com.lukasanda.aismobile.ui.activity.BaseActivityViews
import com.lukasanda.aismobile.ui.activity.BaseUIActivity
import com.lukasanda.aismobile.ui.loading.LoadingActivity
import com.lukasanda.aismobile.ui.login.LoginActivity
import com.lukasanda.aismobile.ui.main.composeEmail.ComposeEmailHandler
import com.lukasanda.aismobile.ui.main.documents.DocumentsFragmentDirections
import com.lukasanda.aismobile.ui.main.documents.DocumentsHandler
import com.lukasanda.aismobile.ui.main.email.EmailFragmentDirections
import com.lukasanda.aismobile.ui.main.email.EmailFragmentHandler
import com.lukasanda.aismobile.ui.main.emailDetail.EmailDetailFragmentDirections
import com.lukasanda.aismobile.ui.main.emailDetail.EmailDetailHandler
import com.lukasanda.aismobile.ui.main.logout.LogoutHandler
import com.lukasanda.aismobile.ui.main.subjectDetail.SubjectDetailFragmentDirections
import com.lukasanda.aismobile.ui.main.subjectDetail.SubjectDetailHandler
import com.lukasanda.aismobile.ui.main.subjects.SubjectsFragmentDirections
import com.lukasanda.aismobile.ui.main.subjects.SubjectsFragmentHandler
import com.lukasanda.aismobile.ui.main.timetable.TimetableFragmentDirections
import com.lukasanda.aismobile.ui.main.timetable.TimetableFragmentHandler
import com.lukasanda.aismobile.util.createLiveData
import com.lukasanda.aismobile.util.getMimeType
import com.lukasanda.aismobile.util.show
import com.lukasanda.aismobile.util.startWorker
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class MainActivity : BaseUIActivity<MainViewModel, MainActivity.Views, ActivityMainBinding>(),
    TimetableFragmentHandler, SubjectsFragmentHandler, EmailFragmentHandler, EmailDetailHandler,
    ComposeEmailHandler, SubjectDetailHandler, DocumentsHandler, LogoutHandler, AnalyticsTrait {

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var drawerHeaderBinding: DrawerHeaderViewBinding

    override val viewModel by viewModel<MainViewModel> { parametersOf(Bundle()) }
    private val safePrefs by inject<SafePrefs>()
    private val prefs by inject<Prefs>()

    inner class Views : BaseActivityViews {

        override fun modifyViews() {
            drawerHeaderBinding = DrawerHeaderViewBinding.bind(binding.navigationView.getHeaderView(0))
            this@MainActivity.navController?.let {
                NavigationUI.setupWithNavController(binding.bottomMenu, it)
            }


            if (safePrefs.sessionCookie.isEmpty()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            } else {
                startWorker(applicationContext)
            }

            viewModel.profile().observe(this@MainActivity, Observer {
                if (it == null) return@Observer

                val builder = LazyHeaders.Builder().addHeader("Cookie", safePrefs.sessionCookie)

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

            viewModel.fileHandle().observe(this@MainActivity, Observer { result ->
                if (result.isSuccess) {
                    val pair = result.getOrNull() ?: return@Observer
                    MaterialDialog(this@MainActivity, BottomSheet()).show {
                        title(R.string.new_document)
                        message(text = getString(R.string.new_document_description, pair.first.path))
                        positiveButton(R.string.open_document) {
                            logEvent(ACTION_OPEN_DOCUMENT)

                            val (file, _) = pair
                            val intent = Intent(Intent.ACTION_VIEW)
                            val uri = FileProvider.getUriForFile(this@MainActivity, this@MainActivity.applicationContext.packageName + ".provider", file)
                            intent.setDataAndType(uri, getMimeType(file.path))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            try {
                                startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                        negativeButton(R.string._cancel)
                    }
                } else if (result.isFailure) {
                    MaterialDialog(this@MainActivity, BottomSheet()).show {
                        title(R.string.document_failed)
                        message(R.string.document_failed_description)
                        positiveButton(R.string._ok)
                    }
                }

            })

            viewModel.logoutData().observe(this@MainActivity, Observer {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            })

            navController?.let { binding.navigationView.setupWithNavController(it) }
        }

        override fun setNavigationGraph() = R.id.homeNavigationContainer
    }

    override fun onResume() {
        super.onResume()
        if (!prefs.didShowLoading && safePrefs.sessionCookie.isNotEmpty()) {
            startActivity(Intent(this, LoadingActivity::class.java))
        }
    }

    override fun setDrawer() = binding.drawer

    override fun setToolbar() = binding.toolbar

    override fun createViews(): Views = Views()

    override fun setBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun setAppBarConfig() = AppBarConfiguration.Builder(R.id.scheduleFragment, R.id.subjectsFragment, R.id.emailFragment, R.id.documentsFragment).setDrawerLayout(binding.drawer).build()

    override fun lowerToolbar() {
        binding.appbar.outlineProvider = null
    }

    override fun showDetailFromSubjects(courseId: String) {
        logEvent(ACTION_SHOW_COURSE_DETAIL)
        navController?.navigateSafe(SubjectsFragmentDirections.actionSubjectsFragmentToSubjectDetailFragment(courseId))
    }

    override fun setDayText(text: String) {
        binding.windowTitle.text = text
    }

    override fun showDetailFromTimetable(courseId: String) {
        logEvent(ACTION_SHOW_COURSE_DETAIL)
        navController?.navigateSafe(TimetableFragmentDirections.actionScheduleFragmentToSubjectDetailFragment(courseId))
    }

    override fun riseToolbar() {
        binding.appbar.outlineProvider = ViewOutlineProvider.BOUNDS
    }

    override fun showEmailDetail(email: Email) {
        logEvent(ACTION_SHOW_EMAIL_DETAIL)
        navController?.navigateSafe(EmailFragmentDirections.actionEmailFragmentToEmailDetailFragment(email))
    }

    override fun replyToEmail(email: Email) {
        logEvent(ACTION_COMPOSE_REPLY)
        navController?.navigateSafe(EmailFragmentDirections.actionEmailFragmentToComposeEmailFragment(email = email))
    }

    override fun composeEmail() {
        logEvent(ACTION_COMPOSE_EMAIL)
        navController?.navigateSafe(EmailFragmentDirections.actionEmailFragmentToComposeEmailFragment())
    }

    override fun closeFragment() {
        navController?.popBackStack()
    }

    override fun reply(email: Email) {
        logEvent(ACTION_COMPOSE_REPLY)
        navController?.navigateSafe(EmailDetailFragmentDirections.actionEmailDetailFragmentToComposeEmailFragment(email = email))
    }

    override fun writeToTeacher(teacher: Teacher) {
        logEvent(ACTION_COMPOSE_EMAIL_TO_TEACHER)
        navController?.navigateSafe(SubjectDetailFragmentDirections.actionSubjectDetailFragmentToComposeEmailFragment(teacher = teacher))
    }

    override fun openFolder(document: Document) {
        logEvent(ACTION_OPEN_FOLDER)
        navController?.navigateSafe(DocumentsFragmentDirections.actionDocumentsFragmentSelf(document.id))
    }

    override fun openDocument(document: Document) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            viewModel.downloadFile(document, this)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }
    }

    override fun navigateToParent() {
        navController?.popBackStack()
    }

    override fun getAnalytics() = FirebaseAnalytics.getInstance(this)

    companion object {
        const val PERMISSION_REQUEST_CODE = 1234
    }

    override fun logout() {
        navController?.popBackStack()

        MaterialDialog(this, BottomSheet()).show {
            title(R.string.logout_title)
            message(R.string.logout_description)
            positiveButton(R.string.logout) {
                logEvent(ACTION_LOGOUT)
                viewModel.logout()
            }
            negativeButton(R.string._cancel) {
            }
        }
    }
}