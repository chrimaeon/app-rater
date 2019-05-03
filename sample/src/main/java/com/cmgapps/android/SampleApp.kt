/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.cmgapps.android.apprater.AppRater

/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 */

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val apprater = AppRater.Builder(this).build()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(apprater))

    }
}

class AppLifecycleListener(private val appRater: AppRater) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        appRater.incrementUseCount()
    }
}
