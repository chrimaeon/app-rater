/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import isCI
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.Project
import java.io.IOException

fun Project.sendCreateReleaseRequest(version: String, body: String): Response? {
    val request =
        Request.Builder().url("https://api.github.com/repos/chrimaeon/app-rater/releases")
            .addHeader("Accept", "application/vnd.github.v3+json")
            .addHeader(
                "Authorization", "token " + rootProject.file("GITHUB_TOKEN").readText().trim()
            )
            .post(
                Json.encodeToString(
                    GithubReleaseNotes(
                        tagName = version,
                        name = version,
                        draft = !isCI(),
                        body = body
                    )
                ).toRequestBody("application/json".toMediaType())
            )
            .build()

    val loggingInterceptor: HttpLoggingInterceptor =
        HttpLoggingInterceptor { message -> logger.info(message) }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected response: $response")
            Json { ignoreUnknownKeys = true }.decodeFromString(
                response.body?.string() ?: return@use null
            )
        }
}

@Serializable
data class GithubReleaseNotes(
    @SerialName("tag_name")
    val tagName: String,
    val name: String,
    val draft: Boolean,
    val body: String
)

@Serializable
data class Response(@SerialName("html_url") val htmlUrl: String)
