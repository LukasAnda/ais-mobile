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

package com.lukasanda.aismobile.ui.main.loading

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.Observer
import com.lukasanda.aismobile.data.remote.SyncCoroutineWorker
import com.lukasanda.aismobile.databinding.LoadingFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class LoadingFragment : BaseFragment<LoadingFragment.Views, LoadingFragmentBinding, LoadingViewModel, LoadingHandler>() {
    inner class Views : BaseViews {
        override fun modifyViews() {
            handler.startLoading()
            var animator: ValueAnimator? = null
            viewModel.getWorkLiveData().observe(viewLifecycleOwner, Observer {
                it.filterNotNull().firstOrNull()?.let {
                    val progress = it.progress.getInt(SyncCoroutineWorker.PROGRESS, -1)
                    if (progress == -1) return@let
                    val progressMessage = it.progress.getInt(SyncCoroutineWorker.PROGRESS_MESSAGE, 0).takeIf { it != 0 }

                    val actualProgress = binding?.loadingIndicator?.mProgressValue ?: 0

                    Log.d("TAG", "Actual progress: $actualProgress, target progress: $progress")
                    animator?.end()
                    animator = ValueAnimator.ofInt(actualProgress, progress).apply {
                        duration = 1500
                        interpolator = AccelerateDecelerateInterpolator()
                        addUpdateListener {
                            binding?.loadingIndicator?.progressValue = it.animatedValue as Int
                            binding?.loadingIndicator?.requestLayout()

                            if (it.animatedValue as Int >= 99) {
                                Handler().postDelayed({
                                    handler.loadingComplete()
                                }, 1500)
                            }
                        }
                        start()
                    }

                    if (progressMessage != null) {
                        binding?.status?.setText(progressMessage)
                    }
                }
            })
        }

    }

    override val viewModel: LoadingViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): LoadingFragmentBinding = LoadingFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: LoadingHandler
}

interface LoadingHandler {
    fun startLoading()
    fun loadingComplete()
}