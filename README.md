# Android&trade; App Rater Dialog

[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg?style=for-the-badge&logo=apache)](http://www.apache.org/licenses/LICENSE-2.0)
[![Bintray](https://www.cmgapps.com/badge/chrimaeon/maven/com.cmgapps.android:app-rater/badge.svg)](https://jcenter.bintray.com/com/cmgapps/android/cmgUtilities/)

This is a App Rater Dialog to encourage user to rate the app on the Google Play Store&trade;

It is used in my Android&trade; Projects:

* [Bierdeckel][1]
* [Numerals Converter][4]
* [PhoNews][2]
* [PhoNews Pro][3]
* [Running Sushi][5]

## Usage

Add the following dependency to your `build.gradle`.

```groovy
dependencies {
    implementation 'com.cmgapps.android:app-rater:<version>'
}
```

Extend the `Application` class and register a `LifecycleObserver`

```kotlin
import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.cmgapps.android.apprater.AppRater

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val appRater = AppRater.Builder(this).build()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(apprater))

    }
}

class AppLifecycleListener(private val appRater: AppRater) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        appRater.incrementUseCount()
    }
}
```
and in your main `Activity#onCreate` check if requirements for rating are met and show the dialog

```koltin
override fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val appRater = AppRater.Builder(this).build()

    if (mAppRater.checkForRating()) {
        mAppRater.show(this)
    }
}
``` 

## License

```text
Copyright 2016-2019 Christian Grach
    
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
*Android is a trademark of Google Inc.*

 [1]: https://play.google.com/store/apps/details?id=com.cmgapps.android.bierdeckel&referrer=utm_source%3Dgithub%26utm_medium%3DREADME
 [2]: https://play.google.com/store/apps/details?id=at.cmg.android.phonews&referrer=utm_source%3Dgithub%26utm_medium%3DREADME
 [3]: https://play.google.com/store/apps/details?id=com.cmgapps.android.phonewspro&referrer=utm_source%3Dgithub%26utm_medium%3DREADME
 [4]: https://play.google.com/store/apps/details?id=com.cmgapps.android.numeralsconverter&referrer=utm_source%3Dgithub%26utm_medium%3DREADME
 [5]: https://play.google.com/store/apps/details?id=com.cmgapps.android.sushicounter&referrer=utm_source%3Dgithub%26utm_medium%3DREADME
