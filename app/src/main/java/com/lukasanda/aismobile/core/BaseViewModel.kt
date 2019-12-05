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

package com.lukasanda.aismobile.core

import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
//    private val disposables: CompositeDisposable = CompositeDisposable()

//    fun addToDisposable(disposable: Disposable) {
//        disposables.add(disposable)
//    }
//
//    fun launchRx(func: ()-> Disposable) = disposables.add(func())

    override fun onCleared() {
//        disposables.clear()
        super.onCleared()
    }
}