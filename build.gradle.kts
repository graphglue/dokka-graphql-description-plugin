import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

description =
    "A Dokka plugin for using the content of the GraphQLDescription annotation as documentation of no other exists"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.nmcp)
    alias(libs.plugins.nmcpAggregation)
    `maven-publish`
    signing
}

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.dokka.core)
    implementation(libs.dokka.base)
}

dokka {
    dokkaSourceSets.main {
        documentedVisibilities.set(VisibilityModifier.entries.asIterable())
    }
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("dokka-graphql-description-plugin")
                description.set(provider { project.description })
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
            }
        }
    }
}

signing {
    setRequired { isReleaseVersion }
    // In-memory keys keep CI from having to materialise a keyring file.
    val signingKey = providers.environmentVariable("SIGNING_KEY")
        .orElse(providers.gradleProperty("signingKey"))
    val signingPassword = providers.environmentVariable("SIGNING_PASSWORD")
        .orElse(providers.gradleProperty("signingPassword"))
    if (signingKey.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPassword.orNull)
    }
    sign(publishing.publications["mavenJava"])
}

nmcpAggregation {
    centralPortal {
        username = providers.gradleProperty("mavenCentralUsername")
            .orElse(providers.environmentVariable("MAVEN_CENTRAL_USERNAME"))
        password = providers.gradleProperty("mavenCentralPassword")
            .orElse(providers.environmentVariable("MAVEN_CENTRAL_PASSWORD"))
        // Upload the deployment, then release it manually from the portal.
        publishingType = "USER_MANAGED"
        publicationName = "dokka-graphql-description-plugin:$version"
    }
}

dependencies {
    nmcpAggregation(project(path))
}
