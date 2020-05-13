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

package com.lukasanda.aismobile.ui.trait

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

interface LifecycleTrait : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun create() {
        Log.d("TAG", "Create called")
        this.onCreated()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun start() {
        Log.d("TAG", "START called")
        this.onStarted()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun resume() {
        Log.d("TAG", "RESUME called")
        this.onResumed()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun pause() {
        Log.d("TAG", "PAUSE called")
        this.onPaused()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stop() {
        Log.d("TAG", "STOP called")
        this.onStopped()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun destroy() {
        Log.d("TAG", "DESTROY called")
        this.onDestroyed()
    }

    fun onCreated() {}
    fun onStarted() {}
    fun onResumed() {}
    fun onPaused() {}
    fun onStopped() {}
    fun onDestroyed() {}
}