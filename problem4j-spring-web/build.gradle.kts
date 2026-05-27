plugins {
    id("internal.jacoco-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(libs.problem4j.core)
    api(libs.problem4j.jackson2)

    compileOnly(platform(libs.spring.boot.dependencies))
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.web)

    compileOnly(libs.jackson2.databind)
    compileOnly(libs.jakarta.servlet.api)
    compileOnly(libs.jakarta.validation.api)

    annotationProcessor(platform(libs.spring.boot.dependencies))
    annotationProcessor(libs.spring.boot.autoconfigure.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Test
    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.starter.validation)
    testImplementation(libs.jakarta.servlet.api)

    testRuntimeOnly(libs.junit.platform.launcher)
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Problem4J Spring Web"
    description = "Spring Web integration for library implementing RFC7807 (aka RFC9457)."
}
