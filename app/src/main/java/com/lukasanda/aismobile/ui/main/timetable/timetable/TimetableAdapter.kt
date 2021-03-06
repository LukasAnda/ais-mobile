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
import androidx.core.content.ContextCompat
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.TimetableItem
import com.lukasanda.aismobile.databinding.ScheduleItemBinding
import com.lukasanda.aismobile.ui.recyclerview.BaseAdapter
import com.lukasanda.aismobile.ui.recyclerview.BindingViewHolder
import com.lukasanda.aismobile.ui.recyclerview.create
import com.lukasanda.aismobile.util.dp

class TimetableAdapter(private val listener: (TimetableItem) -> Unit) :
    BaseAdapter<TimetableItem, TimetableItem, TimetableCourseViewHolder>(listener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::TimetableCourseViewHolder, ScheduleItemBinding::inflate)
}

class TimetableCourseViewHolder(binding: ScheduleItemBinding) :
    BindingViewHolder<TimetableItem, ScheduleItemBinding>(binding) {

    override fun bind(item: TimetableItem, onClick: (TimetableItem) -> Unit) {
        with(binding) {
            name.text = item.name
            time.text = "${item.startTime} - ${item.endTime}"
            place.text = item.place
            teacher.text = item.teacher

            if (!item.isSeminar) {
                root.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.color_seminar
                    )
                )
            } else {
                root.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.color_practice
                    )
                )
            }

            if (item.actual) {
                root.strokeWidth = 2.dp
            } else {
                root.strokeWidth = 0
            }

            binding.root.setOnClickListener {
                onClick.invoke(item)
            }
        }
    }
}