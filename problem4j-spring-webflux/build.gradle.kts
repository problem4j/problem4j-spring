plugins {
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":problem4j-spring-web"))

    compileOnly(platform(libs.spring.boot.dependencies))
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.webflux)

    compileOnly(libs.jakarta.validation.api)
    compileOnly(libs.slf4j.api)

    annotationProcessor(platform(libs.spring.boot.dependencies))
    annotationProcessor(libs.spring.boot.autoconfigure.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Test
    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.webflux)
    testImplementation(libs.spring.boot.starter.webflux.test)
    testImplementation(libs.spring.boot.validation)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.jackson3.module.kotlin)
    testImplementation(libs.jspecify)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.stdlib)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Problem4J Spring WebFlux"
    description = "Spring WebFlux integration for library implementing RFC7807 (and RFC9457)"
}
