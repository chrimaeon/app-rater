/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.gradle.plugin.use.PluginDependenciesSpec

inline val PluginDependenciesSpec.ktlint: org.gradle.plugin.use.PluginDependencySpec
    get() = id("com.cmgapps.gradle.ktlint")
inline val PluginDependenciesSpec.bintrayPublish: org.gradle.plugin.use.PluginDependencySpec
    get() = id("com.cmgapps.gradle.bintray-publish")

fun isCI(): Boolean = System.getenv("CI") != null
