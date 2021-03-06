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

package com.lukasanda.aismobile.ui.main.timetable

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.analytics.FirebaseAnalytics
import com.lukasanda.aismobile.core.AnalyticsTrait
import com.lukasanda.aismobile.core.SCREEN_TIMETABLE
import com.lukasanda.aismobile.data.db.entity.WeekItem
import com.lukasanda.aismobile.databinding.TimetableFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import com.lukasanda.aismobile.ui.main.BaseFragmentHandler
import com.lukasanda.aismobile.ui.main.timetable.timetable.WeekAdapter
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import org.joda.time.DateTime
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TimetableFragment :
    BaseFragment<TimetableFragment.Views, TimetableFragmentBinding, TimetableViewModel, TimetableFragmentHandler>(), AnalyticsTrait {

    override lateinit var handler: TimetableFragmentHandler
    override val viewModel: TimetableViewModel by viewModel { parametersOf(Bundle()) }

    private val weekAdapter by lazy {
        WeekAdapter {
            handler.showDetailFromTimetable(it.courseId)
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewModel.setDay(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                if (binding?.pager?.currentItem == 0) {
                    binding?.pager?.setCurrentItem(weekAdapter.itemCount - 2, false)
                } else if (binding?.pager?.currentItem == weekAdapter.itemCount - 1) {
                    binding?.pager?.setCurrentItem(1, false)
                }
            }
        }
    }

    override fun onDestroyView() {
        binding?.pager?.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
    }

    override fun setBinding(): TimetableFragmentBinding =
        TimetableFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    inner class Views : BaseViews {
        override fun modifyViews() {
            logEvent(SCREEN_TIMETABLE)
            binding?.pager?.apply {
                offscreenPageLimit = 3
                adapter = weekAdapter
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                binding?.pager?.registerOnPageChangeCallback(pageChangeCallback)
            }

            viewModel.timetable().observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                binding?.progress?.hide()
                if (it.isEmpty()) {
                    binding?.pager?.hide()
                    binding?.indicatorLayout?.hide()
                } else {
                    binding?.pager?.show()
                    binding?.indicatorLayout?.show()

                    weekAdapter.swapData(it.map { WeekItem(it) })
                    binding?.indicator?.attachToPager(binding?.pager!!)
                    binding?.pager?.setCurrentItem(DateTime.now().dayOfWeek, false)

                }
            })

            viewModel.days().observe(viewLifecycleOwner, Observer {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    handler.setTitle(it)
                }
            })
        }

    }

    override fun getAnalytics() = FirebaseAnalytics.getInstance(requireContext())
}

interface TimetableFragmentHandler : BaseFragmentHandler {
    fun showDetailFromTimetable(courseId: String)
}