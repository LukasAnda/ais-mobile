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

package com.lukasanda.aismobile.core

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.lukasanda.aismobile.util.px
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


abstract class SwipeHelper : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private var recyclerView: RecyclerView? = null
    private var buttons: MutableList<UnderlayButton>? = null
    private lateinit var gestureDetector: GestureDetector
    private var swipedPos = -1
    private var swipeThreshold = 0.5f
    private val buttonsBuffer: MutableMap<Int, MutableList<UnderlayButton>?>
    private lateinit var recoverQueue: Queue<Int>
    private val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            buttons?.forEach {
                if (it.onClick(e.x, e.y)) return@forEach
            }
            recoverQueue.add(swipedPos)
            swipedPos = -1
            recoverSwipedItem()
            return false
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { _, e ->
        if (swipedPos < 0) return@OnTouchListener false
        val point = Point(e.rawX.toInt(), e.rawY.toInt())
        val swipedViewHolder: RecyclerView.ViewHolder = recyclerView?.findViewHolderForAdapterPosition(swipedPos) ?: return@OnTouchListener false
        val swipedItem: View = swipedViewHolder.itemView
        val rect = Rect()
        swipedItem.getGlobalVisibleRect(rect)
        if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
            if (rect.top < point.y && rect.bottom > point.y) gestureDetector.onTouchEvent(e) else {
                recoverQueue.add(swipedPos)
                swipedPos = -1
                recoverSwipedItem()
            }
        }
        false
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos: Int = viewHolder.adapterPosition
        if (swipedPos != pos) recoverQueue.add(swipedPos)
        swipedPos = pos
        if (buttonsBuffer.containsKey(swipedPos)) buttons = buttonsBuffer[swipedPos] else buttons!!.clear()
        buttonsBuffer.clear()
        swipeThreshold = 0.5f * buttons!!.size * getButtonWidth(viewHolder)
        recoverSwipedItem()
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 10.0f * defaultValue
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val pos: Int = viewHolder.adapterPosition
        var translationX = dX
        val itemView: View = viewHolder.itemView
        if (pos < 0) {
            swipedPos = pos
            return
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                var buffer: MutableList<UnderlayButton>? = ArrayList()
                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateUnderlayButton(viewHolder, buffer)
                    buttonsBuffer[pos] = buffer
                } else {
                    buffer = buttonsBuffer[pos]
                }
                translationX = dX * buffer!!.size * getButtonWidth(viewHolder) / itemView.width
                drawButtons(c, itemView, buffer, pos, translationX)
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            val pos: Int = recoverQueue.poll() ?: return
            if (pos > -1) {
                recyclerView?.adapter?.notifyItemChanged(pos)
            }
        }
    }

    private fun drawButtons(c: Canvas, itemView: View, buffer: List<UnderlayButton>?, pos: Int, dX: Float) {
        var right: Float = itemView.right.toFloat() + 10
        val dButtonWidth = -1 * dX / buffer!!.size
        for (button in buffer) {
            val left = right - dButtonWidth
            button.onDraw(
                c,
                RectF(
                    left + 10,
                    itemView.top.toFloat(),
                    right - 10,
                    itemView.bottom.toFloat()
                ),
                pos
            )
            right = left
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        gestureDetector = GestureDetector(recyclerView.context, gestureListener)
        this.recyclerView?.setOnTouchListener(onTouchListener)
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(this.recyclerView)
    }

    abstract fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder?, underlayButtons: MutableList<UnderlayButton>?)
    interface UnderlayButtonClickListener {
        fun onClick(pos: Int)
    }

    fun getButtonWidth(viewHolder: RecyclerView.ViewHolder) = viewHolder.itemView.height + 20

    class UnderlayButton(private val text: String, private val drawable: Drawable?, private val color: Int, private val clickListener: UnderlayButtonClickListener) {
        private var pos = 0
        private var clickRegion: RectF? = null
        fun onClick(x: Float, y: Float): Boolean {
            if (clickRegion != null && clickRegion!!.contains(x, y)) {
                clickListener.onClick(pos)
                return true
            }
            return false
        }

        @SuppressLint("RestrictedApi")
        fun onDraw(c: Canvas, rect: RectF, pos: Int) {
            val p = Paint()
            // Draw background
            p.color = color
            c.drawRoundRect(rect, 8f.px, 8f.px, p)
            // Draw Text
            p.color = Color.WHITE

            if (drawable == null) {
                //p.setTextSize(LayoutHelper.getPx(MyApplication.getAppContext(), 12));
                p.textSize = Resources.getSystem().displayMetrics.density * 12
                val r = Rect()
                val cHeight = rect.height()
                val cWidth = rect.width()
                p.textAlign = Paint.Align.LEFT
                p.getTextBounds(text, 0, text.length, r)
                val x: Float = cWidth / 2f - r.width() / 2f - r.left
                val y: Float = cHeight / 2f + r.height() / 2f - r.bottom
                c.drawText(text, rect.left + x, rect.top + y, p)
            } else {
                val width = rect.width().toInt() / 2 - 12.px
                val height = rect.height().toInt() / 2 - 12.px
                val r = Rect(rect.left.toInt() + width, rect.top.toInt() + height, rect.right.toInt() - width, rect.bottom.toInt() - height)
                p.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                val bitmap = drawable.toBitmap(60, 60)
                c.drawBitmap(bitmap, null, r, p)
            }
            clickRegion = rect
            this.pos = pos
        }


    }

    init {
        buttons = ArrayList()
        buttonsBuffer = HashMap()
        recoverQueue = object : LinkedList<Int>() {
            override fun add(element: Int): Boolean {
                return if (contains(element)) false else super.add(element)
            }
        }
    }
}