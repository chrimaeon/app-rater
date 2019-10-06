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

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import digital.wup.android_maven_publish.AndroidMavenPublishPlugin
import java.util.Date
import java.util.Properties

val pomName: String by project
val versionName: String by project
project.version = versionName

val group: String by project
project.group = group

val pomArtifactId: String by project
val pomDesc: String by project

fun Project.bintrayPublishConvention() {
    apply<AndroidMavenPublishPlugin>()

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("pluginMaven") {

                from(components["android"])
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
        val credentialProps = Properties()
        credentialProps.load(rootProject.file("credentials.properties").inputStream())
        user = credentialProps.getProperty("user")
        key = credentialProps.getProperty("key")
        setPublications("pluginMaven")

        dryRun = true

        pkg(closureOf<BintrayExtension.PackageConfig> {

            val projectUrl: String by project
            repo = "maven"
            name = "${project.group}:$pomArtifactId"
            userOrg = user
            setLicenses("Apache-2.0")
            vcsUrl = projectUrl
            val issuesTrackerUrl: String by project
            issueTrackerUrl = issuesTrackerUrl
            githubRepo = projectUrl
            version(closureOf<BintrayExtension.VersionConfig> {
                name = versionName
                vcsTag = versionName
                released = Date().toString()
            })
        })

    }
}

bintrayPublishConvention()


