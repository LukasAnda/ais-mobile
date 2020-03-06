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
import com.lukasanda.aismobile.util.authenticatedOrThrow
import sk.lukasanda.dataprovider.Parser

class DocumentRepository(private val prefs: Prefs, private val documentDao: DocumentDao, private val aisApi: AISApi) {

    fun getDocuments(folder: String) = documentDao.getDocuments(folder)

    suspend fun fetchDocument(folder: String) {
        val response = aisApi.getDocumentsInFolder("1;id=$folder").authenticatedOrThrow()
        val documents = Parser.getDocuments(response, folder)?.map { Document(it.id, it.name, it.mimeType, it.parentFolderId, it.openable) } ?: emptyList()
        documentDao.updateFolder(folder, documents)
    }
}