/*
 * Copyright 2019 Lukáš Anda. All rights reserved.
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

package com.lukasanda.aismobile.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.Course
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import kotlinx.android.synthetic.main.item_week.view.*

class CourseWeekAdapter : RecyclerView.Adapter<CourseWeekAdapter.CourseWeekViewHolder>() {
    private val days = mutableListOf<List<Course>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CourseWeekViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_week, parent, false
        )
    )

    override fun getItemCount() = Int.MAX_VALUE

    override fun onBindViewHolder(holder: CourseWeekViewHolder, position: Int) {
        if (days.size > 0) {
            holder.bind(days[position % days.size])
        }
    }

    fun swapData(data: List<List<Course>>) {
        days.clear()
        days.addAll(data)
        notifyDataSetChanged()
    }

    class CourseWeekViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val adapter = CourseScheduleAdapter()
        fun bind(schedule: List<Course>) {
            if (schedule.isEmpty()) {
                itemView.empty.show()
                itemView.recycler.hide()
            } else {
                itemView.empty.hide()
                itemView.recycler.show()
                itemView.recycler.apply {
                    adapter = this@CourseWeekViewHolder.adapter
                    layoutManager = LinearLayoutManager(context)
                }
                adapter.swapData(schedule)
            }
        }
    }

}