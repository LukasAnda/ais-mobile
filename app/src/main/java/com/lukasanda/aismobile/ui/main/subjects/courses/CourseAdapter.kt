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

package com.lukasanda.aismobile.ui.main.subjects.courses

import android.view.ViewGroup
import com.lukasanda.aismobile.data.db.entity.FullCourse
import com.lukasanda.aismobile.databinding.CourseItemBinding
import com.lukasanda.aismobile.ui.recyclerview.BaseAdapter
import com.lukasanda.aismobile.ui.recyclerview.BindingViewHolder
import com.lukasanda.aismobile.ui.recyclerview.create
import com.lukasanda.aismobile.util.getTextDrawable

class CourseAdapter(private val listener: (FullCourse) -> Unit) :
    BaseAdapter<FullCourse, FullCourse, CourseViewHolder>(listener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::CourseViewHolder, CourseItemBinding::inflate)
}

class CourseViewHolder(binding: CourseItemBinding) :
    BindingViewHolder<FullCourse, CourseItemBinding>(binding) {

    override fun bind(item: FullCourse, onClick: (FullCourse) -> Unit) {
        with(binding) {
            name.text = item.course.courseName.split(" ").drop(1).joinToString(" ")
            teacher.text = item.teachers.lastOrNull()?.name ?: "-"
            icon.setImageDrawable(
                getTextDrawable(
                    item.course.courseName.split(" ").first().substringBefore(
                        "_"
                    ), item.course.courseName, 30
                )
            )
            root.setOnClickListener {
                onClick.invoke(item)
            }
        }
    }
}