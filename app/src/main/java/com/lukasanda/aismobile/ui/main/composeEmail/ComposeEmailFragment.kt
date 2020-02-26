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

package com.lukasanda.aismobile.ui.main.composeEmail

import android.os.Bundle
import androidx.lifecycle.Observer
import com.lukasanda.aismobile.databinding.ActivityLoginBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import sk.lukasanda.base.ui.activity.BaseViews
import sk.lukasanda.base.ui.fragment.BaseFragment

class ComposeEmailFragment :
    BaseFragment<ComposeEmailFragment.Views, ActivityLoginBinding, ComposeEmailViewModel, ComposeEmailHandler>() {

    private val contactAdapter: ContactAdapter = ContactAdapter {
        //        emptyAdapter()
    }

    private fun emptyAdapter() = contactAdapter.swapData(emptyList())

    inner class Views : BaseViews {
        override fun modifyViews() {
//            binding.recipients.apply {
//                doOnTextChanged { text, start, count, after ->
//                    if (text?.length ?: 0 > 3) {
//                        viewModel.getSuggestions(text.toString())
//                    }
//                }
//            }
//
//            binding.contactsRecycler.bindLinear(contactAdapter)

            viewModel.suggestions().observe(viewLifecycleOwner, Observer {
                contactAdapter.swapData(it)
            })
        }

    }

    override val viewModel by viewModel<ComposeEmailViewModel> { parametersOf(Bundle()) }

    override fun setBinding(): ActivityLoginBinding =
        ActivityLoginBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: ComposeEmailHandler

}

interface ComposeEmailHandler {

}