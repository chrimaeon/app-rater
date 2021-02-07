/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.gradle.api.Project
import java.io.File
import java.util.Locale
import java.util.Properties
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class CredentialsDelegate(propertiesFile: File) : ReadOnlyProperty<Any?, String?> {

    private val properties: Properties? = if (propertiesFile.exists()) {
        Properties().apply {
            load(propertiesFile.inputStream())
        }
    } else {
        null
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {

        if (properties != null) {
            return properties.getProperty(property.name)
        }

        return System.getenv("SONATYPE_${property.name.toUpperCase(Locale.ROOT)}")
    }
}

fun Project.credentials() = CredentialsDelegate(rootProject.file("credentials.properties"))
