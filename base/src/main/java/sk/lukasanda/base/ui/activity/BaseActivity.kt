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

package sk.lukasanda.base.ui.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.viewbinding.ViewBinding
import sk.lukasanda.base.ui.trait.LifecycleTrait
import sk.lukasanda.base.ui.viewmodel.BaseViewModel

abstract class BaseUIActivity<VIEWMODEL : BaseViewModel, BASE_VIEWS : BaseActivityViews, BINDING : ViewBinding> :
    AppCompatActivity(), LifecycleTrait {

    protected var views: BASE_VIEWS? = null
    abstract val viewModel: VIEWMODEL

    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var toolbar: Toolbar? = null
    @Suppress("MemberVisibilityCanBePrivate")
    protected lateinit var binding: BINDING
    @Suppress("MemberVisibilityCanBePrivate")
    protected var drawerLayout: DrawerLayout? = null
    @Suppress("MemberVisibilityCanBePrivate")
    protected lateinit var activityLayoutCL: CoordinatorLayout
    @Suppress("MemberVisibilityCanBePrivate")
    protected var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.lifecycle.addObserver(this)

        this.binding = this.setBinding()

        setContentView(binding.root)

        this.initializeParameters()
        this.initializeViews()
        this.initializeToolbar()
        this.initializeNavigationDrawer(this.binding.root)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initializeParameters() =
        this.intent.extras?.let(viewModel::setHandleValues)


    private fun initializeViews() {
        this.views = this.createViews()
        this.navController = this.views?.setNavigationGraph()?.let { navigationGraph ->
            Navigation.findNavController(this, navigationGraph)
        }
        this.views?.modifyViews()
    }

    private fun initializeToolbar() {
        this.toolbar = this.setToolbar()
        toolbar?.let { it ->
            it.setNavigationOnClickListener { this@BaseUIActivity.onBackPressed() }
            //this.toolbar.setNavigationIcon(R.drawable.ic_back)
        }
    }

    private fun initializeNavigationDrawer(activityLayout: View) {
        this.drawerLayout = setDrawer()
        drawerLayout?.let {
            it.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            this.actionBarDrawerToggle = ActionBarDrawerToggle(this, it, this.toolbar, 0, 0)
            it.addDrawerListener(this.actionBarDrawerToggle!!)
        }
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

    abstract fun createViews(): BASE_VIEWS
}

interface BaseViews {
    fun modifyViews()

}

interface BaseActivityViews : BaseViews {
    fun setNavigationGraph(): Int?
}

