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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:" + Deps.Versions.ANDROID_PLUGIN)
        classpath(kotlin("gradle-plugin", version = Deps.Versions.KOTLIN))
    }
}

plugins {
    id("com.github.ben-manes.versions") version Deps.Versions.VERSIONS_PLUGIN
    id("org.jetbrains.changelog") version Deps.Versions.CHANGELOG_PLUGIN
    id("org.jetbrains.dokka") version Deps.Versions.DOKKA_PLUGIN apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks {
    withType<DependencyUpdatesTask> {
        revision = "release"
        rejectVersionIf {
            listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea", "dev").any { qualifier ->
                candidate.version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
            }
        }
    }

    register<Delete>("clean") {
        delete(rootProject.buildDir)
    }

    named<Wrapper>("wrapper") {
        distributionType = DistributionType.ALL
        gradleVersion = Deps.Versions.GRADLE
    }
}

changelog {
    val versionName: String by project
    version.set(versionName)
}
