package com.cmgapps.android.apprater

import android.content.Context
import android.text.format.DateUtils
import com.cmgapps.android.apprater.store.Store
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppRaterBuilderShould {

    @Mock
    lateinit var mockContext: Context

    lateinit var builder: AppRater.Builder

    @Before
    fun setup() {
        builder = AppRater.Builder(mockContext)
    }

    @Test
    fun `set store`() {
        val store = mock(Store::class.java)
        builder.setStore(store)

        assertThat(builder.mStore, `is`(store))
    }

    @Test
    fun `set launches until prompt`() {
        builder.setLaunchesUntilPrompt(200)
        assertThat(builder.mLaunchesUntilPrompt, `is`(200))
    }

    @Test
    fun `set days until prompt`() {
        builder.setDaysUntilPrompt(50)
        assertThat(builder.mDaysUntilPrompt, `is`(50 * DateUtils.DAY_IN_MILLIS))
    }

    @Test
    fun `set days until remind again`() {
        builder.setDaysUntilRemindAgain(20)
        assertThat(builder.mDaysUntilRemindAgain, `is`(20 * DateUtils.DAY_IN_MILLIS))
    }


    @Test
    fun `set debug mode`() {
        builder.setDebug(true)
        assertThat(builder.mDebug, `is`(true))
    }
}
