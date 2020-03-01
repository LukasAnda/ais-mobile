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

package com.lukasanda.aismobile.ui.main.subjectDetail.adapters

import android.view.ViewGroup
import com.lukasanda.aismobile.data.db.entity.Sheet
import com.lukasanda.aismobile.databinding.SubjectTableItemBinding
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import com.lukasanda.aismobile.view.TableRowView
import sk.lukasanda.base.ui.recyclerview.BaseAdapter
import sk.lukasanda.base.ui.recyclerview.BindingViewHolder
import sk.lukasanda.base.ui.recyclerview.create

class SubjectTablesAdapter(private val listener: (Sheet) -> Unit) :
    BaseAdapter<Sheet, Sheet, SubjectTableItemHolder>(listener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::SubjectTableItemHolder, SubjectTableItemBinding::inflate)
}

class SubjectTableItemHolder(binding: SubjectTableItemBinding) :
    BindingViewHolder<Sheet, SubjectTableItemBinding>(binding) {
    override fun bind(item: Sheet, onClick: (Sheet) -> Unit) {
        binding.tableName.text = item.name
        binding.tableComment.text = item.comments()

        if (item.comments().isEmpty()) {
            binding.tableComment.hide()
        } else {
            binding.tableComment.show()
        }

        binding.rowsContainer.removeAllViews()
        item.getColumnPairs().filterNot { it.second.isBlank() }.forEach {
            binding.rowsContainer.addView(TableRowView(binding.root.context).apply {
                setData(TableRowView.Config(it.first, it.second))
            })
        }
    }

}