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

package sk.lukasanda.dataprovider.data


import com.google.gson.annotations.SerializedName

data class PeriodicLessonsItem(@SerializedName("note")
                               val note: String = "",
                               @SerializedName("week")
                               val week: String = "",
                               @SerializedName("campus")
                               val campus: String = "",
                               @SerializedName("courseCode")
                               val courseCode: String = "",
                               @SerializedName("typeName")
                               val typeName: String = "",
                               @SerializedName("room")
                               val room: String = "",
                               @SerializedName("isSeminar")
                               val isSeminar: String = "",
                               @SerializedName("courseName")
                               val courseName: String = "",
                               @SerializedName("dayOfWeek")
                               val dayOfWeek: String = "",
                               @SerializedName("teachers")
                               val teachers: List<TeachersItem>?,
                               @SerializedName("roomStructured")
                               val roomStructured: RoomStructured,
                               @SerializedName("isDefaultCampus")
                               val isDefaultCampus: String = "",
                               @SerializedName("periodicity")
                               val periodicity: Int = 0,
                               @SerializedName("startTime")
                               val startTime: String = "",
                               @SerializedName("endTime")
                               val endTime: String = "",
                               @SerializedName("courseId")
                               val courseId: String = "",
                               @SerializedName("facultyCode")
                               val facultyCode: String = "")


data class RoomStructured(@SerializedName("name")
                          val name: String = "",
                          @SerializedName("id")
                          val id: String = "")


data class Schedule(@SerializedName("periodicLessons")
                    val periodicLessons: List<PeriodicLessonsItem>?,
                    @SerializedName("modificationDate")
                    val modificationDate: String = "")


data class TeachersItem(@SerializedName("fullName")
                        val fullName: String = "",
                        @SerializedName("id")
                        val id: String = "",
                        @SerializedName("shortName")
                        val shortName: String = "")


