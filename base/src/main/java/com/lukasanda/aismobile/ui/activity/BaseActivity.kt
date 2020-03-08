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

package com.lukasanda.aismobile.ui.activity

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.viewbinding.ViewBinding
import com.lukasanda.aismobile.ui.trait.LifecycleTrait
import com.lukasanda.aismobile.ui.viewmodel.BaseViewModel

abstract class BaseUIActivity<VIEWMODEL : BaseViewModel, BASE_VIEWS : BaseActivityViews, BINDING : ViewBinding> :
    AppCompatActivity(), LifecycleTrait {

    protected var views: BASE_VIEWS? = null
    abstract val viewModel: VIEWMODEL

    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var toolbar: Toolbar? = null
    protected lateinit var binding: BINDING
    protected var drawerLayout: DrawerLayout? = null
    protected lateinit var activityLayoutCL: CoordinatorLayout
    protected var navController: NavController? = null

    private var appBarConfig: AppBarConfiguration? = null

    private fun logThisName() {
        Log.d("TAG", this.javaClass.simpleName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.lifecycle.addObserver(this)
        logThisName()

        this.binding = this.setBinding()

        setContentView(binding.root)

        this.initializeParameters()
        this.initializeViews()

        this.toolbar = this.setToolbar()

        this.drawerLayout = setDrawer()

        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowHomeEnabled(true)
//            setHomeButtonEnabled(true)
//            setDisplayHomeAsUpEnabled(true)
        }

        appBarConfig = setAppBarConfig()

        appBarConfig?.let {
            navController?.let { it1 ->
                NavigationUI.setupActionBarWithNavController(
                    this,
                    it1, it
                )
            }
        }

        this.views?.modifyViews()
    }

    override fun onSupportNavigateUp(): Boolean {
        return appBarConfig?.let { navController?.navigateUp(it) } ?: false || super.onSupportNavigateUp()
    }

    private fun initializeParameters() =
        this.intent.extras?.let(viewModel::setHandleValues)


    private fun initializeViews() {
        this.views = this.createViews()
        this.navController = this.views?.setNavigationGraph()?.let { navigationGraph ->
            Navigation.findNavController(this, navigationGraph)
        }
    }

    private fun initializeToolbar() {
        this.toolbar = this.setToolbar()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle?.onOptionsItemSelected(item) == true) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    abstract fun setBinding(): BINDING

    protected open fun setDrawer(): DrawerLayout? = null

    protected open fun setToolbar(): Toolbar? = null

    protected open fun setAppBarConfig(): AppBarConfiguration? = null

    abstract fun createViews(): BASE_VIEWS
}

interface BaseViews {
    fun modifyViews()
}

interface BaseActivityViews : BaseViews {
    fun setNavigationGraph(): Int?
}

