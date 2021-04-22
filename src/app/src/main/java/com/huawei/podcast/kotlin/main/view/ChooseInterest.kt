/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.podcast.kotlin.main.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.data.model.HomePageModel
import com.huawei.podcast.kotlin.interfaces.CategoryClickListener
import com.huawei.podcast.kotlin.main.adapter.TrendingAdapter
import com.huawei.podcast.kotlin.utils.Constants
import kotlinx.android.synthetic.main.activity_choose_your_interest.*
import kotlinx.android.synthetic.main.include_header.*

class ChooseInterest : AppCompatActivity(), CategoryClickListener {

    private lateinit var adapter: TrendingAdapter
    private var isAllowed = false // = checkReadPermissionBoolean();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_your_interest)
        setupUI()
    }

    private fun setupUI() {
        rv_interest.layoutManager = GridLayoutManager(this, 2).also { rv_interest.layoutManager = it }
        adapter = TrendingAdapter(this)
        rv_interest.adapter = adapter
        img_back_arrow.visibility = View.GONE
        txt_title.text = getString(R.string.interest)
        renderList()
        isAllowed = checkReadPermissionBoolean()
    }

    private fun renderList() {
        val interestList = resources.getStringArray(R.array.choose_your_interest).toList()
        val chooseInterestList = ArrayList<HomePageModel>()
        for (items in interestList) {
            chooseInterestList.add(HomePageModel(items))
        }
        adapter.setList(chooseInterestList)
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(category: HomePageModel) {
        if (isAllowed) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        } else {
            requestPermission()
        }
    }

    private fun checkReadPermissionBoolean(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            Constants.REQUEST_CODE_ONE
        )
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Checking whether user granted the permission or not.
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Not granted
            isAllowed = true
            Toast.makeText(
                this,
                getString(R.string.grant_permission),
                Toast.LENGTH_SHORT
            ).show()
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        } else {
            Toast.makeText(
                this,
                getString(R.string.allow_permission),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
