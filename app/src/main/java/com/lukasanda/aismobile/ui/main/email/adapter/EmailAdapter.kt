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

package com.lukasanda.aismobile.ui.main.email.adapter

import android.graphics.Color
import android.view.ViewGroup
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.databinding.EmailItemBinding
import com.lukasanda.aismobile.util.getInitialsFromName
import com.lukasanda.aismobile.util.getNameFromSender
import sk.lukasanda.base.ui.recyclerview.BaseAdapter
import sk.lukasanda.base.ui.recyclerview.BindingViewHolder
import sk.lukasanda.base.ui.recyclerview.create

class EmailAdapter(listener: (Email) -> Unit) :
    BaseAdapter<Email, Email, EmailItemHolder>(listener) {
    fun getEmailAt(position: Int) = items[position]
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::EmailItemHolder, EmailItemBinding::inflate)
}

class EmailItemHolder(binding: EmailItemBinding) :
    BindingViewHolder<Email, EmailItemBinding>(binding) {
    override fun bind(item: Email, onClick: (Email) -> Unit) {
        binding.name.text = item.sender
        binding.subject.text = item.subject
        val name = item.sender.getNameFromSender()
        val initials = name.getInitialsFromName()
        val drawable =
            TextDrawable.builder().beginConfig().textColor(Color.WHITE).bold().toUpperCase()
                .endConfig()
                .buildRound(initials, ColorGenerator.MATERIAL.getColor(item.sender))

        binding.icon.setImageDrawable(drawable)

        if (item.opened) {
            binding.name.setTextAppearance(binding.name.context, R.style.regular__text_view)
            binding.subject.setTextAppearance(binding.name.context, R.style.regular__text_view)
        } else {
            binding.name.setTextAppearance(binding.name.context, R.style.bold__text_view)
            binding.subject.setTextAppearance(binding.name.context, R.style.bold__text_view)
        }

        binding.root.setOnClickListener {
            onClick?.invoke(item)
        }
    }
}