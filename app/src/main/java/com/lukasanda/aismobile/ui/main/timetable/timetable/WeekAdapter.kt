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

package com.lukasanda.aismobile.ui.main.timetable.timetable

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lukasanda.aismobile.data.db.entity.TimetableItem
import com.lukasanda.aismobile.databinding.WeekItemBinding
import sk.lukasanda.base.ui.recyclerview.BaseAdapter
import sk.lukasanda.base.ui.recyclerview.BaseBindingViewHolder
import sk.lukasanda.base.ui.recyclerview.create

class WeekAdapter : BaseAdapter<List<TimetableItem>, TimetableItem, WeekItemHolder>() {
    private val pool = RecyclerView.RecycledViewPool()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::WeekItemHolder, WeekItemBinding::inflate).apply {
            binding.recycler.setRecycledViewPool(pool)
        }

    override fun getItemCount() = Int.MAX_VALUE

    override fun onBindViewHolder(holder: WeekItemHolder, position: Int) {
        if (items.isNotEmpty()) {
            holder.bind(items[position % items.size])
        }
    }
}

class WeekItemHolder(binding: WeekItemBinding) :
    BaseBindingViewHolder<List<TimetableItem>, TimetableItem, WeekItemBinding>(binding) {
    override fun bind(item: List<TimetableItem>, onClick: ((TimetableItem) -> Unit)?) {
        val adapter = TimetableAdapter()
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        adapter.swapData(item)

        if (item.isEmpty()) {
            binding.empty.show()
            binding.recycler.hide()
        } else {
            binding.empty.hide()
            binding.recycler.show()
        }
    }
}