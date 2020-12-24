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
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.json.simple.JSONObject

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath(kotlin("gradle-plugin", version = Deps.Versions.KOTLIN))
        classpath("com.squareup.okhttp3:okhttp:4.9.0")
        classpath("com.squareup.okhttp3:logging-interceptor:4.9.0")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.36.0"
    id("org.jetbrains.changelog") version "0.6.2"
}

allprojects {
    repositories {
        jcenter()
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
        gradleVersion = "6.7.1"
    }

    register("createGithubRelease") {
        val versionName: String by project
        if (!changelog.has(versionName)) {
            dependsOn(patchChangelog)
        }
        doLast {
            val changelog = changelog.get(versionName).toText()
            sendCreateReleaseRequest(
                versionName,
                changelog
            )?.let {
                logger.lifecycle(it.htmlUrl)
            }
        }
    }
}

changelog {
    val versionName: String by project
    version = versionName
}

fun sendCreateReleaseRequest(version: String, body: String): Response? {
    val request =
        Request.Builder().url("https://api.github.com/repos/chrimaeon/app-rater/releases")
            .addHeader("Accept", "application/vnd.github.v3+json")
            .addHeader(
                "Authorization", "token " + rootProject.file("GITHUB_TOKEN").readText().trim()
            )
            .post(
                JSONObject.toJSONString(
                    mapOf(
                        "tag_name" to version,
                        "name" to version,
                        "draft" to true,
                        "body" to body
                    )
                ).toRequestBody("application/json".toMediaType())
            )
            .build()

    @Suppress("ObjectLiteralToLambda")
    val loggingInterceptor: HttpLoggingInterceptor =
        HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                logger.info(message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("Unexpected response: $response")

            Gson().fromJson(response.body?.string(), Response::class.java)
        }
}

data class Response(@SerializedName("html_url") val htmlUrl: String)


