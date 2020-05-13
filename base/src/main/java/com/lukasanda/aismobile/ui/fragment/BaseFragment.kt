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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.trait.HandlerTrait
import com.lukasanda.aismobile.ui.trait.LifecycleTrait
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel

abstract class BaseFragment<VIEWS : BaseViews, BINDING : ViewBinding, VIEWMODEL : BaseViewModel, HANDLER : Any> :
    Fragment(), LifecycleTrait, HandlerTrait<HANDLER> {
    protected var binding: BINDING? = null
    private lateinit var views: VIEWS
    abstract val viewModel: VIEWMODEL

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.lifecycle.addObserver(this)
        this.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun createView() {
                Log.d("TAG", "${this@BaseFragment.javaClass.simpleName} Current event: ${lifecycle.currentState.name}")
                this@BaseFragment.views = this@BaseFragment.createViews()
                this@BaseFragment.views.modifyViews()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = this.setBinding()
        return binding?.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.let {
            if (it.isActive) {
                it.hideSoftInputFromWindow(view?.windowToken, 0)
            }
        }
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        if (!::views.isInitialized) {
//            this.views = this.createViews()
//            this.views.modifyViews()
//        }
//    }

    override fun onCreated() {
        super.onCreated()
        Log.d("TAG", "On created called")
        this.views = this.createViews()
        this.views.modifyViews()
    }

    abstract fun setBinding(): BINDING

    abstract fun createViews(): VIEWS
}