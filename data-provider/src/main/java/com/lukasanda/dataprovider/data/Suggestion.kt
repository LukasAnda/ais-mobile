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

package com.lukasanda.dataprovider.data

import com.google.gson.annotations.SerializedName


data class SuggestionResult(
    @SerializedName("data")
    val data: List<List<String>> = listOf(),
    @SerializedName("more")
    val more: Int = 0
) {
    fun toSuggestions() = data.map {
        Suggestion(
            it.component1(),
            it.component2(),
            it.component4()
        )
    }
}

data class Suggestion(val name: String, val id: String, val study: String)

