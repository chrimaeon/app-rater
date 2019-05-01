/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppRaterShould {

    @Mock
    lateinit var mockContext: Context

    @Mock
    lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    lateinit var mockPackageManager: PackageManager

    @Before
    fun setup() {


        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(mockSharedPreferences)

        `when`(mockContext.packageName).thenReturn("com.cmgapps")

        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)

        `when`(mockContext.packageManager)
                .thenReturn(mockPackageManager)

    }

    @Test
    fun `increment use count for new version`() {

        val trackingVersion = 1
        val packageInfo = PackageInfo()
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
                .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getInt(eq("tracking_version"), anyInt()))
                .thenAnswer { it.getArgument(1) }

        `when`(mockSharedPreferences.getInt(eq("use_count"), anyInt()))
                .thenReturn(0)

        val appRater = AppRater.Builder(mockContext).build()
        appRater.incrementUseCount()

        verify(mockSharedPreferences).getInt(eq("tracking_version"), anyInt())
        verify(mockEditor).putInt("tracking_version", trackingVersion)
        verify(mockEditor).putLong(eq("first_use"), anyLong())
        verify(mockSharedPreferences, times(2)).getInt(eq("use_count"), anyInt())
        verify(mockEditor).putInt("use_count", 1)
    }

    @Test
    fun `increment use count for existing version`() {

        val trackingVersion = 1

        val packageInfo = PackageInfo()
        packageInfo.versionCode = trackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
                .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getInt(eq("tracking_version"), anyInt()))
                .thenReturn(trackingVersion)

        val appRater = AppRater.Builder(mockContext).build()
        appRater.incrementUseCount()

        verify(mockSharedPreferences).getInt(eq("tracking_version"), anyInt())
        verify(mockEditor, times(0)).putInt("tracking_version", trackingVersion)
        verify(mockEditor).putLong(eq("first_use"), anyLong())
        verify(mockSharedPreferences, times(2)).getInt(eq("use_count"), anyInt())
        verify(mockEditor).putInt("use_count", 1)
    }

    @Test
    fun `reset for new version`() {

        val oldTrackingVersion = 1
        val newTrackingVersion = oldTrackingVersion + 1

        val packageInfo = PackageInfo()
        packageInfo.versionCode = newTrackingVersion

        `when`(mockPackageManager.getPackageInfo("com.cmgapps", 0))
                .thenReturn(packageInfo)

        `when`(mockSharedPreferences.getInt(eq("tracking_version"), anyInt()))
                .thenReturn(oldTrackingVersion)

        val appRater = AppRater.Builder(mockContext).build()
        appRater.incrementUseCount()

        verify(mockSharedPreferences).getInt(eq("tracking_version"), anyInt())
        verify(mockEditor).putInt("tracking_version", newTrackingVersion)
        verify(mockEditor).putLong(eq("first_use"), anyLong())
        verify(mockEditor).putInt("use_count", 1)
        verify(mockEditor).putBoolean("declined_rate", false)
        verify(mockEditor).putLong("remind_later_date", 0L)
        verify(mockEditor).putBoolean("rated", false)
    }
}
