import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(Deps.Versions.COMPILE_SDK_VERSION)
    buildToolsVersion(Deps.Versions.BUILD_TOOLS_VERSION)

    defaultConfig {
        applicationId = "com.cmgapps.android.appratersample"
        minSdkVersion(19)
        targetSdkVersion(Deps.Versions.TARGET_SDK_VERSION)
        versionCode = 1
        versionName = "1.0"
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
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":library"))
    implementation(project(":library-ktx"))
    implementation("androidx.appcompat:appcompat:${Deps.Versions.APP_COMPAT}")
    implementation("androidx.core:core-ktx:${Deps.Versions.CORE_KTX}")
    implementation("androidx.lifecycle:lifecycle-extensions:${Deps.Versions.LIFECYCLE_EXT}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Deps.Versions.LIFECYCLE_COMMON}")
    implementation(kotlin("stdlib-jdk7", Deps.Versions.KOTLIN))
}
