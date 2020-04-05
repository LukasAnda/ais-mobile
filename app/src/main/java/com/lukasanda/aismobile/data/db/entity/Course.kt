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

package com.lukasanda.aismobile.data.db.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.lukasanda.aismobile.ui.recyclerview.DiffUtilItem
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "course")
data class Course(
    @PrimaryKey(autoGenerate = false) val id: String = "",
    val courseName: String = "",
    val coursePresence: String = "", //Presence separated by "#"
    val seminarPresence: String = "", //Presence separated by "#"
    val documentsId: String = "",
    val hasAISTests: Boolean = false,
    val hasSheets: Boolean = false,
    val study: String = "",
    val semester: String = "",
    val semesterId: Int = 0,
    val finalMark: String = "-"
)

class FullCourse : DiffUtilItem {
    @Embedded
    var course: Course = Course()

    @Relation(parentColumn = "id", entityColumn = "courseId")
    var sheets: List<Sheet> = emptyList()

    @Relation(parentColumn = "id", entityColumn = "courseId")
    var timetable: List<TimetableItem> = emptyList()

    @Relation(parentColumn = "id", entityColumn = "courseId")
    var teachers: List<Teacher> = emptyList()

    override fun toString(): String {
        return course.courseName + "\n" + sheets.joinToString("\n") + "\n" + teachers.joinToString(
            "\n"
        )
    }

    override fun getContentDescription() = "$course ${sheets.joinToString(" ")} ${timetable.joinToString(" ")} ${teachers.joinToString(" ")}"
}

data class Semester(val courses: List<FullCourse>) : DiffUtilItem {
    override fun getContentDescription() = courses.map { it.getContentDescription() }.joinToString(" ")
}

@Entity(tableName = "sheet")
data class Sheet(
    @PrimaryKey(autoGenerate = false) val id: String = "",
    val courseId: String = "",
    val name: String = "",
    val comment: String = "",
    val headers: String = "", // headers sepatated by #
    val values: String = "" //values separated by #
) : DiffUtilItem {
    private fun headers() = headers.split("#")
    private fun values() = values.split("#")

    fun getColumnPairs() = headers().zip(values()) { a, b ->
        Pair(
            a,
            b
        )
    }.filterNot { it.first.equals("Poznámka") || it.first.equals("Zlučka") }

    fun comments() = comment.takeIf { it.isNotEmpty() } ?: run {
        val index = headers().indexOf("Poznámka")
        if (index > -1) {
            return@run values()[index]
        }
        ""
    }

    override fun getContentDescription() = "$courseId $name ${comments()} $headers $values"

    override fun toString(): String {
        return "Name: $name \n Values: ${getColumnPairs()}\nComment: ${comments()}\n\n"
    }
}

@Parcelize
@Entity(tableName = "teacher", primaryKeys = ["id", "courseId"])
data class Teacher(
    val id: String,
    val name: String,
    val courseId: String
) : Parcelable, DiffUtilItem {
    fun isLector() = name.contains("prednášajúci")

    override fun getContentDescription() = "$name $courseId"
}