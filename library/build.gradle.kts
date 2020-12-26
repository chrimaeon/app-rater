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

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka")
    bintrayPublish
    ktlint
}

android {
    compileSdkVersion(Deps.Versions.COMPILE_SDK_VERSION)
    buildToolsVersion(Deps.Versions.BUILD_TOOLS_VERSION)

    defaultConfig {
        minSdkVersion(Deps.Versions.MIN_SDK_VERSION)
        targetSdkVersion(Deps.Versions.TARGET_SDK_VERSION)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = false
        renderScript = false
        aidl = false
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }

    testOptions {
        unitTests.all { test ->
            test.testLogging {
                events(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            }
        }
    }
}

tasks {
    withType(KotlinCompile::class).all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<DokkaTask> {
        moduleName.set("app-rater")
        dokkaSourceSets {
            named("main") {
                noAndroidSdkLink.set(false)
            }
            named("debug") {
                suppress.set(true)
            }
        }
    }

    register<Jar>("sourcesJar") {
        from(android.sourceSets["main"].java.srcDirs)
        archiveClassifier.set("sources")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:" + Deps.Versions.APP_COMPAT)
    implementation("androidx.core:core-ktx:" + Deps.Versions.CORE_KTX)
    implementation(kotlin("stdlib-jdk7", Deps.Versions.KOTLIN))

    testImplementation("junit:junit:${Deps.Versions.JUNIT}")
    testImplementation("androidx.test:core:${Deps.Versions.TEST_CORE}")
    testImplementation("org.mockito:mockito-core:${Deps.Versions.MOCKITO_CORE}")
    testImplementation("org.hamcrest:hamcrest:${Deps.Versions.HAMCREST}")

    androidTestImplementation("androidx.test:runner:${Deps.Versions.TEST_RUNNER}")
    androidTestImplementation("androidx.test.ext:junit-ktx:${Deps.Versions.JUNIT_KTX}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Deps.Versions.ESPRESSO_CORE}")
}
