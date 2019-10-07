/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.cmgapps.android.apprater.AppRater
import com.cmgapps.android.apprater.appRater

class SampleApp : Application() {

    lateinit var appRater: AppRater
        private set

    override fun onCreate() {
        super.onCreate()
        appRater = appRater(this) {
            if (BuildConfig.DEBUG) {
                debug(true)
            }
            daysUntilPrompt(0)
            launchesUntilPrompt(0)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(appRater))
    }
}

class AppLifecycleListener(private val appRater: AppRater) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        appRater.incrementUseCount()
    }
}
