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

package sk.lukasanda.dataprovider.data

data class Course(
    val coursePresence: String = "", //Presence separated by "#
    var seminarPresence: String = "", //Presence separated by "#
    val courseId: String = "",
    val courseName: String = "",
    val documentsId: String = "",
    val hasAISTests: Boolean = false,
    val hasSheets: Boolean = false

)

data class Sheet(
    val name: String = "",
    val headers: String = "", // headers sepatated by #
    val values: String = "" //values separated by #
) {
    fun headers() = headers.split("#")
    fun values() = values.split("#")

    override fun toString(): String {
        return name + "\n" + headers().zip(values()).joinToString("\n")
    }
}

data class Semester(var studiesId: String, val id: String, val name: String)
data class Study(val id: String, val semesterCount: Int)