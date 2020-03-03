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

import android.graphics.Color
import android.view.ViewGroup
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.lukasanda.aismobile.data.db.entity.Teacher
import com.lukasanda.aismobile.databinding.TeacherContactItemBinding
import com.lukasanda.aismobile.util.getInitialsFromName
import com.lukasanda.aismobile.util.getNameFromSender
import sk.lukasanda.base.ui.recyclerview.BaseAdapter
import sk.lukasanda.base.ui.recyclerview.BindingViewHolder
import sk.lukasanda.base.ui.recyclerview.create

class SubjectTeachersAdapter(listener: (Teacher) -> Unit) :
    BaseAdapter<Teacher, Teacher, TeacherItemHolder>(listener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        parent.create(::TeacherItemHolder, TeacherContactItemBinding::inflate)

}

class TeacherItemHolder(binding: TeacherContactItemBinding) :
    BindingViewHolder<Teacher, TeacherContactItemBinding>(binding) {
    override fun bind(item: Teacher, onClick: (Teacher) -> Unit) {
        binding.name.text = item.name

        val name = item.name.getNameFromSender()
        val initials = name.getInitialsFromName()
        val drawable =
            TextDrawable.builder().beginConfig().textColor(Color.WHITE).bold().toUpperCase()
                .endConfig()
                .buildRound(initials, ColorGenerator.MATERIAL.getColor(item.name))
        binding.icon.setImageDrawable(drawable)

        binding.root.setOnClickListener {
            onClick(item)
        }
    }

}