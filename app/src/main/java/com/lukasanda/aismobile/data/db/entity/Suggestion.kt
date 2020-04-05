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
import com.lukasanda.aismobile.ui.recyclerview.DiffUtilItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Suggestion(val name: String, val id: String, val study: String) : Parcelable, DiffUtilItem {
    override fun getContentDescription() = "$name $id $study"
}