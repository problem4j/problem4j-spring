import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java-library")
}

// The project is built using a JDK 25 toolchain, but the main sources are compiled with --release 17.
//
// This means:
// - Gradle can run on any JDK 17+,
// - javac from JDK 25 is used,
// - the produced bytecode and available Java API for main sources are restricted to Java 17.
//
// This setup lets us use a modern JDK for tooling (ErrorProne, NullAway) while keeping Java 17 binary compatibility for
// library consumers.
//
// Tests are NOT compiled with --release XYZ, so they may use newer Java APIs.

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
}

tasks.named<JavaCompile>("compileJava").configure {
    options.release = 17
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Build-Jdk-Spec"] = java.toolchain.languageVersion.get().toString()
        attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
        attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
    }
    from("${rootProject.rootDir}/LICENSE") {
        into("META-INF/")
        rename { "LICENSE.txt" }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.SHORT
        showStandardStreams = true
    }

    // For resolving warnings from mockito.
    jvmArgs("-XX:+EnableDynamicAgentLoading")

    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
}

tasks.withType<Javadoc>().configureEach {
    javadocTool = javaToolchains.javadocToolFor { languageVersion = JavaLanguageVersion.of(17) }
}

// Usage:
//   ./gradlew printVersion
tasks.register<DefaultTask>("printVersion") {
    description = "Prints the current project version to the console."
    group = "help"

    val projectName = project.name
    val projectVersion = project.version.toString()

    doLast {
        println("$projectName version: $projectVersion")
    }
}

tasks.withType<PublishToMavenLocal>().configureEach {
    finalizedBy("printVersion")
}
