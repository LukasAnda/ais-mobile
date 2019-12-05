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

package com.lukasanda.aismobile.extensions

//import io.reactivex.Completable
//import io.reactivex.Flowable
//import io.reactivex.Single
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.schedulers.Schedulers
//
//fun <T> Single<T>.with(): Single<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//
//fun <T> Flowable<T>.with(): Flowable<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//
//fun Completable.with(): Completable = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//
//inline fun <reified T> List<Single<T>>.singleZip(): Single<List<T>> =
//    when (isEmpty()) {
//        true -> Single.just(emptyList())
//        else -> Single.zip(this) { it.toList() }
//            .map {
//                @Suppress("UNCHECKED_CAST")
//                it as List<T>
//            }
//    }