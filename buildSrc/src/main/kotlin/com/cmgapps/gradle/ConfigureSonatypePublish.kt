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

fun Project.configureSonatypePublish(pomName: String, pomDesc: String, pomArtifactId: String) {

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

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("libraryMaven") {

                from(components["release"])
                artifact(tasks["dokkaHtmlJar"])
                artifact(tasks["dokkaJavadocJar"])
                artifact(tasks["sourcesJar"])

                artifactId = pomArtifactId

                pom {
                    name.set(pomName)
                    description.set(pomDesc)
                    developers {
                        developer {
                            id.set("chrimaeon")
                            name.set("Christian Grach")
                        }
                    }
                    scm {
                        val pomScmConnection: String by project
                        connection.set(pomScmConnection)
                        val pomScmDevConnection: String by project
                        developerConnection.set(pomScmDevConnection)
                        val pomScmUrl: String by project
                        url.set(pomScmUrl)
                    }
                    licenses {
                        license {
                            val pomLicenseName: String by project
                            name.set(pomLicenseName)
                            val pomLicenseUrl: String by project
                            url.set(pomLicenseUrl)
                            distribution.set("repo")
                        }
                    }
                    issueManagement {
                        val issuesTrackerUrl: String by project
                        system.set("github")
                        url.set(issuesTrackerUrl)
                    }
                }
            }
        }

        repositories {
            maven {
                val versionName: String by project

                name = "sonatype"
                val releaseUrl =
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (versionName.endsWith("SNAPSHOT")) snapshotUrl else releaseUrl

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
        sign(project.extensions.getByType(PublishingExtension::class.java).publications["libraryMaven"])
    }
}
