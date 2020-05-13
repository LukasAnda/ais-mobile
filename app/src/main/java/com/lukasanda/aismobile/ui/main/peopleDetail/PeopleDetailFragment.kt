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

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.firebase.analytics.FirebaseAnalytics
import com.lukasanda.aismobile.core.AnalyticsTrait
import com.lukasanda.aismobile.core.SCREEN_PPL_DETAIL
import com.lukasanda.aismobile.data.cache.SafePrefs
import com.lukasanda.aismobile.data.db.entity.Suggestion
import com.lukasanda.aismobile.databinding.PeopleDetailFragmentBinding
import com.lukasanda.aismobile.ui.activity.BaseViews
import com.lukasanda.aismobile.ui.fragment.BaseFragment
import com.lukasanda.aismobile.ui.main.BaseFragmentHandler
import com.lukasanda.aismobile.ui.recyclerview.bindLinear
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PeopleDetailFragment : BaseFragment<PeopleDetailFragment.Views, PeopleDetailFragmentBinding, PeopleDetailViewModel, PeopleDetailHandler>(), AnalyticsTrait {

    private val safePrefs by inject<SafePrefs>()

    inner class Views : BaseViews {
        override fun modifyViews() {
            val args by navArgs<PeopleDetailFragmentArgs>()

            logEvent(SCREEN_PPL_DETAIL)

            val infoAdapter = PeopleDetailAdapter {
                if (it.info.second.contains("@")) {
                    if (it.info.second.contains("stuba.sk")) {
                        handler.sendMail(args.suggestion)
                    } else {
                        handler.sendMail(it.info.second)
                    }
                }
            }

            handler.setTitle("")

            binding?.items?.bindLinear(infoAdapter)

            val id = args.suggestion.id

            val builder = LazyHeaders.Builder().addHeader("Cookie", safePrefs.sessionCookie)

            val url = GlideUrl("https://is.stuba.sk/auth/lide/foto.pl?id=${id}", builder.build())

            binding?.picture?.let { Glide.with(this@PeopleDetailFragment).load(url).into(it) }

            viewModel.profileInfo().observe(viewLifecycleOwner, Observer {
                infoAdapter.swapData(it.info)
            })

            viewModel.getProfileInfo(args.suggestion)
        }
    }

    override val viewModel: PeopleDetailViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding() = PeopleDetailFragmentBinding.inflate(layoutInflater)

    override fun createViews() = Views()

    override lateinit var handler: PeopleDetailHandler
    override fun getAnalytics() = FirebaseAnalytics.getInstance(requireContext())
}

interface PeopleDetailHandler : BaseFragmentHandler {
    fun sendMail(suggestion: Suggestion)
    fun sendMail(address: String)
}