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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cmgapps.android.apprater.testing.TestActivity
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppRaterUiShould {

    @Rule
    @JvmField
    var activityScenarioRule = activityScenarioRule<TestActivity>()

    @Test
    fun showDialog() {
        activityScenarioRule.scenario.onActivity {
            AppRater.Builder(it).build().show(it)
        }

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
