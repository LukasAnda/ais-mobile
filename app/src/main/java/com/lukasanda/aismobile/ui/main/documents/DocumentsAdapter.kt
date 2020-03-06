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

package com.lukasanda.aismobile.ui.main.documents

import android.view.ViewGroup
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.Document
import com.lukasanda.aismobile.databinding.DocumentItemBinding
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import sk.lukasanda.base.ui.recyclerview.BaseAdapter
import sk.lukasanda.base.ui.recyclerview.BindingViewHolder
import sk.lukasanda.base.ui.recyclerview.create
import java.util.*

class DocumentsAdapter(onClick: (Document) -> Unit) : BaseAdapter<Document, Document, DocumentItemHolder>(onClick) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = parent.create(::DocumentItemHolder, DocumentItemBinding::inflate)
}

class DocumentItemHolder(binding: DocumentItemBinding) : BindingViewHolder<Document, DocumentItemBinding>(binding) {
    override fun bind(item: Document, onClick: (Document) -> Unit) {
        if (item.openable) {
            binding.icon.hide()
            binding.iconText.show()
            binding.iconText.text = item.mimeType.substringAfter("-").toUpperCase(Locale.getDefault())
        } else {
            binding.iconText.hide()
            binding.icon.show()
            binding.icon.setImageResource(R.drawable.ic_folder)
        }
        binding.iconBackground.setBackgroundColor(ColorGenerator.MATERIAL.getColor(item.mimeType))
        binding.name.text = item.name

        binding.root.setOnClickListener {
            onClick(item)
        }
    }

}