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

package com.lukasanda.aismobile.ui.login

//import com.lukasanda.aismobile.data.remote.SyncWorker
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.core.*
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.cache.SafePrefs
import com.lukasanda.aismobile.databinding.ActivityLoginBinding
import com.lukasanda.aismobile.ui.activity.BaseActivityViews
import com.lukasanda.aismobile.ui.activity.BaseUIActivity
import com.lukasanda.aismobile.util.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class LoginActivity : BaseUIActivity<LoginViewModel, LoginActivity.Views, ActivityLoginBinding>(), AnalyticsTrait {
    private val safePrefs by inject<SafePrefs>()
    private val prefs by inject<Prefs>()
    private var hasPressedBack = false

    inner class Views : BaseActivityViews {
        override fun setNavigationGraph(): Int? = null

        override fun modifyViews() {
            binding.emailEditText.setText(safePrefs.email)
            binding.passwordEditText.setText(safePrefs.password)

            if (safePrefs.email.isNotEmpty() && safePrefs.password.isNotEmpty()) {
                viewModel.login(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                )
            }

            binding.loginButton.setOnClickListener {
                viewModel.login(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                )
            }

            viewModel.state.observe(this@LoginActivity, Observer {
                when (it) {
                    is State.Loading -> {
                        showProgress()
                    }
                    is State.Success -> {
                        logEvent(ACTION_LOGIN)
                        hideProgress()
                        startSingleWorker(applicationContext)
                        finish()
                    }
                    is State.Failure -> {
                        hideProgress()
                        when (it.errorType) {
                            LoginViewModel.ErrorState.Auth -> {

                                logEvent(EVENT_WRONG_PASSWORD)

                                binding.emailInputLayout.error = "Zlé meno alebo heslo"
                                binding.passwordInputLayout.error = "Zlé meno alebo heslo"
                            }
                            LoginViewModel.ErrorState.Network -> {
                                Snackbar.make(
                                    binding.root,
                                    R.string.network_error,
                                    Snackbar.LENGTH_SHORT
                                )
                            }
                        }
                    }
                }
            })

            createLiveData<String>(prefs.prefs, prefs.THEME).observe(this@LoginActivity, Observer { number ->
                val it = number.takeIf { it.isNotEmpty() } ?: "2"
                Log.d("TAG", "Main activity Actual value: $it")
                ThemeHelper.applyTheme(it.toInt())
                delegate.localNightMode = ThemeHelper.getLocalTheme(it.toInt())

            })
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }

    override fun onBackPressed() {
        if (hasPressedBack) {
            logEvent(ACTION_EXIT)
            //exit the whole app
            this.finishAffinity()
        } else {
            Toast.makeText(this, R.string.press_twice_exit, Toast.LENGTH_SHORT).show()

            hasPressedBack = true
            Handler().postDelayed({
                hasPressedBack = false
            }, 2000)
        }
    }

    private fun showProgress() {
        binding.progress.show()
        binding.loginButton.text = ""
    }

    private fun hideProgress() {
        binding.progress.hide()
        binding.loginButton.setText(R.string.login)
    }

    override val viewModel: LoginViewModel by viewModel { parametersOf(Bundle()) }

    override fun setBinding(): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
    override fun createViews() = Views()
    override fun getAnalytics() = FirebaseAnalytics.getInstance(this)
}