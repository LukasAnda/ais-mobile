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

package com.lukasanda.aismobile.ui.main.people

import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.core.ACTION_SEARCH_PEOPLE
import com.lukasanda.aismobile.core.AnalyticsTrait
import com.lukasanda.aismobile.core.SCREEN_PEOPLE
import com.lukasanda.aismobile.data.db.entity.Suggestion
import com.lukasanda.aismobile.databinding.PeopleFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import com.lukasanda.aismobile.ui.main.BaseFragmentHandler
import com.lukasanda.aismobile.ui.recyclerview.bindLinear
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PeopleFragment : BaseFragment<PeopleFragment.Views, PeopleFragmentBinding, PeopleViewModel, PeopleHandler>(), AnalyticsTrait {
    private val peopleAdapter = PeopleAdapter {
        handler.showPeopleDetail(it)
    }

    inner class Views : BaseViews {
        override fun modifyViews() {
            logEvent(SCREEN_PEOPLE)
            handler.setTitle(getString(R.string.people_at_stu))

            binding?.results?.bindLinear(peopleAdapter)

            viewModel.suggestions().observe(viewLifecycleOwner, Observer {
                peopleAdapter.swapData(it)
            })

            binding?.search?.doOnTextChanged { text, start, count, after ->
                if (text?.length ?: 0 > 2) {
                    logEvent(ACTION_SEARCH_PEOPLE)
                    viewModel.getSuggestions(text.toString())
                } else {
                    viewModel.cancelJobs()
                }
            }


        }
    }

    override val viewModel: PeopleViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding() = PeopleFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: PeopleHandler
    override fun getAnalytics() = FirebaseAnalytics.getInstance(requireContext())
}

interface PeopleHandler : BaseFragmentHandler {
    fun showPeopleDetail(suggestion: Suggestion)
}