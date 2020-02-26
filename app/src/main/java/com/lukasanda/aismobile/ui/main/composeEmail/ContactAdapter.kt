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

import android.graphics.Color
import android.view.ViewGroup
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.lukasanda.aismobile.databinding.ContactItemBinding
import sk.lukasanda.base.ui.recyclerview.BaseAdapter
import sk.lukasanda.base.ui.recyclerview.BindingViewHolder
import sk.lukasanda.base.ui.recyclerview.create
import sk.lukasanda.dataprovider.data.Suggestion

class ContactAdapter(private val listener: (Suggestion) -> Unit) :
    BaseAdapter<Suggestion, Suggestion, ContactItemHolder>(listener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::ContactItemHolder, ContactItemBinding::inflate)
}

class ContactItemHolder(binding: ContactItemBinding) :
    BindingViewHolder<Suggestion, ContactItemBinding>(binding) {
    override fun bind(item: Suggestion, onClick: ((Suggestion) -> Unit)?) {
        binding.name.text = item.name
        val drawable =
            TextDrawable.builder().beginConfig().textColor(Color.WHITE).bold().toUpperCase()
                .endConfig().buildRound(
                    item.name.first().toString(),
                    ColorGenerator.MATERIAL.getColor(item.name)
                )
        binding.icon.setImageDrawable(drawable)

        binding.root.setOnClickListener {
            onClick?.invoke(item)
        }
    }

}