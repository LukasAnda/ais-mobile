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

package com.lukasanda.aismobile.ui.loading

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.Observer
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.remote.SyncCoroutineWorker
import com.lukasanda.aismobile.databinding.LoadingActivityBinding
import com.lukasanda.aismobile.ui.activity.BaseActivityViews
import com.lukasanda.aismobile.ui.activity.BaseUIActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class LoadingActivity : BaseUIActivity<LoadingViewModel, LoadingActivity.Views, LoadingActivityBinding>() {
    private val prefs by inject<Prefs>()

    inner class Views : BaseActivityViews {
        override fun setNavigationGraph(): Int? = null

        override fun modifyViews() {
            var animator: ValueAnimator? = null
            viewModel.getWorkLiveData().observe(this@LoadingActivity, Observer {
                it.filterNotNull().firstOrNull()?.let {
                    val progress = it.progress.getInt(SyncCoroutineWorker.PROGRESS, -1)
                    if (progress == -1) return@let
                    val progressMessage = it.progress.getInt(SyncCoroutineWorker.PROGRESS_MESSAGE, 0).takeIf { it != 0 }
                    val specialProgressMessage = it.progress.getString(SyncCoroutineWorker.PROGRESS_SPECIAL_MESSAGE)

                    val actualProgress = binding.loadingIndicator.mProgressValue

                    animator?.end()
                    animator = ValueAnimator.ofInt(actualProgress, progress).apply {
                        duration = 1500
                        interpolator = AccelerateDecelerateInterpolator()
                        addUpdateListener {
                            binding.loadingIndicator.progressValue = it.animatedValue as Int
                            binding.loadingIndicator.requestLayout()

                            if (it.animatedValue as Int >= 99) {
                                Handler().postDelayed({
                                    loadingComplete()
                                }, 1500)
                            }
                        }
                        start()
                    }

                    if (progressMessage != null) {
                        binding.status.setText(progressMessage)
                    }
                    if (specialProgressMessage != null) {
                        binding.status.setText(specialProgressMessage)
                    }
                }
            })
        }

    }

    override fun onBackPressed() {

    }

    private fun loadingComplete() {
        prefs.didShowLoading = true
        finish()
    }

    override val viewModel: LoadingViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): LoadingActivityBinding = LoadingActivityBinding.inflate(layoutInflater)

    override fun createViews(): Views = Views()
}