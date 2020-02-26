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

package sk.lukasanda.dataprovider

import com.google.gson.Gson
import org.htmlcleaner.ContentNode
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import sk.lukasanda.dataprovider.data.*

object Parser {

    fun getSchedule(webResponse: String): Schedule? {
        if (webResponse.isEmpty()) return null
        val gson = Gson()
        return try {
            gson.fromJson<Schedule>(webResponse, Schedule::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getId(webResponse: String): Int? {
        if (webResponse.isEmpty()) return null
        val cleaner = HtmlCleaner()

        return try {
            val items = cleaner.clean(webResponse)
            val node =
                items.evaluateXPath("/body/div[2]/div/div/form/table[2]/tbody/tr[1]/td[2]/small").first() as TagNode
            val id = node.allChildren.first() as ContentNode
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
            val name = node.allChildren.first() as ContentNode

            val node2 =
                items.evaluateXPath("/body/div[2]/div/div/form/table/tbody/tr[2]/td[2]/small/b").first() as TagNode
            val password = node2.allChildren.first() as ContentNode


            WifiInfo(name.content, password.content)
        } catch (e: Exception) {
            null
        }
    }

    fun TagNode.content(): String = try {
        (allChildren.first() as ContentNode).content
    } catch (e: Exception) {
        ""
    }

    private fun String.getSemesterCount() =
        substringAfter("[").substringBefore(",").substringAfter(" ").toInt()

    fun getSemesters(webResponse: String): List<Semester>? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()
        return try {
            val items = cleaner.clean(webResponse)
            val studies = items.findElementByAttValue("name", "studium", true, true)

            val studiesParent = studies.parent

            //We have someone on Master's/Doctor's studies
            val hasStudiesSelector = studies.name == "select"

            //if semesters == null, then we have only the first semester, therefore we don't need to pass anything
            val semesters =
                items.findElementByAttValue("name", "obdobi", true, true) ?: return listOf(
                    Semester("", "", studiesParent.childTagList.last().content())
                )
            val semesterList = semesters.childTagList.map {
                Semester(
                    "",
                    it.getAttributeByName("value"),
                    it.content()
                )
            }

            val studiesList = if (hasStudiesSelector) studies.childTagList.map {
                Study(
                    it.getAttributeByName("value"),
                    it.content().getSemesterCount()
                )
            } else return semesterList

            var studyIndex = 0
            var nextIndex = studiesList[studyIndex].semesterCount

            semesterList.mapIndexed { index, semester ->
                if (index + 1 > nextIndex) {
                    studyIndex++
                    nextIndex += studiesList[studyIndex].semesterCount
                }

                semester.copy(studiesId = studiesList[studyIndex].id)
            }

            semesterList
        } catch (e: Exception) {
            null
        }
    }

    fun TagNode.usingAttribude(attrName: String, attrValue: String) =
        this.findElementByAttValue(attrName, attrValue, true, true)

    fun getCourses(webResponse: String): List<Course>? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()
        val returnList = mutableListOf<Course>()
        return try {
            val items = cleaner.clean(webResponse)
            val tableBody = items.usingAttribude("id", "tmtab_1").childTagList.last()

            val children = tableBody.allChildren.size

            for (i in 2 until children) {
                val row = tableBody.getElementListByName("tr", false)[i]
                val subject = row.findElementByName("a", true)

                if (subject != null) {
                    //Ak je to tagnode, tak potom yay, nasli sme meno predmetu a tento moj "row" obsahuje vrchy riadok/jediny riadok
                    val id = subject.getAttributeByName("href").substringAfterLast("=")

                    val name = subject.content().replace("&nbsp;", " ")

                    val builder = StringBuilder()
                    for (j in 2 until row.allChildren.size - 5) {
                        val presenceItem = row.childTagList[j].findElementByName("img", true)
                        if (presenceItem != null) {
                            //Je zaznamenany udaj o dochadzke
                            val type = presenceItem.getAttributeByName("sysid")
                            builder.append(type)
                        }
                        builder.append("#")
                    }

                    val evaluationButton =
                        row.childTagList[row.allChildren.size - 5].findElementByName("img", true)
                    val aisTestsButton =
                        row.childTagList[row.allChildren.size - 2].findElementByName("img", true)
                    val documentsButton =
                        row.childTagList[row.allChildren.size - 1].findElementByName("a", true)

                    val documentsId = try {
                        documentsButton.getAttributeByName("href").substringAfterLast("=")
                    } catch (e: Exception) {
                        ""
                    }

                    returnList.add(
                        Course(
                            builder.toString(),
                            "",
                            id,
                            name,
                            documentsId,
                            aisTestsButton != null,
                            evaluationButton != null
                        )
                    )
                } else {
                    //Ak to nie je tagnode tak potom to nenaslo a sice je to druhy riadok pre cvika
                    val builder = StringBuilder()
                    for (j in 1 until row.allChildren.size) {
                        val presenceItem = row.childTagList[j].findElementByName("img", true)
                        if (presenceItem != null) {
                            //Je zaznamenany udaj o dochadzke
                            val type = presenceItem.getAttributeByName("sysid")
                            builder.append(type)
                        }
                        builder.append("#")
                    }
                    returnList.last().seminarPresence = builder.toString()
                }
            }
            returnList
        } catch (e: Exception) {
            return returnList
        }
    }

    fun getSheets(webResponse: String): List<Sheet>? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()
        val returnList = mutableListOf<Sheet>()

        return try {
            val items = cleaner.clean(webResponse)

            val form = items.evaluateXPath("/body/div[2]/div/div/form")

            for (i in 2..Int.MAX_VALUE) {
                val table = items.evaluateXPath("/body/div[2]/div/div/form/table[$i]")
                if (table.isEmpty()) break

                val tableIndex = form.asTagNode {
                    it.allChildren.indexOf(table.first())
                } ?: 0

                val titleNode = form.asTagNode {
                    if (tableIndex > 0) it.allChildren[tableIndex - 1] else null
                }

                val title = getTitle(titleNode)


                val tr = items.evaluateXPath("/body/div[2]/div/div/form/table[$i]/thead/tr")
                val size = tr.asTagNode { it.allChildren.size } ?: 0

                val headers = StringBuilder()
                val values = StringBuilder()
                for (j in 1..size) {
                    items.evaluateXPath("/body/div[2]/div/div/form/table[$i]/thead/tr/th[$j]/*")
                        .asTagNode {
                            headers.append((it.allChildren.first() as ContentNode).content)
                        }

                    items.evaluateXPath("/body/div[2]/div/div/form/table[$i]/tbody/tr/td[$j]/*")
                        .asTagNode {
                            values.append((it.allChildren.first() as ContentNode).content)
                        }

                    items.evaluateXPath("/body/div[2]/div/div/form/table[$i]/tbody/tr/td[$j]/*/*")
                        .asTagNode {
                            values.append((it.allChildren.first() as ContentNode).content)
                        }


                    headers.append("#")
                    values.append("#")
                }

                headers.deleteCharAt(headers.length - 1)
                values.deleteCharAt(values.length - 1)

                returnList.add(Sheet(title, headers.toString(), values.toString()))
            }
            returnList
        } catch (e: Exception) {
            returnList
        }
    }

    fun getEmailPages(webResponse: String): Int {
        if (webResponse.isEmpty()) return -1

        val cleaner = HtmlCleaner()
        return try {
            val items = cleaner.clean(webResponse)

            val image = items.findElementByAttValue("sysid", "tree-8", true, true) ?: return 1
            val row = image.parent.parent


            row.childTagList.size - 4
        } catch (e: Exception) {
            -1
        }
    }

    fun getEmails(webResponse: String): List<Email>? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()
        val returnList = mutableListOf<Email>()
        return try {
            val items = cleaner.clean(webResponse)

            val tableBody =
                items.findElementByAttValue("id", "tmtab_1", true, true).childTagList.last()

            tableBody.childTagList.forEach {
                val sender = it.getElementList(
                    { it.name == "a" && it.getAttributeByName("href").contains("lide/") },
                    true
                ).firstOrNull()?.content() ?: ""

                val date = it.getElementList(
                    { it.name == "small" && it.content().matches(Regex("^([1-9]|([012][0-9])|(3[01]))\\. ([0]{0,1}[1-9]|1[012])\\. \\d\\d\\d\\d\\s([0-1]?[0-9]|2?[0-3]):([0-5]\\d)\$")) },
                    true
                ).firstOrNull()?.content()
                    ?: ""

                val subjectTag = it.getElementList(
                    { it.name == "a" && it.getAttributeByName("href").contains("email.pl") },
                    true
                ).first()

                val subject = subjectTag.getAttributeByName("title")
                val eid =
                    subjectTag.getAttributeByName("href").substringAfter("eid").substringBefore(";")
                        .substringAfter("=")
                val fid =
                    subjectTag.getAttributeByName("href").substringAfter("fid").substringBefore(";")
                        .substringAfter("=")
                val opened = subjectTag.findElementByName("b", false) == null

                returnList.add(
                    Email(
                        eid,
                        fid,
                        sender.ifEmpty { "Akademický informačný systém" },
                        subject,
                        date,
                        opened
                    )
                )
            }
            returnList
        } catch (e: Exception) {
            returnList
        }
    }

    fun getEmailDetail(webResponse: String): String {
        if (webResponse.isEmpty()) return ""

        val cleaner = HtmlCleaner()
        return try {
            val items = cleaner.clean(webResponse)
            val form = items.findElementByAttValue("name", "wqqwqqwwqyw0", true, true)
            val table = form.findElementByName("tbody", true).findElementByName("tbody", true)
            val message = table.childTagList.last().findElementByName("small", true)
                .allChildren.map { if (it is ContentNode) it.content else "\n" }.joinToString("")
                .replace("&nbsp;", " ")
            message
        } catch (e: Exception) {
            ""
        }
    }

    fun getDocuments(webResponse: String): List<Document>? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()
        val returnList = mutableListOf<Document>()
        return try {
            val items = cleaner.clean(webResponse)

            val topTable = items.findElementByAttValue("name", "wqqwqqwwqyw0", true, true)
            topTable.childTagList.forEach {
                if (it.childTagList.size == 1) return@forEach

                val name = it.childTagList.component2().content()
                val link: String = it.getElementList(
                    { it.name == "a" && it.getAttributeByName("href").contains("slozka.pl") },
                    true
                ).firstOrNull()?.getAttributeByName("href") ?: ""
                val id = link.substringAfter("id=").substringBefore(";")
                val parentId = link.substringAfter("dok=").substringBefore(";")
                returnList.add(Document(name, id, parentId, true))
            }
            val bottomTable = items.findElementByAttValue("name", "wqqwqqwwqyw1", true, true)

            bottomTable.childTagList.forEach {
                if (it.childTagList.size == 1) return@forEach

                val name = it.childTagList.component2().content()
                val link: String = it.getElementList(
                    { it.name == "a" && it.getAttributeByName("href").contains("slozka.pl") },
                    true
                ).firstOrNull()?.getAttributeByName("href") ?: ""
                val id = link.substringAfter("id=").substringBefore(";")
                val parentId = link.substringAfter("dok=").substringBefore(";")
                returnList.add(Document(name, id, parentId, false))
            }

            returnList
        } catch (e: Exception) {
            returnList
        }

    }

    fun getSuggestions(webResponse: String): List<Suggestion>? {
        if (webResponse.isEmpty()) return emptyList()

        return Gson().fromJson<SuggestionResult>(webResponse, SuggestionResult::class.java)
            .toSuggestions()
    }

    private fun <T> Array<Any>.asTagNode(function: (TagNode) -> T): T? {
        if (isNotEmpty()) {
            if (first() is TagNode) {
                return try {
                    function(first() as TagNode)
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    private fun <T> Any.asTagNode(function: (TagNode) -> T): T? {
        if (this is TagNode) {
            return try {
                function(this)
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    private fun getTitle(node: Any?): String {
        if (node is ContentNode) return node.content
        return node?.asTagNode {
            getTitle(it.allChildren.first())
        } ?: ""
    }
}
