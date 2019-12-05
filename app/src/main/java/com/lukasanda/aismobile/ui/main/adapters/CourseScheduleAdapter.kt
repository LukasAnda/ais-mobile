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

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.Course
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import kotlinx.android.synthetic.main.item_course_schedule.view.*
import org.joda.time.DateTime

class CourseScheduleAdapter : RecyclerView.Adapter<CourseScheduleAdapter.CourseViewHolder>() {

    private var data: MutableList<Course> = mutableListOf()
    private val actualDay = DateTime.now().dayOfWeek
    private val actualTime = DateTime.now()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        return CourseViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_course_schedule, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) =
        holder.bind(actualDay, actualTime, data[position])

    fun swapData(data: List<Course>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(actualDay: Int, actualTime: DateTime, item: Course) = with(itemView) {
            name.text = item.name
            time.text = "${item.startTime} - ${item.endTime}"
            place.text = item.place
            teacher.text = item.teacher

            if (!item.isSeminar) {
                root.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.color_seminar
                    )
                )
            } else {
                root.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.color_practice
                    )
                )
            }
            val hourBeforeStart = DateTime.now().withHourOfDay(
                item.startTime.substringBefore(
                    ":"
                ).toInt()
            ).withMinuteOfHour(item.startTime.substringAfter(":").toInt()).minusHours(1)

            if (item.dayOfWeek == actualDay && hourBeforeStart.isBeforeNow && hourBeforeStart.plusHours(
                    1
                ).isAfterNow
            ) {
                Log.d("TAG", "ID: ${item.acronym} -> ${hourBeforeStart.toString()}")
                nextLesson.show()
            } else {
                nextLesson.hide()
            }

            setOnClickListener {
                // TODO: Handle on click
            }
        }
    }
}