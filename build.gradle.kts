import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

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

buildscript {
    repositories {
        jcenter()
        google()
        mavenCentral()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.1")
        classpath(kotlin("gradle-plugin", version = Deps.Versions.KOTLIN))
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.25.0"
}

allprojects {
    repositories {
        jcenter()
        google()
        mavenCentral()
    }
}


subprojects {
    val ktlint by configurations.creating

    tasks {
        val ktlint by registering(JavaExec::class) {
            group = "Verification"
            description = "Check Kotlin code style."
            main = "com.pinterest.ktlint.Main"
            classpath = ktlint
            args = listOf(
                "src/**/*.kt",
                "--reporter=plain",
                "--reporter=checkstyle,output=${buildDir}/reports/ktlint.xml"
            )

        }

        afterEvaluate {
            named("check") {
                dependsOn(ktlint)
            }
        }
    }

    dependencies {
        ktlint("com.pinterest:ktlint:0.34.2")
    }

}

tasks {
    withType<DependencyUpdatesTask> {
        revision = "release"
        rejectVersionIf {
            listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea").any { qualifier ->
                candidate.version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
            }
        }
    }

    register<Delete>("clean") {
        delete(rootProject.buildDir)
    }
}


