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

package sk.lukasanda.dataprovider

import com.google.gson.Gson
import org.htmlcleaner.ContentToken
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import sk.lukasanda.dataprovider.data.Schedule
import sk.lukasanda.dataprovider.data.WifiInfo
import java.lang.Exception

object Parser {

    fun getSchedule(webResponse: String): Schedule? {
        if (webResponse.isEmpty()) return null
        val gson = Gson()
        val schedule = gson.fromJson<Schedule>(webResponse, Schedule::class.java)
        return schedule
    }

    fun getId(webResponse: String): Int? {
        if (webResponse.isEmpty()) return null
        val cleaner = HtmlCleaner()

        return try {
            val items = cleaner.clean(webResponse)
            val node =
                items.evaluateXPath("/body/div[2]/div/div/form/table[2]/tbody/tr[1]/td[2]/small").first() as TagNode
            val id = node.children.first() as ContentToken
            id.content.toInt()
        } catch (e: Exception) {
            null
        }
    }

    fun getWifiInfo(webResponse: String): WifiInfo? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()

        return try {
            val items = cleaner.clean(webResponse)
            val node =
                items.evaluateXPath("/body/div[2]/div/div/form/table/tbody/tr[1]/td[2]/small/b").first() as TagNode
            val name = node.children.first() as ContentToken

            val node2 =
                items.evaluateXPath("/body/div[2]/div/div/form/table/tbody/tr[2]/td[2]/small/b").first() as TagNode
            val password = node2.children.first() as ContentToken



            WifiInfo(name.content, password.content)
        } catch (e: Exception) {
            null
        }
    }
}
