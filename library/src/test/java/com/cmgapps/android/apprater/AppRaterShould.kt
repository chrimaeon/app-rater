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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.format.DateUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppRaterShould {

    @Mock
    lateinit var mockContext: Context

    @Mock
    lateinit var mockAppContext: Context

    @Mock
    lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    lateinit var mockPackageManager: PackageManager

    private val clock = TestClock()

    @Before
    fun setup() {
        `when`(mockContext.applicationContext).thenReturn(mockAppContext)

        `when`(mockAppContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPreferences)

        `when`(mockAppContext.packageName).thenReturn("com.cmgapps")

        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)

        `when`(mockAppContext.packageManager)
            .thenReturn(mockPackageManager)
    }

    @Test
    fun `increment use count for new version`() {

        val trackingVersion = 1L
        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion.toInt()

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getLong(eq("tracking_version_long"), anyLong()))
            .thenAnswer { it.getArgument(1) }

        `when`(mockSharedPreferences.getInt(eq("use_count"), anyInt()))
            .thenReturn(0)

        val appRater = AppRater.Builder(mockContext, clock).build()
        appRater.incrementUseCount()

        verify(mockSharedPreferences).getLong(eq("tracking_version_long"), anyLong())
        verify(mockEditor).putLong("tracking_version_long", trackingVersion)
        verify(mockEditor).putLong("first_use", 305078400)
        verify(mockSharedPreferences, times(2)).getInt(eq("use_count"), anyInt())
        verify(mockEditor).putInt("use_count", 1)
    }

    @Test
    fun `increment use count for existing version`() {

        val trackingVersion = 1L

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion.toInt()

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getLong(eq("tracking_version_long"), anyLong()))
            .thenReturn(trackingVersion)

        val appRater = AppRater.Builder(mockContext, clock).build()
        appRater.incrementUseCount()

        verify(mockSharedPreferences).getLong(eq("tracking_version_long"), anyLong())
        verify(mockEditor, times(0)).putLong("tracking_version_long", trackingVersion)
        verify(mockEditor).putLong("first_use", 305078400)
        verify(mockSharedPreferences, times(2)).getInt(eq("use_count"), anyInt())
        verify(mockEditor).putInt("use_count", 1)
    }

    @Test
    fun `reset for new version`() {

        val oldTrackingVersion = 1L
        val newTrackingVersion = oldTrackingVersion + 1

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = newTrackingVersion.toInt()

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getLong(eq("tracking_version_long"), anyLong()))
            .thenReturn(oldTrackingVersion)

        val appRater = AppRater.Builder(mockContext, clock).build()
        appRater.incrementUseCount()

        verify(mockSharedPreferences).getLong(eq("tracking_version_long"), anyLong())
        verify(mockEditor).putLong("tracking_version_long", newTrackingVersion)
        verify(mockEditor).putLong("first_use", 305078400)
        verify(mockEditor).putInt("use_count", 1)
        verify(mockEditor).putBoolean("declined_rate", false)
        verify(mockEditor).putLong("remind_later_date", 0L)
        verify(mockEditor).putBoolean("rated", false)
    }

    @Test
    fun `check for rating complete`() {
        val trackingVersion = 1

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getBoolean(eq("declined_rate"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getBoolean(eq("rated"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getLong(eq("first_use"), anyLong()))
            .thenReturn(0)
        `when`(mockSharedPreferences.getInt(eq("use_count"), anyInt()))
            .thenReturn(100)
        `when`(mockSharedPreferences.getLong(eq("remind_later_date"), anyLong()))
            .thenReturn(0)

        val appRater = AppRater.Builder(mockContext).build()
        assertThat(appRater.checkForRating(), `is`(true))
    }

    @Test
    fun `check for rating declined rate`() {
        val trackingVersion = 1

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)
        `when`(mockSharedPreferences.getBoolean(eq("declined_rate"), anyBoolean()))
            .thenReturn(true)

        val appRater = AppRater.Builder(mockContext).build()
        assertThat(appRater.checkForRating(), `is`(false))
    }

    @Test
    fun `check for rating already rated`() {
        val trackingVersion = 1

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getBoolean(eq("declined_rate"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getBoolean(eq("rated"), anyBoolean()))
            .thenReturn(true)

        val appRater = AppRater.Builder(mockContext).build()
        assertThat(appRater.checkForRating(), `is`(false))
    }

    @Test
    fun `check for rating first use to just happened`() {
        val trackingVersion = 1

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getBoolean(eq("declined_rate"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getBoolean(eq("rated"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getLong(eq("first_use"), anyLong()))
            .thenReturn(305078400)

        val appRater = AppRater.Builder(mockContext, clock).build()
        assertThat(appRater.checkForRating(), `is`(false))
    }

    @Test
    fun `check for rating with use count too low`() {
        val trackingVersion = 1

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getBoolean(eq("declined_rate"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getBoolean(eq("rated"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getLong(eq("first_use"), anyLong()))
            .thenReturn(0)
        `when`(mockSharedPreferences.getInt(eq("use_count"), anyInt()))
            .thenReturn(1)

        val appRater = AppRater.Builder(mockContext).build()
        assertThat(appRater.checkForRating(), `is`(false))
    }

    @Test
    fun `check for rating remind later too low`() {
        val trackingVersion = 1
        val daysUntilPrompt = 3

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getBoolean(eq("declined_rate"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getBoolean(eq("rated"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getLong(eq("first_use"), anyLong()))
            .thenReturn(305078400 - ((daysUntilPrompt + 1) * DateUtils.DAY_IN_MILLIS))
        `when`(mockSharedPreferences.getInt(eq("use_count"), anyInt()))
            .thenReturn(100)
        `when`(mockSharedPreferences.getLong(eq("remind_later_date"), anyLong()))
            .thenReturn(305078400)

        val appRater =
            AppRater.Builder(mockContext, clock).daysUntilPrompt(daysUntilPrompt).build()
        assertThat(appRater.checkForRating(), `is`(false))
    }

    @Test
    fun `check for rating with valid check requirements`() {
        val trackingVersion = 1

        val clock = object : Clock {
            override fun millis() = 11 * DateUtils.DAY_IN_MILLIS
        }

        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getBoolean(eq("declined_rate"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getBoolean(eq("rated"), anyBoolean()))
            .thenReturn(false)
        `when`(mockSharedPreferences.getLong(eq("first_use"), anyLong()))
            .thenReturn(0)
        `when`(mockSharedPreferences.getInt(eq("use_count"), anyInt()))
            .thenReturn(100)
        `when`(mockSharedPreferences.getLong(eq("remind_later_date"), anyLong()))
            .thenReturn(0)

        val appRater = AppRater.Builder(mockContext, clock).build()
        assertThat(appRater.checkForRating(), `is`(true))
    }

    @Test
    fun `manually set 'rated'`() {
        val trackingVersion = 1L
        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.versionCode = trackingVersion.toInt()

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
            .thenReturn(packageInfo)

        val appRater = AppRater.Builder(mockContext).build()
        appRater.setCurrentVersionRated()
        verify(mockEditor).putBoolean("rated", true)
    }
}
