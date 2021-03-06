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

package com.lukasanda.aismobile.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.trait.HandlerTrait
import com.lukasanda.aismobile.ui.trait.LifecycleTrait
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel

abstract class BaseDialogFragment<VIEWS : BaseViews, BINDING : ViewBinding, VIEWMODEL : BaseViewModel, HANDLER : Any> :
    DialogFragment(), LifecycleTrait, HandlerTrait<HANDLER> {
    protected lateinit var binding: BINDING
    private lateinit var views: VIEWS
    abstract val viewModel: VIEWMODEL

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = this.setBinding()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.views = this.createViews()
        this.views.modifyViews()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    this@BaseDialogFragment.dismiss()
                }
            })
    }

    abstract fun setBinding(): BINDING

    abstract fun createViews(): VIEWS
}