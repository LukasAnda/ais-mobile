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

package com.lukasanda.aismobile.ui.main.subjectDetail

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.lukasanda.aismobile.databinding.SubjectDetailFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import sk.lukasanda.base.ui.activity.BaseViews
import sk.lukasanda.base.ui.fragment.BaseFragment
import sk.lukasanda.base.ui.recyclerview.bindLinear

class SubjectDetailFragment :
    BaseFragment<SubjectDetailFragment.Views, SubjectDetailFragmentBinding, SubjectDetailViewModel, SubjectDetailHandler>() {
    val adapter = SubjectTablesAdapter {

    }

    inner class Views : BaseViews {
        override fun modifyViews() {

            val args by navArgs<SubjectDetailFragmentArgs>()

            binding.tablesRecycler.bindLinear(adapter)

            viewModel.getCourse(args.courseId).observe(viewLifecycleOwner, Observer {
                it.sheets?.let { it1 -> adapter.swapData(it1) }
            })
        }

    }

    override val viewModel: SubjectDetailViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): SubjectDetailFragmentBinding =
        SubjectDetailFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: SubjectDetailHandler
}

interface SubjectDetailHandler