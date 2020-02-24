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

package sk.lukasanda.base.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BindingViewHolder<I, T : ViewBinding>(binding: T) :
    BaseBindingViewHolder<I, I, T>(binding)

abstract class BaseBindingViewHolder<ITEM, LISTENER_ITEM, BINDING : ViewBinding>(val binding: BINDING) :
    RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(item: ITEM, onClick: ((LISTENER_ITEM) -> Unit)? = null)
}

abstract class BaseAdapter<ITEM : Any, LISTENER_ITEM : Any, VIEWHOLDER : BaseBindingViewHolder<ITEM, LISTENER_ITEM, *>>(
    val onClick: ((LISTENER_ITEM) -> Unit)? = null
) : RecyclerView.Adapter<VIEWHOLDER>() {
    protected val items = mutableListOf<ITEM>()

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VIEWHOLDER, position: Int) =
        holder.bind(items[position], onClick)

    fun swapData(newItems: List<ITEM>) = items.replaceWith(newItems).also { notifyDataSetChanged() }
}

inline fun <ITEM, LISTENER_ITEM, reified VIEW_BINDING : ViewBinding, reified T : BaseBindingViewHolder<ITEM, LISTENER_ITEM, VIEW_BINDING>> ViewGroup.create(
    createHolder: (VIEW_BINDING) -> T
): T {
    val inflater = LayoutInflater.from(context)
    val inflateMethod = VIEW_BINDING::class.java.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )
    val binding = inflateMethod.invoke(null, inflater, this, false) as VIEW_BINDING
    return createHolder(binding)
}

inline fun <ITEM, LISTENER_ITEM, reified VIEW_BINDING : ViewBinding, reified T : BaseBindingViewHolder<ITEM, LISTENER_ITEM, VIEW_BINDING>> ViewGroup.create(
    createHolder: (VIEW_BINDING) -> T,
    inflateFun: (LayoutInflater, ViewGroup, Boolean) -> VIEW_BINDING
): T {
    val inflater = LayoutInflater.from(context)
    return createHolder(inflateFun(inflater, this, false))
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.bindLinear(
    adapter: RecyclerView.Adapter<VH>,
    orientation: Int = RecyclerView.VERTICAL
) {
    apply {
        this.adapter = adapter
        this.layoutManager = LinearLayoutManager(this.context, orientation, false)
    }
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.bindGrid(
    adapter: RecyclerView.Adapter<VH>,
    columns: Int
) {
    apply {
        this.adapter = adapter
        this.layoutManager = GridLayoutManager(this.context, columns)
    }
}

fun <T> MutableList<T>.replaceWith(newItems: List<T>) {
    this.clear()
    this.addAll(newItems)
}