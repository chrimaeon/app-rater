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
import androidx.appcompat.app.AlertDialog
import com.cmgapps.android.apprater.databinding.DialogContentBinding
import com.cmgapps.android.apprater.store.GooglePlayStore
import com.cmgapps.android.apprater.store.Store

class AppRater private constructor(builder: Builder) {

    private val debug = builder._debug
    private val launchesUntilPrompt = builder._launchesUntilPrompt
    private val daysUntilPrompt = builder._daysUntilPrompt
    private val daysUntilRemindAgain = builder._daysUntilRemindAgain
    private val store = builder._store
    private val preferenceManager = PreferenceManager(builder.mContext)

    private val versionCode: Long

    init {
        val context = builder.mContext

        versionCode = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            } else {
                packageInfo.longVersionCode
            }
        } catch (exc: NameNotFoundException) {
            Log.e(TAG, "PackageName not found: " + context.packageName)
            0
        }
    }

    /**
     * Call to check if the requirements to open the rating dialog are met
     *
     * @return true if requirements are met.
     */
    fun checkForRating(): Boolean {

        if (debug) {
            Log.i(TAG, "Rater Content:" + toString())
        }

        return !preferenceManager.declinedToRate &&
            !preferenceManager.appRated &&
            System.currentTimeMillis() >= preferenceManager.firstUsedTimestamp + daysUntilPrompt &&
            preferenceManager.useCount > launchesUntilPrompt &&
            System.currentTimeMillis() >= preferenceManager.remindLaterTimeStamp + daysUntilRemindAgain
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
                preferenceManager.firstUsedTimestamp = System.currentTimeMillis()
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
            val ai = pm.getApplicationInfo(activity.packageName, 0)
            pm.getApplicationLabel(ai) as String
        } catch (e: NameNotFoundException) {
            Log.e(TAG, "Application name can not be found")
            "App"
        }

        val dialogContentBinding = DialogContentBinding.inflate(activity.layoutInflater).apply {
            header.text =
                activity.getString(R.string.dialog_cmgrate_message_fmt, appName)
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_cmgrate_title)
            .setView(dialogContentBinding.root)
            .setPositiveButton(R.string.dialog_cmgrate_ok) { _, _ ->
                preferenceManager.appRated = true

                val intent = Intent(Intent.ACTION_VIEW, store.getStoreUri(activity))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            }
            .setNeutralButton(R.string.dialog_cmgrate_later) { _, _ ->
                preferenceManager.remindLaterTimeStamp = System.currentTimeMillis()
            }
            .setNegativeButton(R.string.dialog_cmgrate_no) { _, _ ->
                preferenceManager.declinedToRate = true
            }
            .setOnCancelListener {
                preferenceManager.remindLaterTimeStamp = System.currentTimeMillis()
            }.create().also {
                dialogContentBinding.ratingBar.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        preferenceManager.appRated = true

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

    @Suppress("PropertyName")
    open class Builder(internal val mContext: Context) {
        internal var _store: Store = GooglePlayStore()
            private set

        internal var _launchesUntilPrompt = 5
            private set

        internal var _daysUntilPrompt = 10 * DateUtils.DAY_IN_MILLIS
            private set

        internal var _daysUntilRemindAgain = 5 * DateUtils.DAY_IN_MILLIS
            private set

        internal var _debug = false
            private set

        /**
         * Set the store to open for rating
         */
        fun store(store: Store) = apply {
            _store = store
        }

        /**
         * Sets the minimun app lauched until the dialog is shown
         */
        fun launchesUntilPrompt(launchesUntilPrompt: Int) = apply {
            _launchesUntilPrompt = launchesUntilPrompt
        }

        /**
         * Set the minimal days to pass until the dialog is shown
         */
        fun daysUntilPrompt(daysUntilPrompt: Int) = apply {
            _daysUntilPrompt = daysUntilPrompt * DateUtils.DAY_IN_MILLIS
        }

        /**
         * Set the days until the dialog is shown again
         */
        fun daysUntilRemindAgain(daysUntilRemindAgain: Int) = apply {
            _daysUntilRemindAgain = daysUntilRemindAgain * DateUtils.DAY_IN_MILLIS
        }

        /**
         * Enables debug mode with Logcat output id the current state
         */
        fun debug(debug: Boolean) = apply {
            _debug = debug
        }

        /**
         * creates the App Rater instance
         */
        fun build(): AppRater {
            return AppRater(this)
        }
    }

    companion object {
        private const val TAG = "AppRater"
    }
}
