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

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.android.material.chip.Chip
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.databinding.ComposeEmailFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import com.lukasanda.aismobile.ui.main.composeEmail.ComposeEmailViewModel.EmailSendState.Fail
import com.lukasanda.aismobile.ui.main.composeEmail.ComposeEmailViewModel.EmailSendState.Success
import com.lukasanda.aismobile.ui.recyclerview.bindLinear
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import com.lukasanda.dataprovider.data.Suggestion
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ComposeEmailFragment :
    BaseFragment<ComposeEmailFragment.Views, ComposeEmailFragmentBinding, ComposeEmailViewModel, ComposeEmailHandler>() {

    private val selected = mutableListOf<Suggestion>()

    private lateinit var type: SendType


    private val contactAdapter: ContactAdapter = ContactAdapter {
        selected.add(it)
        binding?.recipients?.setText("")
        binding?.chipGroup?.addView(createChip(it))
        binding?.chipGroup?.show()
        clearLoading()
    }

    private fun clearLoading() {
        viewModel.cancelJobs()
        contactAdapter.swapData(emptyList())
        binding?.contactsRecycler?.hide()
    }

    private fun removeLastContact() {
        binding?.contactsRecycler?.hide()
        val viewCount = binding?.chipGroup?.childCount ?: 0
        if (viewCount > 0) {
            selected.dropLast(1)
            binding?.chipGroup?.removeViewAt(viewCount - 1)

            if (binding?.chipGroup?.isEmpty() == true) {
                binding?.chipGroup?.hide()
            }
        } else {
            binding?.chipGroup?.hide()
        }
    }

    inner class Views : BaseViews {
        override fun modifyViews() {
            setHasOptionsMenu(true)

            val args by navArgs<ComposeEmailFragmentArgs>()

            args.teacher?.let {
                val suggestion = Suggestion(it.name, it.id, "")
                selected.add(suggestion)
                binding?.chipGroup?.addView(createChip(suggestion))
                binding?.chipGroup?.show()

                binding?.recipients?.hide()

                binding?.recipients?.isEnabled = false
            }

            type = args.email?.let {
                if (it.subject.contains("Re:")) {
                    binding?.subject?.setText(it.subject)
                } else {
                    binding?.subject?.setText("Re: ${it.subject}")
                }
                binding?.subject?.isEnabled = false
                binding?.subject?.isClickable = false

                val suggestion = Suggestion(it.sender, it.senderId, "")
                selected.add(suggestion)
                binding?.chipGroup?.addView(createChip(suggestion))
                binding?.chipGroup?.show()

                binding?.recipients?.hide()
                binding?.recipients?.isEnabled = false

                SendType.Reply(it)
            } ?: run {
                SendType.Send()
            }

            binding?.recipients?.apply {
                doOnTextChanged { text, start, count, after ->
                    if (text?.length ?: 0 > 2) {
                        viewModel.getSuggestions(text.toString())
                    } else {
                        viewModel.cancelJobs()
                        clearLoading()
                    }
                }
                setOnKeyListener { v, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && binding?.recipients?.text?.isEmpty() == true) {
                        removeLastContact()
                    }
                    false
                }
            }

            binding?.contactsRecycler?.apply {
                bindLinear(contactAdapter)
                addItemDecoration(
                    DividerItemDecoration(
                        this.context,
                        DividerItemDecoration.VERTICAL
                    )
                )
            }

            viewModel.suggestions().observe(viewLifecycleOwner, Observer {
                if (it.isEmpty()) {
                    binding?.contactsRecycler?.hide()
                } else {
                    binding?.contactsRecycler?.show()
                }
                contactAdapter.swapData(it)
            })

            viewModel.sentMailState().observe(viewLifecycleOwner, Observer { emailSendState ->
                when (emailSendState) {
                    Success -> handler.closeFragment()
                    Fail -> {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong, try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.compose_email__menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.send -> {
                when (type) {
                    is SendType.Reply -> {
                        viewModel.replyMail(
                            getEmails(),
                            binding?.subject?.text.toString(),
                            binding?.message?.text.toString(),
                            (type as SendType.Reply).email
                        )
                    }
                    is SendType.Send -> {
                        viewModel.sendMail(
                            getEmails(),
                            binding?.subject?.text.toString(),
                            binding?.message?.text.toString()
                        )
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getEmails(): String {
        val builder = StringBuilder()
        selected.forEach { builder.append("${it.id}@is.stuba.sk").append(", ") }
        return builder.toString().substringBeforeLast(",")
    }

    private fun createChip(suggestion: Suggestion): Chip {
        val drawable =
            TextDrawable.builder().beginConfig().textColor(Color.WHITE).fontSize(40).bold()
                .toUpperCase()
                .endConfig()
                .buildRound(
                    suggestion.name.first().toString(),
                    ColorGenerator.MATERIAL.getColor(suggestion.name)
                )

        val chip = Chip(requireContext()).apply {
            chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
            chipStrokeColor = ColorStateList.valueOf(Color.GRAY)
            chipStrokeWidth = 1f
            text = suggestion.name
            setTextColor(Color.BLACK)
            setEnsureMinTouchTargetSize(false)
            chipIcon = drawable
        }
        chip.setPaddingRelative(chip.paddingStart, 0, chip.paddingEnd, 0)
        return chip
    }

    override val viewModel by viewModel<ComposeEmailViewModel> { parametersOf(Bundle()) }

    override fun setBinding(): ComposeEmailFragmentBinding =
        ComposeEmailFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: ComposeEmailHandler

    sealed class SendType {
        class Reply(val email: Email) : SendType()
        class Send() : SendType()
    }

}

interface ComposeEmailHandler {
    fun closeFragment()
}