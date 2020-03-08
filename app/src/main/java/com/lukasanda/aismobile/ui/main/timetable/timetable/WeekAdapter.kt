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
import com.lukasanda.aismobile.ui.recyclerview.BaseAdapter
import com.lukasanda.aismobile.ui.recyclerview.BaseBindingViewHolder
import com.lukasanda.aismobile.ui.recyclerview.create
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show

class WeekAdapter(private val listener: (TimetableItem) -> Unit) :
    BaseAdapter<List<TimetableItem>, TimetableItem, WeekItemHolder>(listener) {
    private val pool = RecyclerView.RecycledViewPool()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::WeekItemHolder, WeekItemBinding::inflate).apply {
            binding.recycler.setRecycledViewPool(pool)
        }

    override fun onViewRecycled(holder: WeekItemHolder) {
        super.onViewRecycled(holder)
        holder.binding.recycler.adapter = null
    }

    override fun getItemCount() = Int.MAX_VALUE

    override fun onBindViewHolder(holder: WeekItemHolder, position: Int) {
        if (items.isNotEmpty()) {
            holder.bind(items[position % items.size], listener)
        }
    }
}

class WeekItemHolder(binding: WeekItemBinding) :
    BaseBindingViewHolder<List<TimetableItem>, TimetableItem, WeekItemBinding>(binding) {
    override fun bind(item: List<TimetableItem>, onClick: (TimetableItem) -> Unit) {
        val adapter = TimetableAdapter(onClick)
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