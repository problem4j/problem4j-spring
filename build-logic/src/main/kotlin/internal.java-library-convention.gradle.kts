import internal.getIntProperty
import internal.isTestTask
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("net.ltgt.errorprone")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlinx.kover")
    id("io.github.malczuuu.nullmarked")
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
//
// The project also includes interop utilities between Java and Kotlin.

val javaToolchainVersion = project.getIntProperty("internal.java.toolchain-version")
val javaTargetVersion = project.getIntProperty("internal.java.target-version")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaToolchainVersion)
    }
    withSourcesJar()
    withJavadocJar()
}
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"

    options.errorprone {
        if (isTestTask()) {
            disableAllChecks = true
        }

        error("NullAway")
        option("NullAway:OnlyNullMarked", "true")
        option("NullAway:JSpecifyMode", "true")
    }
}
tasks.named<JavaCompile>("compileJava") {
    options.release = javaTargetVersion
    // spring-boot-configuration-processor merges META-INF/additional-spring-configuration-metadata.json
    // only if resources are processed before compilation
    inputs.files(tasks.named("processResources"))
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(javaToolchainVersion)
    }
    compilerOptions {
        explicitApi()
        moduleName = project.name
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
    }
}
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        javaParameters = true
    }
}
tasks.named<KotlinCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaTargetVersion.toString())
        freeCompilerArgs.add("-Xjdk-release=$javaTargetVersion")
    }
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Build-Jdk-Spec"] = javaTargetVersion
        attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
        attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
    }
    from("${rootProject.rootDir}/LICENSE") {
        into("META-INF/")
        rename { "$it.txt" }
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
    systemProperty("spring.test.context.cache.maxSize", 5)
}

tasks.named<Task>("check") {
    finalizedBy(tasks.named("koverHtmlReport"), tasks.named("koverXmlReport"))
}

tasks.withType<Javadoc>().configureEach {
    javadocTool =
        javaToolchains.javadocToolFor {
            languageVersion = JavaLanguageVersion.of(javaTargetVersion)
        }
}

// Assert that each package has a package-info.java with @NullMarked annotation on it.
nullmarked {
    verifyOnly = true
    verify {
        strict()
    }
}
