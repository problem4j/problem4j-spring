plugins {
    id("internal.errorprone-convention")
    id("internal.java-library-convention")
    id("internal.kover-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":problem4j-spring-web"))

    compileOnly(platform(libs.kotlin.bom))
    compileOnly(platform(libs.spring.boot.dependencies))
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.webmvc)

    compileOnly(libs.jakarta.servlet.api)
    compileOnly(libs.jakarta.validation.api)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.slf4j.api)

    annotationProcessor(platform(libs.spring.boot.dependencies))
    annotationProcessor(libs.spring.boot.autoconfigure.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Test
    testImplementation(platform(libs.junit.bom))
    testImplementation(platform(libs.kotlin.bom))
    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.restclient)
    testImplementation(libs.spring.boot.validation)
    testImplementation(libs.archunit.junit6)
    testImplementation(libs.jackson3.dataformat.xml)
    testImplementation(libs.jackson3.module.kotlin)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.reactor)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Problem4J Spring WebMVC"
    description = "Spring WebMVC integration for library implementing RFC7807 (and RFC9457)"
}
