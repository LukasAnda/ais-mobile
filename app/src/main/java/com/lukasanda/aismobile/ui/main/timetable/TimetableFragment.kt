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
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.lukasanda.aismobile.databinding.FragmentScheduleBinding
import com.lukasanda.aismobile.ui.main.timetable.timetable.WeekAdapter
import com.lukasanda.aismobile.util.dec
import com.lukasanda.aismobile.util.inc
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import sk.lukasanda.base.ui.activity.BaseViews
import sk.lukasanda.base.ui.fragment.BaseFragment

class TimetableFragment :
    BaseFragment<TimetableFragment.Views, FragmentScheduleBinding, TimetableViewModel, TimetableFragmentHandler>() {

    override lateinit var handler: TimetableFragmentHandler
    override val viewModel: TimetableViewModel by viewModel { parametersOf(Bundle()) }

    private val weekAdapter by lazy {
        WeekAdapter()
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewModel.setDay(position)
        }
    }

    override fun onDestroyView() {
        binding.pager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
    }

    override fun setBinding(): FragmentScheduleBinding =
        FragmentScheduleBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    inner class Views : BaseViews {
        override fun modifyViews() {
            binding.buttonBack.setOnClickListener {
                binding.pager.dec()
            }

            binding.buttonForward.setOnClickListener {
                binding.pager.inc()
            }

            binding.pager.apply {
                offscreenPageLimit = 1
                adapter = weekAdapter
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                binding.pager.setCurrentItem(viewModel.getCurrentDay(), false)

                binding.pager.registerOnPageChangeCallback(pageChangeCallback)
            }

            viewModel.timetable().observe(this@TimetableFragment, Observer {
                binding.progress.hide()
                weekAdapter.swapData(it)
                handler.lowerToolbar()
            })

            viewModel.days().observe(this@TimetableFragment, Observer {
                binding.day.text = it
            })

            viewModel.setDay(viewModel.getActualDay())
        }

    }
}

interface TimetableFragmentHandler {
    fun lowerToolbar()
}