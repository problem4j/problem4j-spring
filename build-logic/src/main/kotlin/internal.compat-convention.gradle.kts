import internal.getListProperty

plugins {
    id("java-library")
    id("org.jetbrains.kotlinx.kover")
    id("io.github.malczuuu.downshift")
}

// Runs the same compiled tests against older Spring Boot lines, listed in gradle.properties as
// internal.spring-boot.compat-lines (e.g. "4.0.+" or "4.0.+,4.0.0").
//
// Tests relying on API of a newer Spring Boot line must be tagged with the first line providing
// it, e.g. @Tag("springBoot4.1+").

downshift {
    platform("springBoot") {
        bom = "org.springframework.boot:spring-boot-dependencies"
        lines(getListProperty("internal.spring-boot.compat-lines"))
    }
}

// coverage is measured only by the main test task
kover {
    currentProject {
        instrumentation {
            disabledForTestTasks.addAll(downshift.testTaskNames)
        }
    }
}
