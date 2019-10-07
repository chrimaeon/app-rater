/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
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

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

internal class PreferenceManager(context: Context) {

    private val pref: SharedPreferences =
        context.getSharedPreferences(APP_RATE_FILE_NAME, Context.MODE_PRIVATE)

    var declinedToRate: Boolean
        get() = pref.getBoolean(DECLINED_RATE, false)
        set(declined) = pref.edit { putBoolean(DECLINED_RATE, declined) }

    var appRated: Boolean
        get() = pref.getBoolean(APP_RATED, false)
        set(rated) = pref.edit { putBoolean(APP_RATED, rated) }

    var firstUsedTimestamp: Long
        get() = pref.getLong(FIRST_USE, 0L)
        set(timestamp) = pref.edit { putLong(FIRST_USE, timestamp) }

    val useCount: Int
        get() = pref.getInt(USE_COUNT, 0)

    var remindLaterTimeStamp: Long
        get() = pref.getLong(REMIND_LATER_DATE, 0L)
        set(timeStamp) = pref.edit { putLong(REMIND_LATER_DATE, timeStamp) }

    var trackingVersion: Long
        get() = pref.getLong(TRACKING_VERSION_LONG, -1)
        set(versionCode) = pref.edit { putLong(TRACKING_VERSION_LONG, versionCode) }

    fun incrementUseCount() = pref.edit { putInt(USE_COUNT, useCount + 1) }

    fun resetNewVersion(versionCode: Long) {
        pref.edit {
            putLong(TRACKING_VERSION_LONG, versionCode)
            putLong(FIRST_USE, System.currentTimeMillis())
            putInt(USE_COUNT, 1)
            putBoolean(DECLINED_RATE, false)
            putLong(REMIND_LATER_DATE, 0L)
            putBoolean(APP_RATED, false)
        }
    }

    override fun toString(): String {
        return JSONObject().apply {
            try {
                put(DECLINED_RATE, pref.getBoolean(DECLINED_RATE, false))
                put(APP_RATED, pref.getBoolean(APP_RATED, false))
                put(TRACKING_VERSION_LONG, pref.getLong(TRACKING_VERSION_LONG, -1))
                put(
                    FIRST_USE,
                    SimpleDateFormat.getDateTimeInstance().format(Date(pref.getLong(FIRST_USE, 0L)))
                )
                put(USE_COUNT, pref.getInt(USE_COUNT, 0))
                put(
                    REMIND_LATER_DATE,
                    SimpleDateFormat.getDateTimeInstance().format(
                        Date(
                            pref.getLong(
                                REMIND_LATER_DATE,
                                0L
                            )
                        )
                    )
                )
            } catch (exc: JSONException) {
                Log.e("PreferenceManager", "Error creating JSON Object ", exc)
            }
        }.toString(2)
    }

    companion object {
        private const val APP_RATE_FILE_NAME = "AppRater"
        private const val APP_RATED = "rated"
        private const val REMIND_LATER_DATE = "remind_later_date"
        private const val DECLINED_RATE = "declined_rate"
        private const val USE_COUNT = "use_count"
        private const val FIRST_USE = "first_use"
        private const val TRACKING_VERSION_LONG = "tracking_version_long"
    }
}
