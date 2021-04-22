/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.podcast.kotlin.main.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import com.huawei.agconnect.apms.APMS
import com.huawei.agconnect.applinking.AGConnectAppLinking
import com.huawei.agconnect.applinking.AppLinking
import com.huawei.agconnect.applinking.ShortAppLinking
import com.huawei.agconnect.auth.* // ktlint-disable no-wildcard-imports
import com.huawei.agconnect.crash.AGConnectCrash
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.analytics.HiAnalyticsTools
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.preference.SharedPreference
import com.huawei.podcast.kotlin.utils.Constants
import kotlinx.android.synthetic.main.activity_navigation_drawer.*
import kotlinx.android.synthetic.main.nav_header_layout.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    var instance: HiAnalyticsInstance? = null
    private lateinit var navMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_drawer)
        // default fragment
        val tx: FragmentTransaction = supportFragmentManager.beginTransaction()
        tx.replace(R.id.frame_layout, HomeFragment())
        tx.commit()
        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer_layout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                nav_header_name.setOnClickListener(this@MainActivity)
                nav_header_img.setOnClickListener(this@MainActivity)
                if (SharedPreference.contains(getString(R.string.username))) {
                    nav_header_name.text = SharedPreference.getValueString(getString(R.string.username))
                    nav_header_email.text = SharedPreference.getValueString(getString(R.string.email))
                    navMenu.findItem(R.id.nav_logout).isVisible = true
                }
            }
        }
        // Configure the drawer layout to add listener and show icon on toolbar
        drawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        /*hide logout button based on login status*/
        navMenu = navigation_view.menu
        // Set navigation view navigation item selected listener
        navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_downloads -> {
                    val i = Intent(this, DownloadActivity::class.java)
                    startActivity(i)
                }
                R.id.nav_subscription -> {
                    val i = Intent(this, SubscribeListActivity::class.java)
                    startActivity(i)
                }
                R.id.nav_playback_history -> {
                    val i = Intent(this, PlayBackHistoryActivity::class.java)
                    startActivity(i)
                }
                R.id.nav_favorites -> {
                    if (SharedPreference.contains(getString(R.string.username))) {
                        val i = Intent(this, FavouritesActivity::class.java)
                        startActivity(i)
                    } else {
                        login()
                    }
                }
                R.id.nav_add_pod_cast -> {
                    val i = Intent(this, AddToPodCastActivity::class.java)
                    startActivity(i)
                }
                R.id.nav_share -> {
                    val builder = AppLinking.Builder().setUriPrefix(Constants.domainUriPrefix)
                        .setDeepLink(Uri.parse(Constants.deepLink))
                        .setAndroidLinkInfo(AppLinking.AndroidLinkInfo.Builder().build())
                    builder.buildShortAppLinking()
                        .addOnSuccessListener { shortAppLinking: ShortAppLinking ->
                            shareLink(shortAppLinking.shortUrl.toString())
                        }.addOnFailureListener {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.app_link),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                R.id.nav_logout -> {
                    AGConnectAuth.getInstance().signOut()
                    SharedPreference.removeValue(getString(R.string.username))
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.str_logged_out),
                        Toast.LENGTH_LONG
                    ).show()
                    nav_header_name.text = getString(R.string.login)
                    nav_header_email.text = getString(R.string.double_quotes)
                    navMenu.findItem(R.id.nav_logout).isVisible = false
                }
            }
            // Close the drawer
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }
        /*onclick listener*/
        img_nav_menu.setOnClickListener(this)
        img_search.setOnClickListener(this)

        // Analytics
        instance = HiAnalytics.getInstance(this)
        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog()
        // crash
        AGConnectCrash.getInstance().enableCrashCollection(true)
        // init AppLinking
        AGConnectAppLinking.getInstance()
        APMS.getInstance().enableCollection(true)
    }

    override fun onBackPressed() {
        // Checks if the navigation drawer is open -- If so, close it
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Sharing App link
     */
    private fun shareLink(agcLink: String?) {
        if (agcLink != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = Constants.type
                putExtra(Intent.EXTRA_TEXT, agcLink)
            }
            startActivity(intent)
            val bundle = Bundle()
            bundle.putString(getString(R.string.share), getString(R.string.count))
            instance?.onEvent(getString(R.string.count), bundle)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_nav_menu -> drawer_layout.openDrawer(GravityCompat.START)
            R.id.img_search -> {
                val i = Intent(this, SearchActivity::class.java)
                startActivity(i)
            }
            R.id.nav_header_name -> {
                login()
            }
            R.id.nav_header_img -> {
                login()
            }
        }
    }
    /**
     * Login with AGCUI
     */
    private fun login() {
        if (SharedPreference.contains(getString(R.string.username))) {
            nav_header_name.text = SharedPreference.getValueString(getString(R.string.username))
            nav_header_email.text = SharedPreference.getValueString(getString(R.string.email))
        } else {
            startActivityForResult(
                Intent(
                    this,
                    LoginActivity::class.java
                ),
                Constants.SIGNIN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_OK) {

            nav_header_name.text = SharedPreference.getValueString(getString(R.string.username))
            nav_header_email.text = SharedPreference.getValueString(getString(R.string.email))
            navMenu.findItem(R.id.nav_logout).isVisible = true
        }
    }
}
