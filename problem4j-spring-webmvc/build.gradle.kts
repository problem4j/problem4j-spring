plugins {
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-library-convention")
    id("internal.kotlin-interop-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":problem4j-spring-web"))

    compileOnly(platform(libs.spring.boot.dependencies))
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.webmvc)

    compileOnly(libs.jakarta.servlet.api)
    compileOnly(libs.jakarta.validation.api)
    compileOnly(libs.slf4j.api)

    annotationProcessor(platform(libs.spring.boot.dependencies))
    annotationProcessor(libs.spring.boot.autoconfigure.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Test
    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.webmvc)
    testImplementation(libs.spring.boot.validation)
    testImplementation(libs.jackson3.dataformat.xml)
    testImplementation(libs.jackson3.module.kotlin)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.stdlib)

    // Included because TestRestTemplate requires it if used with actual web environment in tests. Not migrating to
    // WebTestClient either for easier merges with 1.x versions.
    testImplementation(libs.spring.boot.restclient)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Problem4J Spring WebMVC"
    description = "Spring WebMVC integration for library implementing RFC7807 (and RFC9457)"
}
