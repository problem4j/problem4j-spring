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
}

// The project is built using a JDK 25 toolchain, but all sources (main and test) are compiled with --release 17.
//
// This means:
// - Gradle can run on any JDK 17+,
// - javac from JDK 25 is used,
// - the produced bytecode and available Java API for main and test sources are restricted to Java 17.
//
// This setup lets us use a modern JDK for tooling (ErrorProne, NullAway) while keeping Java 17 binary compatibility for
// library consumers.
//
// Tests are run on the JVM that Gradle itself runs on, so CI can verify the library on every supported runtime by
// simply starting Gradle on a different JDK.
//
// The project also includes interop utilities between Java and Kotlin.

val javaToolchain = getIntProperty("internal.java.toolchain")
val javaTarget = getIntProperty("internal.java.target")

val javaTestRuntime = JavaVersion.current().majorVersion.toInt()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaToolchain)
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
tasks.withType<JavaCompile>().configureEach {
    options.release = javaTarget
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(javaToolchain)
    }
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
    }
}
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        javaParameters = true
        jvmTarget = JvmTarget.fromTarget(javaTarget.toString())
    }
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
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

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(javaTestRuntime)
    }

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

tasks.named<Task>("check").configure {
    finalizedBy(tasks.named("koverHtmlReport"), tasks.named("koverXmlReport"))
}

tasks.withType<Javadoc>().configureEach {
    javadocTool =
        javaToolchains.javadocToolFor {
            languageVersion = JavaLanguageVersion.of(javaTarget)
        }
}
