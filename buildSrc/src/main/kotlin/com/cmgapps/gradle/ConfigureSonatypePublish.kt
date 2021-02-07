/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import credentials
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

data class PomValues(
    val name: String,
    val desc: String,
    val version: String,
    val artivactId: String,
    val url: String,
    val scmConnection: String,
    val scmDevConnection: String,
    val scmUrl: String,
    val issuesTrackerUrl: String
)

fun Project.configureSonatypePublish(values: PomValues) {

    tasks.register<Jar>("dokkaJavadocJar") {
        val dokkaJavadoc = tasks.named("dokkaJavadoc")
        dependsOn(dokkaJavadoc)
        from(dokkaJavadoc)
        archiveClassifier.set("javadoc")
    }

    tasks.register<Jar>("dokkaHtmlJar") {
        val dokkaHtml = tasks.named("dokkaHtml")
        dependsOn(dokkaHtml)
        from(dokkaHtml)
        archiveClassifier.set("html-doc")
    }

    apply<MavenPublishPlugin>()
    apply<SigningPlugin>()

    val pubName = "libraryMaven"
    configure<PublishingExtension> {
        publications {
            register<MavenPublication>(pubName) {

                from(components["release"])
                artifact(tasks["dokkaHtmlJar"])
                artifact(tasks["dokkaJavadocJar"])
                artifact(tasks["sourcesJar"])

                artifactId = values.artivactId

                pom {
                    name.set(values.name)
                    description.set(values.desc)
                    url.set(values.url)

                    developers {
                        developer {
                            id.set("chrimaeon")
                            name.set("Christian Grach")
                            email.set("christian.grach@cmgapps.com")
                        }
                    }

                    scm {
                        connection.set(values.scmConnection)
                        developerConnection.set(values.scmDevConnection)
                        url.set(values.scmUrl)
                    }

                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    issueManagement {
                        system.set("github")
                        url.set(values.issuesTrackerUrl)
                    }
                }
            }
        }

        repositories {
            maven {
                name = "sonatype"
                val releaseUrl =
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (values.version.endsWith("SNAPSHOT")) snapshotUrl else releaseUrl

                val username by credentials()
                val password by credentials()

                credentials {
                    this.username = username
                    this.password = password
                }
            }
        }
    }

    configure<SigningExtension> {
        sign(project.extensions.getByType(PublishingExtension::class.java).publications[pubName])
    }
}
