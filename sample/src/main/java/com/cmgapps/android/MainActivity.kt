/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cmgapps.android.apprater.AppRater
import com.cmgapps.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appRater: AppRater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appRater = (application as SampleApp).appRater

        if (appRater.checkForRating()) {
            appRater.show(this)
        }

        ActivityMainBinding.inflate(layoutInflater).apply {
            output.text = appRater.toString()
            setContentView(root)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun showDialogClicked(v: View) {
        appRater.show(this)
    }

    companion object {
        private val TAG = "MainActivity"
    }
}
