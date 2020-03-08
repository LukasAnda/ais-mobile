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

package com.lukasanda.aismobile.ui.main.documents

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasanda.aismobile.data.repository.DocumentRepository
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

private const val FOLDER = "FOLDER"

class DocumentsViewModel(private val documentRepository: DocumentRepository, private val handle: SavedStateHandle) : BaseViewModel(handle) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        run {
            t.printStackTrace()
        }
    }

    fun setFolder(folder: String) {
        handle[FOLDER] = folder
    }

    fun getFolder(): String = handle.get(FOLDER) as? String ?: ""

    fun getDocuments() = documentRepository.getDocuments(getFolder())

    fun fetchDocuments() = viewModelScope.launch(coroutineExceptionHandler) {
        if (getFolder().isNotEmpty()) {
            documentRepository.fetchDocument(getFolder())
        }
    }
}