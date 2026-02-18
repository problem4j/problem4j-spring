plugins {
    id("internal.java-convention")
    id("java-library")
}

java {
    withSourcesJar()
    withJavadocJar()
}

// buildSrc/src/main/kotlin/internal.common-convention.gradle.kts - "printVersion" task definition
tasks.withType<PublishToMavenLocal>().configureEach {
    finalizedBy("printVersion")
}
