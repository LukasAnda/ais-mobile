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

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.db.entity.Document
import com.lukasanda.aismobile.databinding.DocumentsFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import com.lukasanda.aismobile.ui.main.BaseFragmentHandler
import com.lukasanda.aismobile.ui.recyclerview.bindLinear
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DocumentsFragment : BaseFragment<DocumentsFragment.Views, DocumentsFragmentBinding, DocumentsViewModel, DocumentsHandler>() {
    private val adapter = DocumentsAdapter {
        it?.let {
            if (it.openable) {
                handler.openDocument(it)
            } else {
                handler.openFolder(it)
            }
        } ?: run {
            handler.navigateToParent()
        }
    }

    inner class Views : BaseViews {
        override fun modifyViews() {
            val args by navArgs<DocumentsFragmentArgs>()
            val folder = args.folder ?: ""
            handler.setTitle(getString(R.string.documents))

            viewModel.setFolder(folder)
            viewModel.fetchDocuments()

            binding?.documentsRecycler?.bindLinear(adapter)
            viewModel.getDocuments().observe(viewLifecycleOwner, Observer {
                if (it.isEmpty()) {
                    binding?.progress?.show()
                } else {
                    binding?.progress?.hide()
                }
                if (folder == "") {
                    adapter.swapData(it.filterNot { it.id.isEmpty() }.map { Either.Right(it) })
                } else {
                    val list: MutableList<Either<Document, Document>> = it.filterNot { it.id.isEmpty() }.map { Either.Right(it) }.toMutableList()
                    list.add(0, Either.Left(Document("", "", "", "", false)))
                    adapter.swapData(list)
                }
            })
        }

    }

    override val viewModel: DocumentsViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): DocumentsFragmentBinding = DocumentsFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: DocumentsHandler
}

interface DocumentsHandler : BaseFragmentHandler {
    fun openFolder(document: Document)
    fun openDocument(document: Document)
    fun navigateToParent()
}