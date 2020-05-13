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

package com.lukasanda.aismobile.ui.main.emailDetail

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.firebase.analytics.FirebaseAnalytics
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.core.AnalyticsTrait
import com.lukasanda.aismobile.core.SCREEN_EMAIL_DETAIL
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.databinding.EmailDetailFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import com.lukasanda.aismobile.ui.main.BaseFragmentHandler
import com.lukasanda.aismobile.util.getInitialsFromName
import com.lukasanda.aismobile.util.getNameFromSender
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EmailDetailFragment :
    BaseFragment<EmailDetailFragment.Views, EmailDetailFragmentBinding, EmailDetailViewModel, EmailDetailHandler>(), AnalyticsTrait {
    inner class Views : BaseViews {
        override fun modifyViews() {
            setHasOptionsMenu(true)
            logEvent(SCREEN_EMAIL_DETAIL)
            handler.setTitle(" ")
            val args by navArgs<EmailDetailFragmentArgs>()

            binding?.sender?.text = args.email.sender
            binding?.subject?.text = args.email.subject
            binding?.date?.text = args.email.date

            val initials = args.email.sender.getNameFromSender().getInitialsFromName()
            val drawable =
                TextDrawable.builder().beginConfig().textColor(Color.WHITE)
                    .endConfig()
                    .buildRound(initials, ColorGenerator.MATERIAL.getColor(args.email.sender))

            binding?.icon?.setImageDrawable(drawable)

            viewModel.emailDetail().observe(viewLifecycleOwner, Observer {
                it?.let { detail ->
                    binding?.progress?.hide()
                    binding?.content?.text = detail.message

                    if (detail.files.isNotEmpty()) {
                        binding?.showDocuments?.show()
                    } else {
                        binding?.showDocuments?.hide()
                    }
                    binding?.showDocuments?.setOnClickListener {
                        MaterialDialog(this@EmailDetailFragment.requireContext(), BottomSheet()).show {
                            title(R.string.attachments)
                            listItems(items = detail.files.map { it.second })
                        }
                    }
                } ?: kotlin.run {
                    binding?.content?.text = ""
                    binding?.progress?.show()
                    binding?.showDocuments?.setOnClickListener {

                    }
                }
            })

            viewModel.setEmail(args.email)
            viewModel.getEmailDetail(args.email)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.email_detail__menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reply -> {
                viewModel.getEmail()?.let {
                    handler.reply(it)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override val viewModel: EmailDetailViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): EmailDetailFragmentBinding =
        EmailDetailFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: EmailDetailHandler
    override fun getAnalytics() = FirebaseAnalytics.getInstance(requireContext())
}

interface EmailDetailHandler : BaseFragmentHandler {
    fun reply(email: Email)
}