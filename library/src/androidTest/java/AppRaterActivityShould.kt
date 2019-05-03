package com.cmgapps.android.apprater

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppRaterActivityShould {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<AppRaterActivity>()

    @Test
    fun showDialog() {
        onView(withText("Rate now!"))
                .check(matches(isDisplayed()))
        onView(withText(containsString("If you enjoy using")))
        onView(withText("Rate now"))
                .check(matches(isDisplayed()))
        onView(withText("Remind me later"))
                .check(matches(isDisplayed()))
        onView(withText("No, thanks"))
                .check(matches(isDisplayed()))
    }
}