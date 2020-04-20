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

import com.lukasanda.aismobile.ui.recyclerview.DiffUtilItem

data class ProfileInfo(val id: String, val name: String, val info: List<ProfileInfoItem>)

data class ProfileInfoItem(val info: Pair<Int, String>) : DiffUtilItem {
    override fun getContentDescription() = info.second
    override fun getUniqueId() = info.first.toString()
}