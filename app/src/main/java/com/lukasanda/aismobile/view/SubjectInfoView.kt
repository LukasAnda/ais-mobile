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

package com.lukasanda.aismobile.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.lukasanda.aismobile.data.db.entity.FullCourse
import com.lukasanda.aismobile.databinding.SubjectInfoViewBinding
import com.lukasanda.aismobile.util.getTextDrawable
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show

class SubjectInfoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding = SubjectInfoViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun setData(course: FullCourse) {
        binding.apply {
            courseName.text = course.course.courseName.split(" ").drop(1).joinToString(" ")
            icon.setImageDrawable(
                getTextDrawable(
                    course.course.courseName.split(" ").first().substringBefore(
                        "_"
                    ), course.course.courseName, 60
                )
            )

            courseTeacher.text =
                course.teachers.lastOrNull()?.name

            course.course.coursePresence.split("#").map { it.toPresenceType() }.takeIf { it.size > 1 }?.let {
                coursePresenceGroup.show()
                coursePresence.setPresence(it)
            } ?: run {
                coursePresenceGroup.hide()
            }

            course.course.seminarPresence.split("#").map { it.toPresenceType() }.takeIf { it.size > 1 }?.let {
                seminarPresenceGroup.show()
                seminarPresence.setPresence(it)
            } ?: run {
                seminarPresenceGroup.hide()
            }
        }
    }
}