/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.gradle

import Deps
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

fun Project.configureKtlint() {
    val ktlint = configurations.create("ktlint")

    tasks {

        val inputFiles = fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))
        val outputDir = "${buildDir}/reports"

        register<JavaExec>("ktlintFormat") {
            inputs.files(inputFiles)
            outputs.dir(outputDir)

            group = "Formatting"
            description = "Fix Kotlin code style deviations."
            mainClass.set("com.pinterest.ktlint.Main")
            classpath = ktlint
            args = listOf("-F", "src/**/*.kt")
        }

        val ktlintTask = register<JavaExec>("ktlint") {
            inputs.files(inputFiles)
            outputs.dir(outputDir)

            group = "Verification"
            description = "Check Kotlin code style."
            mainClass.set("com.pinterest.ktlint.Main")
            classpath = ktlint
            args = listOf(
                "src/**/*.kt",
                "--reporter=plain",
                "--reporter=html,output=${outputDir}/ktlint.html"
            )
        }

        named("check") {
            dependsOn(ktlintTask)
        }
    }

    dependencies {
        ktlint("com.pinterest:ktlint:" + Deps.Versions.KTLINT)
    }
}
