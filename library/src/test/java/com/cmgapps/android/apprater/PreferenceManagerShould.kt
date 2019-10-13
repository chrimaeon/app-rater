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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PreferenceManagerShould {

    @Mock
    lateinit var mockContext: Context

    @Mock
    lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var preferenceManager: PreferenceManager

    @Before
    fun setup() {
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPreferences)

        `when`(mockSharedPreferences.edit())
            .thenReturn(mockEditor)

        preferenceManager = PreferenceManager(mockContext)
    }

    @Test
    fun `get declined to rate with default value`() {
        `when`(mockSharedPreferences.getBoolean(anyString(), anyBoolean()))
            .thenAnswer { it.getArgument(1) }
        assertThat(preferenceManager.declinedToRate, `is`(false))
    }

    @Test
    fun `get declined to rate correctly`() {
        `when`(mockSharedPreferences.getBoolean(eq("declined_rate"), anyBoolean()))
            .thenReturn(true)
        assertThat(preferenceManager.declinedToRate, `is`(true))
    }

    @Test
    fun `set declined to rate calls preference`() {
        preferenceManager.declinedToRate = true

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putBoolean("declined_rate", true)
        verify(mockEditor).apply()
    }

    @Test
    fun `get app rated with default value`() {
        `when`(mockSharedPreferences.getBoolean(anyString(), anyBoolean()))
            .thenAnswer { it.getArgument(1) }
        assertThat(preferenceManager.appRated, `is`(false))
    }

    @Test
    fun `get app rated correctly`() {
        `when`(mockSharedPreferences.getBoolean(eq("rated"), anyBoolean()))
            .thenReturn(true)
        assertThat(preferenceManager.appRated, `is`(true))
    }

    @Test
    fun `set app rated calls preference`() {
        preferenceManager.appRated = true

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putBoolean("rated", true)
        verify(mockEditor).apply()
    }

    @Test
    fun `get first time used with default value`() {
        `when`(mockSharedPreferences.getLong(anyString(), anyLong()))
            .thenAnswer { it.getArgument(1) }
        assertThat(preferenceManager.firstUsedTimestamp, `is`(0L))
    }

    @Test
    fun `get first time used correctly`() {
        `when`(mockSharedPreferences.getLong(eq("first_use"), anyLong()))
            .thenReturn(123456L)
        assertThat(preferenceManager.firstUsedTimestamp, `is`(123456L))
    }

    @Test
    fun `set first time used calls preference`() {
        preferenceManager.firstUsedTimestamp = 987654L

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putLong("first_use", 987654L)
        verify(mockEditor).apply()
    }

    @Test
    fun `get use count with default value`() {
        `when`(mockSharedPreferences.getInt(anyString(), anyInt()))
            .thenAnswer { it.getArgument(1) }
        assertThat(preferenceManager.useCount, `is`(0))
    }

    @Test
    fun `get use count correctly`() {
        `when`(mockSharedPreferences.getInt(eq("use_count"), anyInt()))
            .thenReturn(4567)
        assertThat(preferenceManager.useCount, `is`(4567))
    }

    @Test
    fun `get remind later timestamp with default value`() {
        `when`(mockSharedPreferences.getLong(anyString(), anyLong()))
            .thenAnswer { it.getArgument(1) }
        assertThat(preferenceManager.remindLaterTimeStamp, `is`(0L))
    }

    @Test
    fun `get remind later timestamp correctly`() {
        `when`(mockSharedPreferences.getLong(eq("remind_later_date"), anyLong()))
            .thenReturn(56L)
        assertThat(preferenceManager.remindLaterTimeStamp, `is`(56L))
    }

    @Test
    fun `set remind later timstamp calls preference`() {
        preferenceManager.remindLaterTimeStamp = 654L

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putLong("remind_later_date", 654L)
        verify(mockEditor).apply()
    }

    @Test
    fun `get tracking version with default value`() {
        `when`(mockSharedPreferences.getLong(anyString(), anyLong()))
            .thenAnswer { it.getArgument(1) }
        assertThat(preferenceManager.trackingVersion, `is`(-1L))
    }

    @Test
    fun `get tracking version correctly`() {
        `when`(mockSharedPreferences.getLong(eq("tracking_version_long"), anyLong()))
            .thenReturn(12)
        assertThat(preferenceManager.trackingVersion, `is`(12L))
    }

    @Test
    fun `set tracking version calls preference`() {
        preferenceManager.trackingVersion = 75

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putLong("tracking_version_long", 75)
        verify(mockEditor).apply()
    }

    @Test
    fun `increment use count sets correct preference`() {
        `when`(mockSharedPreferences.getInt(eq("use_count"), anyInt()))
            .thenReturn(3)
        preferenceManager.incrementUseCount()
        verify(mockSharedPreferences).edit()
        verify(mockEditor).putInt("use_count", 4)
        verify(mockEditor).apply()
    }

    @Test
    fun `reset preference on new version`() {
        preferenceManager.resetNewVersion(56)
        verify(mockSharedPreferences).edit()
        verify(mockEditor).putLong("tracking_version_long", 56)
        verify(mockEditor).putLong(eq("first_use"), anyLong())
        verify(mockEditor).putInt("use_count", 1)
        verify(mockEditor).putBoolean("declined_rate", false)
        verify(mockEditor).putLong("remind_later_date", 0L)
        verify(mockEditor).putBoolean("rated", false)
        verify(mockEditor).apply()
        verifyNoMoreInteractions(mockEditor)
    }
}
