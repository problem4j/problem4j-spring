import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("internal.common-convention")
    id("java-library")
}

java {
    toolchain.languageVersion = providers.gradleProperty("internal.java.version").map { JavaLanguageVersion.of(it) }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Build-Jdk-Spec"] = java.toolchain.languageVersion.get().toString()
        attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
    }
    from("${rootProject.rootDir}/LICENSE") {
        into("META-INF/")
        rename { "LICENSE.txt" }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        exceptionFormat = TestExceptionFormat.SHORT
        showStandardStreams = true
    }

    // For resolving warnings from mockito.
    jvmArgs("-XX:+EnableDynamicAgentLoading")

    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
}
