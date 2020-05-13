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

package com.lukasanda.aismobile.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.ui.recyclerview.replaceWith

class PresenceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val presence = mutableListOf<PresenceType>()
    private val presencePaints = mutableListOf(
        createCirclePaint(ContextCompat.getColor(context, R.color.color_presence)),
        createCirclePaint(ContextCompat.getColor(context, R.color.color_absence)),
        createCirclePaint(ContextCompat.getColor(context, R.color.color_unknown)),
        createCirclePaint(ContextCompat.getColor(context, R.color.color_excused)),
        createCirclePaint(ContextCompat.getColor(context, R.color.color_late)),
        createCirclePaint(ContextCompat.getColor(context, R.color.color_disqualified)),
        createCirclePaint(ContextCompat.getColor(context, R.color.color_different)),
        createCirclePaint(ContextCompat.getColor(context, R.color.color_early))
    )

    fun setPresence(items: List<PresenceType>) = presence.replaceWith(items).apply {
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        Log.d("TAG", "Calling onDraw in presenceView, presenceSize: ${presence.size}")

        val totalDots = presence.size
        val totalParts = totalDots + 1

        val move = width.toFloat() / totalParts

        presence.forEachIndexed { index, item ->
            val multiplier = index + 1

            canvas?.drawCircle(
                move * multiplier,
                height.toFloat() / 2,
                10f,
                presencePaints[item.ordinal]
            )
        }
    }
}

fun createCirclePaint(color: Int) = Paint().apply {
    this.color = color
    style = Paint.Style.FILL
    isAntiAlias = true
}

fun String.toPresenceType() = when (this) {
    "doch-pritomen" -> PresenceType.Presence
    "doch-neomluven" -> PresenceType.Absence
    "doch-omluven" -> PresenceType.Excused
    "doch-pozde" -> PresenceType.Late
    "doch-vyloucen" -> PresenceType.Disqualified
    "bullet-j" -> PresenceType.Different
    "bullet-d" -> PresenceType.Early
    else -> PresenceType.Unknown
}

enum class PresenceType {
    Presence, Absence, Unknown, Excused, Late, Disqualified, Different, Early
}