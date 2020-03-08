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
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.Document
import com.lukasanda.aismobile.databinding.DocumentItemBinding
import com.lukasanda.aismobile.ui.recyclerview.BaseAdapter
import com.lukasanda.aismobile.ui.recyclerview.BaseBindingViewHolder
import com.lukasanda.aismobile.ui.recyclerview.create
import com.lukasanda.aismobile.util.getMimeColor
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import java.util.*

sealed class Either<out A, out B> {
    class Left<A>(val value: A) : Either<A, Nothing>()
    class Right<B>(val value: B) : Either<Nothing, B>()
}

class DocumentsAdapter(onClick: (Document?) -> Unit) : BaseAdapter<Either<Unit, Document>, Document?, DocumentItemHolder>(onClick) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = parent.create(::DocumentItemHolder, DocumentItemBinding::inflate)
}

class DocumentItemHolder(binding: DocumentItemBinding) : BaseBindingViewHolder<Either<Unit, Document>, Document?, DocumentItemBinding>(binding) {
    override fun bind(item: Either<Unit, Document>, onClick: (Document?) -> Unit) {
        when (item) {
            is Either.Left -> {
                binding.name.text = "Back"
                binding.icon.setImageResource(R.drawable.ic_back)
                binding.iconBackground.setBackgroundColor(binding.iconBackground.context.getMimeColor(""))
                binding.iconText.hide()
                binding.root.setOnClickListener {
                    onClick(null)
                }
            }
            is Either.Right -> {
                if (item.value.openable) {
                    binding.icon.hide()
                    binding.iconText.show()
                    binding.iconText.text = item.value.mimeType.substringAfter("-").takeUnless { it == "unknown" }?.toUpperCase(Locale.getDefault()) ?: "?"
                } else {
                    binding.iconText.hide()
                    binding.icon.show()
                    binding.icon.setImageResource(R.drawable.ic_folder)
                }
                binding.iconBackground.setBackgroundColor(binding.iconBackground.context.getMimeColor(item.value.mimeType))
                binding.name.text = item.value.name

                binding.root.setOnClickListener {
                    onClick(item.value)
                }
            }
        }
    }
}