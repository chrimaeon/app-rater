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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.cast

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka") version "0.10.1"
    ktlint()
    bintrayPublish()
}

android {
    compileSdkVersion(Deps.Versions.COMPILE_SDK_VERSION)
    buildToolsVersion(Deps.Versions.BUILD_TOOLS_VERSION)

    defaultConfig {
        minSdkVersion(Deps.Versions.MIN_SDK_VERSION)
    }

    testOptions {
        unitTests.all(closureOf<Test> {
            testLogging {
                events(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            }
        }.cast())
    }
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks {
    dokka {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"

        configuration {
            moduleName = "app-rater"
        }
    }

    register<Jar>("androidJavadocsJar") {
        archiveClassifier.set("javadoc")
        from(dokka)
    }

    register<Jar>("androidSourcesJar") {
        archiveClassifier.set("sources")
        from(android.sourceSets["main"].java.srcDirs)
    }
}

dependencies {
    api(project(":library"))
    implementation(kotlin("stdlib-jdk7", Deps.Versions.KOTLIN))
}