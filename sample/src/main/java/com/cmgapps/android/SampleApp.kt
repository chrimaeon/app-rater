/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.cmgapps.android.apprater.AppRater

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val apprater = AppRater.Builder(this).build()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(apprater))

    }
}

class AppLifecycleListener(private val appRater: AppRater) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        appRater.incrementUseCount()
    }
}
