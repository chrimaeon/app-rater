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
import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.dokka.gradle.DokkaAndroidTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import java.util.Properties

plugins {
    id("com.android.library")
    kotlin("android")
    id("digital.wup.android-maven-publish") version "3.6.2"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("com.jfrog.bintray") version "1.8.4"
    id("org.jetbrains.dokka-android") version "0.9.18"
}

android {
    compileSdkVersion(Deps.Versions.COMPILE_SDK_VERSION)
    buildToolsVersion(Deps.Versions.BUILD_TOOLS_VERSION)

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(Deps.Versions.TARGET_SDK_VERSION)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val pomName: String by project
val versionName: String by project

tasks.named<DokkaAndroidTask>("dokka") {
    moduleName = "app-rater"
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

tasks.register<Jar>("androidJavadocsJar") {
    archiveClassifier.set("javadoc")
    from(tasks["dokka"])
}

tasks.register<Jar>("androidSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

version = versionName

val group: String by project
project.group = group

val pomArtifactId: String by project
val pomDesc: String by project

publishing {
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

bintray {
    val credentialProps = Properties()
    credentialProps.load(file("${project.rootDir}/credentials.properties").inputStream())
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

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    revision = "release"
    resolutionStrategy {
        componentSelection {
            all {
                val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea").any { qualifier ->
                    candidate.version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                }
                if (rejected) {
                    reject("Release candidate")
                }
            }
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:${Deps.Versions.APP_COMPAT}")
    implementation("androidx.core:core-ktx:${Deps.Versions.CORE_KTX}")
    implementation(kotlin("stdlib-jdk7", Deps.Versions.KOTLIN))

    testImplementation("junit:junit:${Deps.Versions.JUNIT}")
    testImplementation("androidx.test:core:${Deps.Versions.TEST_CORE}")
    testImplementation("org.mockito:mockito-core:${Deps.Versions.MOCKITO_CORE}")
    testImplementation("org.hamcrest:hamcrest:${Deps.Versions.HAMCREST}")

    androidTestImplementation("androidx.test:runner:${Deps.Versions.TEST_RUNNER}")
    androidTestImplementation("androidx.test.ext:junit-ktx:${Deps.Versions.JUNIT_KTX}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Deps.Versions.ESPRESSO_CORE}")
}
