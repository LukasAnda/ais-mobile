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

package com.lukasanda.aismobile.ui.login

//import com.lukasanda.aismobile.data.remote.SyncWorker
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.core.State
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.remote.SyncCoroutineWorker
import com.lukasanda.aismobile.ui.login.LoginViewModel.ErrorState.Auth
import com.lukasanda.aismobile.ui.login.LoginViewModel.ErrorState.Network
import com.lukasanda.aismobile.ui.main.MainActivity
import com.lukasanda.aismobile.util.hide
import com.lukasanda.aismobile.util.show
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModel<LoginViewModel>()
    private val prefs by inject<Prefs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onStart() {
        super.onStart()

        emailEditText.setText(prefs.username)
        passwordEditText.setText(prefs.password)

        if (prefs.username.isNotEmpty() && prefs.password.isNotEmpty()) {
            viewModel.login(emailEditText.text.toString(), passwordEditText.text.toString())
        }

        loginButton.setOnClickListener {
            viewModel.login(emailEditText.text.toString(), passwordEditText.text.toString())
        }

        viewModel.state.observe(this, Observer {
            when (it) {
                is State.Loading -> {
                    showProgress()
                }
                is State.Success -> {
                    hideProgress()

                    val constraints =
                        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)

                    val request = PeriodicWorkRequest.Builder(
                        SyncCoroutineWorker::class.java,
                        15,
                        TimeUnit.MINUTES
                    ).setConstraints(constraints.build())
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                        .build()
                    WorkManager.getInstance(applicationContext)
                        .enqueueUniquePeriodicWork(
                            "Sync",
                            ExistingPeriodicWorkPolicy.REPLACE,
                            request
                        )
                    startActivity(Intent(this, MainActivity::class.java))
                }
                is State.Failure -> {
                    hideProgress()
                    when (it.errorType) {
                        Auth -> {
                            emailInputLayout.error = "Zlé meno alebo heslo"
                            passwordInputLayout.error = "Zlé meno alebo heslo"
                        }
                        Network -> {
                            Snackbar.make(root, R.string.network_error, Snackbar.LENGTH_SHORT)
                        }
                    }
                }
            }
        })
    }

    private fun showProgress() {
        progress.show()
        loginButton.text = ""
    }

    private fun hideProgress() {
        progress.hide()
        loginButton.setText(R.string.login)
    }
}
