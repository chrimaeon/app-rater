/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.gradle.plugin.use.PluginDependenciesSpec

fun PluginDependenciesSpec.ktlint() = id("com.cmgapps.gradle.ktlint")
fun PluginDependenciesSpec.bintrayPublish() = id("com.cmgapps.gradle.bintray-publish")
