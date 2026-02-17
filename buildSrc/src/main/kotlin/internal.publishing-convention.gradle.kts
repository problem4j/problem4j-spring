import internal.InternalPublishingExtension
import internal.findDevelopers
import internal.findLicenses
import internal.getBooleanProperty

plugins {
    id("internal.common-convention")
    id("maven-publish")
    id("signing")
}

val internalPublishing: InternalPublishingExtension = extensions.create(
    "internalPublishing",
    InternalPublishingExtension::class.java,
)

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            plugins.withType<JavaPlugin> {
                from(components["java"])
            }

            plugins.withType<JavaPlatformPlugin> {
                from(components["javaPlatform"])
            }

            pom {
                name = internalPublishing.displayName
                description = internalPublishing.description
                url = property("internal.pom.url") as String
                inceptionYear = property("internal.pom.inception-year") as String
                licenses {
                    project.findLicenses().forEach {
                        license {
                            name = it.name
                            url = it.url
                            distribution = it.distribution
                            comments = it.comments
                        }
                    }
                }
                developers {
                    project.findDevelopers().forEach {
                        developer {
                            id = it.id
                            name = it.name
                            url = it.url
                            email = it.email
                            organization = it.organization
                            organizationUrl = it.organizationUrl
                        }
                    }
                }
                scm {
                    connection = property("internal.pom.scm.connection") as String
                    developerConnection = property("internal.pom.scm.developer-connection") as String
                    url = property("internal.pom.scm.url") as String
                }
                issueManagement {
                    system = property("internal.pom.issue-management.system") as String
                    url = property("internal.pom.issue-management.url") as String
                }
            }
        }
    }
}

signing {
    if (project.getBooleanProperty("sign")) {
        useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
        sign(publishing.publications["maven"])
    }
}
