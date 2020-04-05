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

package com.lukasanda.aismobile.ui.main.peopleDetail

import android.view.ViewGroup
import com.lukasanda.aismobile.data.db.entity.ProfileInfoItem
import com.lukasanda.aismobile.databinding.PeopleDetailItemBinding
import com.lukasanda.aismobile.ui.recyclerview.BaseAdapter
import com.lukasanda.aismobile.ui.recyclerview.BindingViewHolder
import com.lukasanda.aismobile.ui.recyclerview.create

class PeopleDetailAdapter(onClick: (ProfileInfoItem) -> Unit) : BaseAdapter<ProfileInfoItem, ProfileInfoItem, PeopleInfoItemHolder>(onClick) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = parent.create(::PeopleInfoItemHolder, PeopleDetailItemBinding::inflate)
}

class PeopleInfoItemHolder(binding: PeopleDetailItemBinding) : BindingViewHolder<ProfileInfoItem, PeopleDetailItemBinding>(binding) {
    override fun bind(item: ProfileInfoItem, onClick: (ProfileInfoItem) -> Unit) {
        binding.hint.setText(item.info.first)
        binding.field.text = item.info.second

        binding.root.setOnClickListener {
            onClick(item)
        }
    }
}