/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cmgapps.android.apprater.AppRater
import com.cmgapps.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appRater: AppRater
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appRater = (application as SampleApp).appRater

        if (appRater.checkForRating()) {
            appRater.show(this)
        }

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            button.setOnClickListener {
                appRater.show(this@MainActivity)
            }
            setContentView(root)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.output.text = appRater.toString()
    }

    private companion object {
        private val TAG = "MainActivity"
    }
}
