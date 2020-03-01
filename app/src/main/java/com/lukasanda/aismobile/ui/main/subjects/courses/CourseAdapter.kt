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

import android.util.Log
import android.view.ViewGroup
import com.lukasanda.aismobile.data.db.entity.FullCourse
import com.lukasanda.aismobile.databinding.CourseItemBinding
import sk.lukasanda.base.ui.recyclerview.BaseAdapter
import sk.lukasanda.base.ui.recyclerview.BindingViewHolder
import sk.lukasanda.base.ui.recyclerview.create

class CourseAdapter(private val listener: (FullCourse) -> Unit) :
    BaseAdapter<FullCourse, FullCourse, CourseViewHolder>(listener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::CourseViewHolder, CourseItemBinding::inflate)
}

class CourseViewHolder(binding: CourseItemBinding) :
    BindingViewHolder<FullCourse, CourseItemBinding>(binding) {

    override fun bind(item: FullCourse, onClick: (FullCourse) -> Unit) {
        with(binding) {
            name.text = item.course.courseName
            root.setOnClickListener {
                Log.d("TAG", "On Click invoked")
                onClick.invoke(item)
            }
        }
    }
}