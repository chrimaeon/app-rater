/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cmgapps.android.apprater.AppRater

class MainActivity : AppCompatActivity() {
    private lateinit var appRater: AppRater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appRater = (application as SampleApp).appRater

        if (appRater.checkForRating()) {
            appRater.show(this)
        }

        findViewById<TextView>(R.id.output)?.apply {
            text = appRater.toString()
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
