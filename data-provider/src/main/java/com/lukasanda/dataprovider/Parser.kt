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

package com.lukasanda.dataprovider

import com.google.gson.Gson
import com.lukasanda.dataprovider.data.*
import org.htmlcleaner.ContentNode
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import kotlin.math.max

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
            return items.findElementByAttValue("name", "formular", true, true).let {
                it.getElementListByName("table", true).component2()
            }.let { it.childTagList.first().childTagList.firstOrNull() }
                ?.getAllElementsList(true)
                ?.lastOrNull() { it.content().isNotEmpty() }
                ?.let { it.content().toInt() }
        } catch (e: Exception) {
            null
        }
    }

    fun getWifiInfo(webResponse: String): WifiInfo? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()

        return try {
            val items = cleaner.clean(webResponse)

            val table = items.findElementByAttValue("name", "wqqwqqwwqyw0", true, true).findElementByName("tbody", true)


            val node = table.childTagList.component1().childTagList.last()
            val name = node.getAllElementsList(true).map { it.content() }.filter { it.isNotEmpty() }.firstOrNull() ?: ""

            val node2 = table.childTagList.component2().childTagList.last()
            val password = node2.getAllElementsList(true).map { it.content() }.filter { it.isNotEmpty() }.firstOrNull() ?: ""


            WifiInfo(name, password)
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
            }.sortedBy { it.id }

            val studiesList = if (hasStudiesSelector) studies.childTagList.map {
                Study(
                    it.getAttributeByName("value"),
                    it.content().getSemesterCount()
                )
            }.sortedBy { it.id } else return semesterList

            var studyIndex = 0
            var nextIndex = studiesList[studyIndex].semesterCount

            val newList = semesterList.mapIndexed { index, semester ->
                if (index + 1 > nextIndex) {
                    studyIndex++
                    nextIndex += studiesList[studyIndex].semesterCount
                }

                semester.copy(studiesId = studiesList[studyIndex].id)
            }

            newList
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
                            "",
                            builder.toString(),
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
                    returnList.last().coursePresence = returnList.last().seminarPresence
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

            val form = items.findElementByName("form", true)

            val tables = form.getElementsByName("table", true)
                .filter { !it.hasAttribute("id") || !it.getAttributeByName("id").contains("table") }
                .filter { it.childTagList.size == 2 }

            tables.forEach { table ->
                val tableIndex = max(0, form.childTagList.indexOf(table))

                var title = ""
                var index = 1
                while (title.isEmpty() && tableIndex - index > 0) {
                    title = form.childTagList[tableIndex - index].getTitle()
                    index++
                }

                val columnNames =
                    table.findElementByName("thead", true).findElementByName("tr", true)
                        .childTagList.map { it.getString() }.joinToString("#")


                val columnValues =
                    table.findElementByName("tbody", true).findElementByName("tr", true)
                        .childTagList.map { it.getString() }.joinToString("#")

                //Mame tu komentar
                val comment = if (table.findElementByName("tbody", true).childTagList.size > 1) {
                    table.findElementByName("tbody", true).getElementListByName("tr", true).last()
                        .childTagList.last().parseMessage()
                } else {
                    ""
                }

                returnList.add(Sheet(title, comment, columnNames, columnValues))

            }


            returnList
        } catch (e: Exception) {
            e.printStackTrace()
            returnList
        }
    }

    fun getTeachers(webResponse: String): List<Teacher>? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()
        val returnList = mutableListOf<Teacher>()

        return try {
            val items = cleaner.clean(webResponse)
            val teachers = items.getElementListHavingAttribute("href", true)
                .filter { it.getAttributeByName("href").contains("clovek.pl") }
                .filter { it.parent.name == "div" || it.parent.name == "span" }.map {
                    val name = it.content()
                    val id = it.getAttributeByName("href").substringAfter("id=").substringBefore(";")
                    Teacher(name, id)
                }

            teachers
        } catch (e: Exception) {
            returnList
        }
    }

    fun TagNode.parseMessage(): String {
        return if (this.allChildren.filter { it is ContentNode }.isNotEmpty()) {
            this.allChildren.map {
                if (it is ContentNode) it.content else if (it is TagNode && it.hasAttribute(
                        "href"
                    )
                ) it.content() else "\n"
            }.joinToString("")
                .replace("&nbsp;", " ")
        } else {
            childTagList.firstOrNull()?.parseMessage() ?: ""
        }
    }

    fun getNewMessageToken(webResponse: String): String {
        if (webResponse.isEmpty()) return ""
        val cleaner = HtmlCleaner()
        return try {
            val items = cleaner.clean(webResponse)

            val tokenElement = items.findElementByAttValue("name", "serializace", true, true)
            val token = tokenElement.getAttributeByName("value")
            token
        } catch (e: Exception) {
            ""
        }
    }

    fun getEmailInfo(webResponse: String): EmailInfo {
        if (webResponse.isEmpty()) return EmailInfo(-1, "", 0)

        val cleaner = HtmlCleaner()
        return try {
            val items = cleaner.clean(webResponse)

            val sentDirectoryImage =
                items.findElementByAttValue("sysid", "post-sent-small", true, true)
            val sentDirectoryLink =
                sentDirectoryImage.parent.findElementByName("a", true).getAttributeByName("href")
                    .substringAfter("fid=")

            val emailCountElement = items.findElementByAttValue("href", "/auth/posta/", true, true)
            val emailCount = emailCountElement.content().toInt()

            val link = items.getElementListByAttValue("sysid", "tree-vpravo-zarazka", true, true)?.map { it.parent.getAttributeByName("href") }?.firstOrNull() ?: ""


            EmailInfo(link.substringAfter("on=").toIntOrNull() ?: 0, sentDirectoryLink, emailCount)
        } catch (e: Exception) {
            EmailInfo(-1, "", 0)
        }
    }

    fun getEmails(webResponse: String): List<Email>? {
        if (webResponse.isEmpty()) return null

//        val doc = Jsoup.parse(webResponse)
//
//        val table = doc.select("#tmtab_1")

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

                val senderId = it.getElementList(
                    { it.name == "a" && it.getAttributeByName("href").contains("lide/") },
                    true
                ).firstOrNull()?.getAttributeByName("href")?.substringAfterLast("id=") ?: ""

                val date = it.getElementList(
                    { it.content().matches(Regex("^([1-9]|([012][0-9])|(3[01]))\\. ([0]{0,1}[1-9]|1[012])\\. \\d\\d\\d\\d\\s([0-1]?[0-9]|2?[0-3]):([0-5]\\d)\$")) },
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
                        senderId,
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

    fun getEmailDetail(webResponse: String): EmailDetail {
        if (webResponse.isEmpty()) return EmailDetail()

        val cleaner = HtmlCleaner()
        return try {
            val items = cleaner.clean(webResponse)
            val form = items.findElementByAttValue("name", "wqqwqqwwqyw0", true, true)
            val table = form.findElementByName("tbody", true).findElementByName("tbody", true)
            val message = table.childTagList.last().getAllElementsList(true).find { it.name == "span" || it.name == "div" }?.allChildren
                ?.map {
                    if (it is ContentNode) it.content else if (it is TagNode && it.hasAttribute(
                            "href"
                        )
                    ) it.content() else "\n"
                }?.joinToString("")
                ?.replace("&nbsp;", " ") ?: ""

            //Second table
            val documentsTable = form.getElementListByName("table", false)[1].findElementByName("tbody", true)
            val documentsList = mutableListOf<Pair<String, String>>()

            if (documentsTable.childTagList.size > 1) {
                documentsTable.childTagList.minus(documentsTable.childTagList.first()).forEach {
                    val linkTag = it.findElementByName("a", true)

                    val link = linkTag.getAttributeByName("href").substringAfter("email.pl?")
                    val filename = linkTag.content().substringBefore("[").trim()

                    documentsList.add(Pair(link, filename))
                }
            }

            EmailDetail(message, documentsList)
        } catch (e: Exception) {
            EmailDetail()
        }
    }

    fun getMaxPages(webResponse: String): Pair<Int, Int?>? {
        if (webResponse.isEmpty()) return null
        val cleaner = HtmlCleaner()
        return try {
            val items = cleaner.clean(webResponse)
            val arrows = items.getElementListByAttValue("sysid", "tree-vpravo-zarazka", true, true) ?: return Pair(0, null)

            val links = arrows.map { it.parent.getAttributeByName("href") }.toSet()



            Pair(links.find { it.contains("zobraz=0") }?.substringAfter("on=")?.toInt() ?: 0, links.find { it.contains("zobraz=1") }?.substringAfter("on=")?.toInt())
        } catch (e: Exception) {
            Pair(0, null)
        }
    }

    fun getDocuments(webResponse: String, parentFolder: String = ""): List<Document>? {
        if (webResponse.isEmpty()) return null

        val cleaner = HtmlCleaner()
        val returnList = mutableListOf<Document>()
        return try {
            val items = cleaner.clean(webResponse)

            val topTableHead = items.findElementByAttValue("name", "wqqwqqwwqyw0", true, true).getElementListByName("table", false).find { it.childTagList.size == 2 }
            val topTable = topTableHead?.findElementByName("tbody", false)
            topTable?.childTagList?.forEach {
                if (it.childTagList.size == 1) return@forEach

                val name = it.childTagList.component2().getAllElementsList(true).map { it.content() }.filter { it.isNotEmpty() }.firstOrNull() ?: ""
                val link: String = it.getElementList({ it.name == "a" && it.getAttributeByName("href").contains("slozka.pl") }, true).firstOrNull()?.getAttributeByName("href") ?: ""
                val id = link.substringAfter("download=").substringBefore(";")
                val parentId = link.substringAfter("id=").substringBefore(";")

                val mimeType = it.getElementList({ it.name == "img" && it.getAttributeByName("sysid").contains("mime") }, true).firstOrNull()?.getAttributeByName("sysid") ?: ""
                returnList.add(Document(name, mimeType, id, parentId, true))
            }

            val bottomTableHead = items.findElementByAttValue("name", "wqqwqqwwqyw1", true, true).getElementListByName("table", false).find { it.childTagList.size == 2 }
            val bottomTable = bottomTableHead?.findElementByName("tbody", false)
            bottomTable?.childTagList?.forEach {
                if (it.childTagList.size == 1) return@forEach

                val name = it.childTagList.component2().getAllElementsList(true).map { it.content() }.filter { it.isNotEmpty() }.firstOrNull() ?: ""
                val link: String = it.getElementList(
                    { it.name == "a" && it.getAttributeByName("href").contains("slozka.pl") },
                    true
                ).firstOrNull()?.getAttributeByName("href") ?: ""
                val id = link.substringAfter("id=").substringBefore(";")
                val parentId = link.substringAfter("dok=").substringBefore(";")

                println("Name: $name, Link: $link, id: $id, parentid: $parentId")
                returnList.add(Document(name, "", id, parentFolder, false))
            }

            returnList
        } catch (e: Exception) {
            returnList
        }

    }

    fun getProfileEmails(webResponse: String): List<String> {
        if (webResponse.isEmpty()) return emptyList()

        val cleaner = HtmlCleaner()
        val returnList = mutableListOf<String>()

        return try {
            val items = cleaner.clean(webResponse)

            val emails = items.getAllElements(true)
                .map { it.content() }
                .filter { it.contains("[at]") }
                .map { it.replace("[at]", "@").replace(" ", "") }
            emails
        } catch (e: Exception) {
            returnList
        }
    }

    fun getSuggestions(webResponse: String): List<Suggestion>? {
        if (webResponse.isEmpty()) return emptyList()

        return Gson().fromJson<SuggestionResult>(webResponse, SuggestionResult::class.java)
            .toSuggestions()
    }

    private fun TagNode.getString(): String {
        return (allChildren.firstOrNull() as? ContentNode)?.content ?: kotlin.run {
            childTagList.firstOrNull()?.getString() ?: ""
        }
    }

    private fun TagNode.getTitle(): String {
        if (this.name == "b") {
            return content()
        }
        getAllElementsList(true).forEach {
            if (it.name == "b") {
                return it.content()
            }
        }
        return ""
    }
}
