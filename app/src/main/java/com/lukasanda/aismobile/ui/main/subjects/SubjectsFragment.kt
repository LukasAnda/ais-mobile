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

package com.lukasanda.aismobile.ui.main.subjects

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.lukasanda.aismobile.databinding.FragmentSubjectsBinding
import com.lukasanda.aismobile.ui.main.subjects.courses.SemesterAdapter
import com.lukasanda.aismobile.util.dec
import com.lukasanda.aismobile.util.inc
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import sk.lukasanda.base.ui.activity.BaseViews
import sk.lukasanda.base.ui.fragment.BaseFragment

class SubjectsFragment :
    BaseFragment<SubjectsFragment.Views, FragmentSubjectsBinding, SubjectsViewModel, SubjectsFragmentHandler>() {
    override val viewModel: SubjectsViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): FragmentSubjectsBinding =
        FragmentSubjectsBinding.inflate(layoutInflater)

    override fun createViews() = Views()
    override lateinit var handler: SubjectsFragmentHandler

    private val semesterAdapter by lazy {
        SemesterAdapter()
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewModel.setSemester(position)
        }
    }

    override fun onDestroyView() {
        binding.pager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
    }

    inner class Views : BaseViews {
        override fun modifyViews() {
            binding.buttonBack.setOnClickListener {
                binding.pager.dec()
            }

            binding.buttonForward.setOnClickListener {
                binding.pager.inc()
            }

            binding.pager.apply {
                offscreenPageLimit = 3
                adapter = semesterAdapter
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                binding.pager.setCurrentItem(viewModel.getCurrentSemester(), false)

                binding.pager.registerOnPageChangeCallback(pageChangeCallback)
            }

            viewModel.courses().observe(this@SubjectsFragment, Observer {
                binding.progress.hide()
                semesterAdapter.swapData(it)
                handler.lowerToolbar()
            })

            viewModel.semesters().observe(this@SubjectsFragment, Observer {
                binding.semester.text = it
            })

            viewModel.setSemester(viewModel.getActualSemester())
        }

    }
}

interface SubjectsFragmentHandler {
    fun lowerToolbar()
}