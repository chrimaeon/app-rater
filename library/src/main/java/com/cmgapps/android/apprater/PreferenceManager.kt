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
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

internal class PreferenceManager(context: Context, private val clock: Clock) {

    private val pref: SharedPreferences =
        context.getSharedPreferences(APP_RATE_FILE_NAME, Context.MODE_PRIVATE)

    var declinedToRate: Boolean
        get() = pref.getBoolean(DECLINED_RATE, false)
        set(declined) {
            pref.edit().apply {
                putBoolean(DECLINED_RATE, declined)
                apply()
            }
        }

    var appRated: Boolean
        get() = pref.getBoolean(APP_RATED, false)
        set(rated) {
            pref.edit().apply {
                putBoolean(APP_RATED, rated)
                apply()
            }
        }

    var firstUsedTimestamp: Long
        get() = pref.getLong(FIRST_USE, 0L)
        set(timestamp) {
            pref.edit().apply {
                putLong(FIRST_USE, timestamp)
                apply()
            }
        }

    val useCount: Int
        get() = pref.getInt(USE_COUNT, 0)

    var remindLaterTimeStamp: Long
        get() = pref.getLong(REMIND_LATER_DATE, 0L)
        set(timeStamp) {
            pref.edit().apply {
                putLong(REMIND_LATER_DATE, timeStamp)
                apply()
            }
        }

    var trackingVersion: Long
        get() = pref.getLong(TRACKING_VERSION_LONG, -1)
        set(versionCode) {
            pref.edit().apply {
                putLong(TRACKING_VERSION_LONG, versionCode)
                apply()
            }
        }

    fun incrementUseCount() {
        pref.edit().apply {
            putInt(USE_COUNT, useCount + 1)
            apply()
        }
    }

    fun resetNewVersion(versionCode: Long) {
        pref.edit().apply {
            putLong(TRACKING_VERSION_LONG, versionCode)
            putLong(FIRST_USE, clock.millis())
            putInt(USE_COUNT, 1)
            putBoolean(DECLINED_RATE, false)
            putLong(REMIND_LATER_DATE, 0L)
            putBoolean(APP_RATED, false)
            apply()
        }
    }

    override fun toString(): String {
        return JSONObject().apply {
            try {
                put(DECLINED_RATE, declinedToRate)
                put(APP_RATED, appRated)
                put(TRACKING_VERSION_LONG, trackingVersion)
                put(
                    FIRST_USE,
                    SimpleDateFormat.getDateTimeInstance().format(Date(firstUsedTimestamp))
                )
                put(USE_COUNT, useCount)
                put(
                    REMIND_LATER_DATE,
                    SimpleDateFormat.getDateTimeInstance().format(
                        Date(remindLaterTimeStamp)
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
