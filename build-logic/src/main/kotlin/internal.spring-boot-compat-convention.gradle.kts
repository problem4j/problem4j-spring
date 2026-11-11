plugins {
    id("java-library")
    id("org.jetbrains.kotlinx.kover")
}

// Runs the same compiled tests against older Spring Boot lines, listed in gradle.properties as
// internal.spring-boot.compat-lines (e.g. "4.0" or "4.0,4.1").
//
// Main and test sources are always compiled against the Spring Boot version from the version
// catalog (the newest one). Only the runtime classpath of each compat test task is different - it
// enforces spring-boot-dependencies BOM of the latest patch of the given line (e.g. 4.0.+). This
// reflects what library consumers do: they run a jar compiled against the newest Spring Boot on an
// older one.
//
// Tests relying on API of a newer Spring Boot line must be tagged with the first line providing
// it, e.g. @Tag("Boot4.1+"). Compat test task of each line excludes tags of all newer lines, up to
// the version from the catalog.

val compatLines =
    providers
        .gradleProperty("internal.spring-boot.compat-lines")
        .map { value -> value.split(',').map { it.trim() }.filter { it.isNotEmpty() } }
        .getOrElse(emptyList())

val currentLine =
    versionCatalogs
        .named("libs")
        .findVersion("spring-boot")
        .orElseThrow { GradleException("spring-boot version not found in libs version catalog") }
        .requiredVersion
        .let { BootLine.parse(it) }

val testSpringBootCompat =
    tasks.register("testSpringBootCompat") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description =
            "Runs tests against the latest patch of each older supported Spring Boot line."
    }

compatLines.forEach { line ->
    val compatLine = BootLine.parse(line)
    if (compatLine.major != currentLine.major || compatLine.minor >= currentLine.minor) {
        throw GradleException(
            "Spring Boot compat line $line must be older than $currentLine and share its major version",
        )
    }

    val suffix = "SpringBoot${compatLine.major}_${compatLine.minor}"

    val compatRuntimeOnly =
        configurations.dependencyScope("testRuntimeOnly$suffix")

    val compatRuntimeClasspath =
        configurations.resolvable("testRuntimeClasspath$suffix") {
            extendsFrom(
                configurations.testImplementation.get(),
                configurations.testRuntimeOnly.get(),
                compatRuntimeOnly.get(),
            )
            attributes.addAllLater(configurations.testRuntimeClasspath.get().attributes)
            // platform(spring-boot-dependencies) inherited from testImplementation would conflict
            // with the enforced one, so it's pointed to the same line
            resolutionStrategy.eachDependency {
                if (requested.group == "org.springframework.boot" &&
                    requested.name == "spring-boot-dependencies"
                ) {
                    useVersion("$line.+")
                }
            }
        }

    dependencies.add(
        compatRuntimeOnly.name,
        dependencies.enforcedPlatform("org.springframework.boot:spring-boot-dependencies:$line.+"),
    )

    val excludedTags =
        ((compatLine.minor + 1)..currentLine.minor).map { "Boot${compatLine.major}.$it+" }

    val compatTest =
        tasks.register<Test>("test$suffix") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Runs tests against the latest patch of Spring Boot $line.x."
            testClassesDirs = sourceSets.test.get().output.classesDirs
            classpath =
                sourceSets.test.get().output +
                sourceSets.main.get().output +
                compatRuntimeClasspath.get()
            useJUnitPlatform {
                excludeTags(*excludedTags.toTypedArray())
            }
            shouldRunAfter(tasks.test)
        }

    testSpringBootCompat.configure { dependsOn(compatTest) }

    // coverage is measured only by the main test task
    kover {
        currentProject {
            instrumentation {
                disabledForTestTasks.add(compatTest.name)
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testSpringBootCompat)
}

data class BootLine(val major: Int, val minor: Int) {
    override fun toString(): String = "$major.$minor"

    companion object {
        fun parse(version: String): BootLine {
            val parts = version.split('.')
            if (parts.size < 2) {
                throw GradleException("Invalid Spring Boot version or line: $version")
            }
            return BootLine(parts[0].toInt(), parts[1].toInt())
        }
    }
}
