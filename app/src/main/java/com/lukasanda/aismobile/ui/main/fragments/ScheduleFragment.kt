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

package com.lukasanda.aismobile.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.core.State
import com.lukasanda.aismobile.ui.main.MainViewModel
import com.lukasanda.aismobile.ui.main.adapters.CourseScheduleAdapter
import com.lukasanda.aismobile.ui.main.adapters.CourseWeekAdapter
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import kotlinx.android.synthetic.main.fragment_schedule.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScheduleFragment : Fragment() {

    private val viewModel by sharedViewModel<MainViewModel>()
//    private val viewAdapter = CourseScheduleAdapter()

    private val weekAdapter = CourseWeekAdapter()

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            viewModel.setDay(position % 7)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCourses()

        viewModel.courses().observe(this, Observer {
            when(it){
                is State.Loading ->{
                    progress.show()
                    pager.hide()
                }
                is State.Success -> {
                    progress.hide()
                    pager.show()
                    weekAdapter.swapData(it.data)

                    var pageToSelect = Int.MAX_VALUE / 2
                    while (pageToSelect % 7 != viewModel.actualDay - 1) {
                        pageToSelect++
                    }
                    pager.unregisterOnPageChangeCallback(pageChangeCallback)
                    pager.setCurrentItem(pageToSelect, false)
                    pager.registerOnPageChangeCallback(pageChangeCallback)
                }
            }
        })

        viewModel.days().observe(this, Observer {
            if(it !in 0..6) return@Observer
            day.text = viewModel.days[it]
        })

        buttonBack.setOnClickListener {
            viewModel.previousDay()

            val currentItem = pager.currentItem
            pager.setCurrentItem(currentItem - 1, true)
        }

        buttonForward.setOnClickListener {
            viewModel.nextDay()

            val currentItem = pager.currentItem
            pager.setCurrentItem(currentItem + 1, true)
        }

        pager.apply {
            offscreenPageLimit = 7
            adapter = weekAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            pager.registerOnPageChangeCallback(pageChangeCallback)
        }
    }

    override fun onDestroyView() {
        pager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
    }
}