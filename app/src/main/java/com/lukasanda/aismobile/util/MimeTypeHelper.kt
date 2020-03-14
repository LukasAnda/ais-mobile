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

package com.lukasanda.aismobile.util

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import com.lukasanda.aismobile.R

fun getMimeType(path: String): String {
    var type = "image/jpeg" // Default Value
    val extension = MimeTypeMap.getFileExtensionFromUrl(path)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: type
    }
    return type
}

fun Context.getMimeColor(type: String) = when {
    type.contains("doc") || type.contains("xls") || type.contains("audio") || type.contains("video") || type.contains("text") -> ContextCompat.getColor(this, R.color.color_documents)
    type.contains("pdf") -> ContextCompat.getColor(this, R.color.color_pdf)
    type.contains("zip") -> ContextCompat.getColor(this, R.color.color_archives)
    type.isEmpty() -> ContextCompat.getColor(this, R.color.color_folders)
    else -> ContextCompat.getColor(this, R.color.color_unknown_file)
}