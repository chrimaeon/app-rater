/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com>
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

package com.cmgapps.android.apprater

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import com.cmgapps.android.apprater.databinding.DialogContentBinding
import com.cmgapps.android.apprater.store.GooglePlayStore
import com.cmgapps.android.apprater.store.Store

class AppRater private constructor(builder: Builder) {

    private val debug = builder.debug
    private val launchesUntilPrompt = builder.launchesUntilPrompt
    private val daysUntilPrompt = builder.daysUntilPrompt
    private val daysUntilRemindAgain = builder.daysUntilRemindAgain
    private val store = builder.store
    private val preferenceManager = PreferenceManager(builder.context, builder.clock)
    private val versionCode: Long = try {
        val context = builder.context
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        } else {
            packageInfo.longVersionCode
        }
    } catch (exc: NameNotFoundException) {
        Log.e(TAG, "PackageName not found: " + builder.context.packageName)
        0
    }
    private val clock = builder.clock

    /**
     * Call to check if the requirements to open the rating dialog are met
     *
     * @return true if requirements are met.
     */
    fun checkForRating(): Boolean {

        if (debug) {
            Log.i(TAG, "Rater Content:" + toString())
        }

        val now = clock.millis()

        return !preferenceManager.declinedToRate &&
            !preferenceManager.appRated &&
            now >= preferenceManager.firstUsedTimestamp + daysUntilPrompt &&
            preferenceManager.useCount > launchesUntilPrompt &&
            now >= preferenceManager.remindLaterTimeStamp + daysUntilRemindAgain
    }

    /**
     * Increments the usage count.
     */
    fun incrementUseCount() {

        var trackingVersion = preferenceManager.trackingVersion

        if (trackingVersion == -1L) {
            trackingVersion = versionCode
            preferenceManager.trackingVersion = trackingVersion
        }

        if (trackingVersion == versionCode) {

            if (preferenceManager.useCount.toLong() == 0L) {
                preferenceManager.firstUsedTimestamp = clock.millis()
            }

            preferenceManager.incrementUseCount()
        } else {
            preferenceManager.resetNewVersion(versionCode)
        }
    }

    /**
     * Shows a default [AlertDialog].
     *
     * @param activity An [Activity] to show the dialog from.
     */
    @SuppressLint("ClickableViewAccessibility")
    fun show(activity: Activity) {
        val pm = activity.packageManager

        val appName = try {
            pm.getApplicationInfo(activity.packageName, 0).let {
                pm.getApplicationLabel(it)
            }
        } catch (e: NameNotFoundException) {
            Log.e(TAG, "Application name can not be found")
            "App"
        }

        val dialogContentBinding =
            DialogContentBinding.inflate(activity.layoutInflater).apply {
                header.text =
                    activity.getString(R.string.dialog_cmgrate_message_fmt, appName)
            }

        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_cmgrate_title)
            .setView(dialogContentBinding.root)
            .setPositiveButton(R.string.dialog_cmgrate_ok) { _, _ ->
                setCurrentVersionRated()

                val intent = Intent(Intent.ACTION_VIEW, store.getStoreUri(activity))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            }
            .setNeutralButton(R.string.dialog_cmgrate_later) { _, _ ->
                preferenceManager.remindLaterTimeStamp = clock.millis()
            }
            .setNegativeButton(R.string.dialog_cmgrate_no) { _, _ ->
                preferenceManager.declinedToRate = true
            }
            .setOnCancelListener {
                preferenceManager.remindLaterTimeStamp = clock.millis()
            }.create().also {
                dialogContentBinding.ratingBar.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        setCurrentVersionRated()

                        val intent = Intent(Intent.ACTION_VIEW, store.getStoreUri(activity))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        activity.startActivity(intent)
                        it.dismiss()
                    }
                    true
                }
            }.show()
    }

    /**
     * Get the [SharedPreferences] file contents
     */
    override fun toString(): String {
        return preferenceManager.toString()
    }

    /**
     * Manually set the rated flag for the current version.
     *
     * Useful when not using the provided [AlertDialog]; i.e. when using
     * [Google Play In-App Review API](https://developer.android.com/guide/playcore/in-app-review)
     *
     */
    fun setCurrentVersionRated() {
        preferenceManager.appRated = true
    }

    /**
     * Builder for [AppRater]
     *
     * default values:
     * * store = [GooglePlayStore]
     * * launchesUntilPrompt = 5
     * * daysUntilPrompt = 10
     * * daysUntilRemindAgain = 5
     * * debug = false
     *
     */
    class Builder {
        constructor(context: Context) {
            this.context = context.applicationContext
            this.clock = SystemClock()
        }

        @VisibleForTesting
        internal constructor(context: Context, clock: Clock) {
            this.context = context.applicationContext
            this.clock = clock
        }

        internal val context: Context
        internal val clock: Clock

        internal var store: Store = GooglePlayStore()
            private set

        internal var launchesUntilPrompt = 5
            private set

        internal var daysUntilPrompt = 10 * DateUtils.DAY_IN_MILLIS
            private set

        internal var daysUntilRemindAgain = 5 * DateUtils.DAY_IN_MILLIS
            private set

        internal var debug = false
            private set

        /**
         * Set the store to open for rating
         */
        fun store(store: Store) = apply {
            this.store = store
        }

        /**
         * Sets the minimum app launches until the dialog is shown
         */
        fun launchesUntilPrompt(launchesUntilPrompt: Int) = apply {
            this.launchesUntilPrompt = launchesUntilPrompt
        }

        /**
         * Set the minimal days to pass until the dialog is shown
         */
        fun daysUntilPrompt(daysUntilPrompt: Int) = apply {
            this.daysUntilPrompt = daysUntilPrompt * DateUtils.DAY_IN_MILLIS
        }

        /**
         * Set the days until the dialog is shown again
         */
        fun daysUntilRemindAgain(daysUntilRemindAgain: Int) = apply {
            this.daysUntilRemindAgain = daysUntilRemindAgain * DateUtils.DAY_IN_MILLIS
        }

        /**
         * Enables debug mode with Logcat output id the current state
         */
        fun debug(debug: Boolean) = apply {
            this.debug = debug
        }

        /**
         * creates the App Rater instance
         */
        fun build(): AppRater {
            return AppRater(this)
        }
    }

    private companion object {
        private const val TAG = "AppRater"
    }
}
