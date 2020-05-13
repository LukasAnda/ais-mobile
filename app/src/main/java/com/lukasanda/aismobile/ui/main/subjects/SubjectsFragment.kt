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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.analytics.FirebaseAnalytics
import com.lukasanda.aismobile.core.AnalyticsTrait
import com.lukasanda.aismobile.core.SCREEN_INDEX
import com.lukasanda.aismobile.data.db.entity.Semester
import com.lukasanda.aismobile.databinding.SubjectsFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import com.lukasanda.aismobile.ui.main.BaseFragmentHandler
import com.lukasanda.aismobile.ui.main.subjects.courses.SemesterAdapter
import com.lukasanda.aismobile.ui.recyclerview.replaceWith
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SubjectsFragment :
    BaseFragment<SubjectsFragment.Views, SubjectsFragmentBinding, SubjectsViewModel, SubjectsFragmentHandler>(), AnalyticsTrait {
    override val viewModel: SubjectsViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): SubjectsFragmentBinding = SubjectsFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()
    override lateinit var handler: SubjectsFragmentHandler

    private val semesterAdapter by lazy {
        SemesterAdapter {
            handler.showDetailFromSubjects(it.course.id)
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            updateToolbar(position)
            viewModel.setPage(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                if (binding?.pager?.currentItem == 0) {
                    binding?.pager?.setCurrentItem(semesterAdapter.itemCount - 2, false)
                } else if (binding?.pager?.currentItem == semesterAdapter.itemCount - 1) {
                    binding?.pager?.setCurrentItem(1, false)
                }
            }
        }
    }

    override fun onDestroyView() {
        binding?.pager?.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
    }

    inner class Views : BaseViews {
        override fun modifyViews() {
            logEvent(SCREEN_INDEX)
            postponeEnterTransition()

            binding?.pager?.apply {
                offscreenPageLimit = 3
                adapter = semesterAdapter
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                binding?.pager?.registerOnPageChangeCallback(pageChangeCallback)
            }

            viewModel.courses().observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                startPostponedEnterTransition()
                binding?.progress?.hide()
                if (it.isEmpty()) {
                    binding?.pager?.hide()
                    binding?.empty?.show()
                    binding?.indicatorLayout?.hide()
                } else {

                    binding?.empty?.hide()
                    binding?.pager?.show()
                    binding?.indicatorLayout?.show()


                    semesterAdapter.swapData(it.map { Semester(it) }, false)
                    viewModel.semesters.replaceWith(it.map { it.first().course.semester })

                    binding?.indicator?.attachToPager(binding?.pager!!)

                    binding?.pager?.setCurrentItem(viewModel.getPage(), true)
                    updateToolbar(viewModel.getPage())
                }
            })
        }
    }

    private fun updateToolbar(position: Int) {
        handler.setTitle(viewModel.getSemesterName(position))
    }

    override fun getAnalytics() = FirebaseAnalytics.getInstance(requireContext())
}

interface SubjectsFragmentHandler : BaseFragmentHandler {
    fun showDetailFromSubjects(courseId: String)
}