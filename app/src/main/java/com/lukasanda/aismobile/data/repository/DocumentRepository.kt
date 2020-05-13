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

package com.lukasanda.aismobile.data.repository

import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.DocumentDao
import com.lukasanda.aismobile.data.db.entity.Document
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.util.authenticatedOrThrow2
import com.lukasanda.aismobile.util.repeatIfException
import com.lukasanda.dataprovider.Parser
import timber.log.Timber

class DocumentRepository(private val prefs: Prefs, private val documentDao: DocumentDao, private val aisApi: AISApi) {

    fun getDocuments(folder: String) = documentDao.getDocuments(folder)

    suspend fun deleteAll() = documentDao.deleteAllDocuments()

    suspend fun fetchDocument(folder: String) {

        runCatching {
            repeatIfException(3, 2000) { aisApi.getDocumentPages("1;id=$folder").authenticatedOrThrow2() }
                ?.let { Parser.getMaxPages(it) }
                ?.let {
                    val documents = (0..it.first).map { "0;id=$folder;on=$it" }
                    val folders = it.second?.let { (0..it).map { "1;id=$folder;on=$it" } } ?: emptyList()

                    documents + folders
                }
                ?.also { Timber.d("Number of pages ${it.size}") }
                ?.map { repeatIfException(3, 2000) { aisApi.getDocumentItems(it).authenticatedOrThrow2() } }
                ?.filterNotNull()
                ?.map { Parser.getDocuments(it, folder) }
                ?.filterNotNull()
                ?.flatten()
                ?.toSet()
                ?.map { Document(it.id, it.name, it.mimeType, it.parentFolderId, it.openable) }
                ?.also { documentDao.updateFolder(folder, it) }
        }

//
//        val response = aisApi.getDocumentPages("1;id=$folder").authenticatedOrThrow2()
//        val documents = Parser.getDocuments(response, folder)?.map { Document(it.id, it.name, it.mimeType, it.parentFolderId, it.openable) } ?: emptyList()
//        documentDao.updateFolder(folder, documents)
    }
}