/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.java.main.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.huawei.agconnect.apms.APMS;
import com.huawei.agconnect.applinking.AGConnectAppLinking;
import com.huawei.agconnect.applinking.AppLinking;
import com.huawei.agconnect.auth.AGConnectAuth;

import com.huawei.agconnect.crash.AGConnectCrash;

import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;

import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.HomePageModel;
import com.huawei.podcast.java.interfaces.CategoryClickListener;


import com.google.android.material.navigation.NavigationView;
import com.huawei.podcast.java.preference.SharedPreference;
import com.huawei.podcast.java.utils.Constants;


public class MainActivity extends AppCompatActivity
        implements CategoryClickListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    HiAnalyticsInstance instance;
    public TextView nav_header_name;
    public TextView nav_header_email;
    Menu navigationMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        initializeViews();
        toggleDrawer();
        HomeFragment homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, homeFragment).commit();
        // Analytics
        instance = HiAnalytics.getInstance(this);
        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog();
        // crash
        AGConnectCrash.getInstance().enableCrashCollection(true);
        // init AppLinking
        AGConnectAppLinking.getInstance();
        APMS.getInstance().enableCollection(true);
    }

    /**
     * Initialize all widgets
     */
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        ImageView img_nav_menu = findViewById(R.id.img_nav_menu);
        ImageView img_search = findViewById(R.id.img_search);
        navigationMenu = navigationView.getMenu();
        View header = navigationView.getHeaderView(Constants.ZERO);
        nav_header_name = header.findViewById(R.id.nav_header_name);
        nav_header_email = header.findViewById(R.id.nav_header_email);
        ImageView nav_header_img = header.findViewById(R.id.nav_header_img);
        /* onclick listener */
        img_nav_menu.setOnClickListener(this);
        img_search.setOnClickListener(this);
        nav_header_name.setOnClickListener(this);
        nav_header_img.setOnClickListener(this);
    }

    /**
     * Creates an instance of the ActionBarDrawerToggle class:
     * 1) Handles opening and closing the navigation drawer
     * 2) Creates a hamburger icon in the toolbar
     * 3) Attaches listener to open/close drawer on icon clicked and rotates the icon
     */
    private void toggleDrawer() {
        ActionBarDrawerToggle drawerToggle =
                new ActionBarDrawerToggle(
                        this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        if (SharedPreference.contains(getString(R.string.username))) {
                            nav_header_name.setText(SharedPreference.getValueString(getString(R.string.username)));
                            nav_header_email.setText(SharedPreference.getValueString(getString(R.string.email)));
                            navigationMenu.findItem(R.id.nav_logout).setVisible(true);
                        }
                    }
                };
        drawerLayout.addDrawerListener(drawerToggle);
        // Configure the drawer layout to add listener and show icon on toolbar
        drawerToggle.isDrawerIndicatorEnabled();
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        // Checks if the navigation drawer is open -- If so, close it
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        // If drawer is already close -- Do not override original functionality
        else {
            super.onBackPressed();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()) {
            case R.id.nav_downloads:
                intent = new Intent(this, DownloadActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_subscription:
                intent = new Intent(this, SubscribeListActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_playback_history:
                intent = new Intent(this, PlayBackHistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_favorites:
                if (SharedPreference.contains(getString(R.string.username))) {
                    intent = new Intent(this, FavouritesActivity.class);
                    startActivity(intent);
                } else {
                    login();
                }
                break;
            case R.id.nav_add_pod_cast:
                intent = new Intent(this, AddToPodCastActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                AppLinking.Builder builder =
                        new AppLinking.Builder()
                                .setUriPrefix(Constants.domainUriPrefix)
                                .setDeepLink(Uri.parse(Constants.deepLink))
                                .setAndroidLinkInfo(new AppLinking.AndroidLinkInfo.Builder().build());
                builder.buildShortAppLinking()
                        .addOnSuccessListener(shortAppLinking -> com.huawei.podcast.java.utils.Utils.shareLink
                                (shortAppLinking.getShortUrl().toString(), this, instance))
                        .addOnFailureListener(
                                e -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show());
                break;
            case R.id.nav_logout:
                AGConnectAuth.getInstance().signOut();
                SharedPreference.removeValue(getString(R.string.username));
                Toast.makeText(this, getString(R.string.str_logged_out), Toast.LENGTH_LONG).show();
                nav_header_name.setText(getString(R.string.login));
                nav_header_email.setText(Constants.EMPTY_STRING);
                navigationMenu.findItem(R.id.nav_logout).setVisible(false);
                break;
        }
        closeDrawer();
        return true;
    }

    /**
     * Checks if the navigation drawer is open - if so, close it
     */
    private void closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_nav_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.img_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_header_name:
                login();
                break;
            case R.id.nav_header_img:
                login();
                break;
        }
    }

    private void login() {
        if (SharedPreference.contains(getString(R.string.username))) {
            nav_header_name.setText(SharedPreference.getValueString(getString(R.string.username)));
            nav_header_email.setText(SharedPreference.getValueString(getString(R.string.email)));
        } else {
            startActivityForResult(new Intent(this, LoginActivity.class), Constants.SIGN_CODE);
        }
    }

    // In your activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (SharedPreference.contains(getString(R.string.username)))
                nav_header_name.setText(SharedPreference.getValueString(getString(R.string.username)));
            nav_header_email.setText(SharedPreference.getValueString(getString(R.string.email)));
            navigationMenu.findItem(R.id.nav_logout).setVisible(true);
        }
    }


    @Override
    public void onItemClick(HomePageModel homePageModel) {
        Intent i = new Intent(this, DetailsActivity.class);
        i.putExtra(getString(R.string.topic), homePageModel.getLabel());
        startActivity(i);
    }
}
