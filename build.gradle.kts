import org.jetbrains.dokka.gradle.DokkaTask

val kotlinVersion: String by project
val dokkaVersion: String by project

description = "A Dokka plugin for using the content of the GraphQLDescription annotation as documentation of no other exists"

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("maven-publish")
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.jetbrains.dokka", "dokka-core", dokkaVersion)
    implementation("org.jetbrains.dokka", "dokka-base", dokkaVersion)
}

extra["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT")

tasks {
    fun configureDokka(builder: Action<org.jetbrains.dokka.gradle.GradleDokkaSourceSetBuilder>) {
        val dokkaJavadoc by getting(DokkaTask::class) {
            dokkaSourceSets {
                configureEach(builder)
            }
        }
    }

    configureDokka {
        includeNonPublic.set(true)
    }

    val jarComponent = project.components.getByName("java")
    val sourcesJar by registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
    val dokka = named("dokkaJavadoc", DokkaTask::class)
    val javadocJar by registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from("${layout.buildDirectory}/dokka/javadoc")
        dependsOn(dokka)
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                pom {
                    name.set("dokka-graphql-description-plugin")
                    description.set(project.description)
                    url.set("https://github.com/graphglue/dokka-graphql-description-plugin")

                    organization {
                        name.set("Software Quality and Architecture - University of Stuttgart")
                        url.set("https://www.iste.uni-stuttgart.de/sqa/")
                    }

                    developers {
                        developer {
                            name.set("Niklas Krieger")
                            email.set("niklas.krieger@iste.uni-stuttgart.de")
                            organization.set("Software Quality and Architecture - University of Stuttgart")
                            organizationUrl.set("https://www.iste.uni-stuttgart.de/sqa/")
                        }
                        developer {
                            name.set("Georg Reißner")
                            email.set("georg.reissner@iste.uni-stuttgart.de")
                            organization.set("Software Quality and Architecture - University of Stuttgart")
                            organizationUrl.set("https://www.iste.uni-stuttgart.de/sqa/")
                        }
                        developer {
                            name.set("Christian Kurz")
                            email.set("chrikuvellberg@gmail.com")
                            organization.set("Software Quality and Architecture - University of Stuttgart")
                            organizationUrl.set("https://www.iste.uni-stuttgart.de/sqa/")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/graphglue/dokka-graphql-description-plugin.git")
                        developerConnection.set("scm:git:https://github.com/graphglue/dokka-graphql-description-plugin.git")
                        url.set("https://github.com/graphglue/dokka-graphql-description-plugin/tree/main")
                    }

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    val mavenPom = this
                    afterEvaluate {
                        mavenPom.description.set(description)
                    }
                }

                from(jarComponent)
                artifact(sourcesJar.get())
                artifact(javadocJar.get())
            }
        }
    }

    signing {
        setRequired({
            (rootProject.extra["isReleaseVersion"] as Boolean)
        })
        sign(publishing.publications["mavenJava"])
    }

    nexusPublishing {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            }
        }
    }
}
