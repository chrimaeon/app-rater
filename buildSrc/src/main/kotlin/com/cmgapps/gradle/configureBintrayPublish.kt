/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import credentials
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import java.util.Date

fun Project.configureBintrayPublish(pomName: String, pomDesc: String, pomArtifactId: String) {
    apply<MavenPublishPlugin>()

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("pluginMaven") {

                from(components["release"])
                artifact(tasks["androidSourcesJar"])
                artifact(tasks["androidJavadocsJar"])

                artifactId = pomArtifactId

                pom {
                    name.set(pomName)
                    description.set(pomDesc)
                    developers {
                        developer {
                            id.set("cgrach")
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
                }
            }
        }
    }

    apply<BintrayPlugin>()

    configure<BintrayExtension> {
        val user by project.credentials()
        val key by project.credentials()

        this.user = user
        this.key = key

        setPublications("pluginMaven")

        pkg(closureOf<BintrayExtension.PackageConfig> {

            val projectUrl: String by project
            val githubRepo: String by project
            repo = "maven"
            name = "${project.group}:$pomArtifactId"
            userOrg = user
            setLicenses("Apache-2.0")
            vcsUrl = projectUrl
            val issuesTrackerUrl: String by project
            issueTrackerUrl = issuesTrackerUrl
            this.githubRepo = githubRepo
            version(closureOf<BintrayExtension.VersionConfig> {
                name = project.version as String
                vcsTag = project.version as String
                released = Date().toString()
            })
        })
    }
}
