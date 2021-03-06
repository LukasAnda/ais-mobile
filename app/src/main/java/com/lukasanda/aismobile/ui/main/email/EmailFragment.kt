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

package com.lukasanda.aismobile.ui.main.email

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.core.ACTION_EMAIL_DELETE
import com.lukasanda.aismobile.core.AnalyticsTrait
import com.lukasanda.aismobile.core.SCREEN_EMAIL
import com.lukasanda.aismobile.core.SwipeHelper
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.databinding.EmailFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import com.lukasanda.aismobile.ui.main.BaseFragmentHandler
import com.lukasanda.aismobile.ui.main.email.adapter.EmailAdapter
import com.lukasanda.aismobile.ui.recyclerview.bindLinear
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class EmailFragment :
    BaseFragment<EmailFragment.Views, EmailFragmentBinding, EmailViewModel, EmailFragmentHandler>(), AnalyticsTrait {

    override val viewModel: EmailViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): EmailFragmentBinding = EmailFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: EmailFragmentHandler

    val adapter = EmailAdapter {
        handler.showEmailDetail(it)
    }

    override fun getAnalytics() = FirebaseAnalytics.getInstance(requireContext())

    inner class Views : BaseViews {
        override fun modifyViews() {
            postponeEnterTransition()
            logEvent(SCREEN_EMAIL)
            handler.setTitle(getString(R.string.emails))
            binding?.recycler?.bindLinear(adapter)

            val swipeHelper: SwipeHelper = object : SwipeHelper() {
                @SuppressLint("RestrictedApi")
                override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder?, underlayButtons: MutableList<UnderlayButton>?) {
                    underlayButtons?.add(
                        UnderlayButton(
                            "", AppCompatDrawableManager.get().getDrawable(requireContext(), R.drawable.ic_delete), Color.parseColor("#E57373"),
                            object : UnderlayButtonClickListener {
                                override fun onClick(pos: Int) {
                                    logEvent(ACTION_EMAIL_DELETE)
                                    val email = adapter.getEmailAt(pos)
                                    viewModel.deleteEmail(email)
                                    Log.d("TAG", "Delete clicked")
                                }
                            }
                        ))
                    underlayButtons?.add(UnderlayButton("",
                        AppCompatDrawableManager.get().getDrawable(requireContext(), R.drawable.ic_reply),
                        ContextCompat.getColor(requireContext(), R.color.color_primary_variant),
                        object : UnderlayButtonClickListener {
                            override fun onClick(pos: Int) {
                                val email = adapter.getEmailAt(pos)
                                handler.replyToEmail(email)
                                Log.d("TAG", "Reply clicked")
                            }
                        }
                    ))
                }
            }

            swipeHelper.attachToRecyclerView(binding?.recycler!!)

            viewModel.emails().observe(viewLifecycleOwner, Observer {
                Timber.d("I have to show ${it.size} emails")
                adapter.swapData(it.sortedByDescending {
                    it.date.takeIf { it.isEmpty() }?.let { DateTime.now() } ?: run {
                        DateTime.parse(
                            it.date,
                            DateTimeFormat.forPattern("dd. MM. yyyy HH:mm")
                        )
                    }
                })
                startPostponedEnterTransition()
            })
            binding?.compose?.setOnClickListener {
                handler.composeEmail()
            }
        }

    }
}

interface EmailFragmentHandler : BaseFragmentHandler {
    fun showEmailDetail(email: Email)
    fun replyToEmail(email: Email)
    fun composeEmail()
}