plugins {
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(libs.jspecify)
    api(libs.problem4j.core)
    api(libs.problem4j.jackson3)

    compileOnly(platform(libs.spring.boot.dependencies))
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.jackson)
    compileOnly(libs.spring.web)

    compileOnly(libs.jackson3.dataformat.xml)
    compileOnly(libs.jakarta.servlet.api)
    compileOnly(libs.jakarta.validation.api)

    // for backwards compatibility with jackson-databind v2
    compileOnly(libs.jackson2.databind)
    compileOnly(libs.problem4j.jackson2)

    annotationProcessor(platform(libs.spring.boot.dependencies))
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Test
    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.jackson)
    testImplementation(libs.spring.boot.web.server)
    testImplementation(libs.spring.boot.validation)
    testImplementation(libs.jakarta.servlet.api)

    testRuntimeOnly(libs.junit.platform.launcher)
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Problem4J Spring Web"
    description = "Spring Web integration for library implementing RFC7807 (aka RFC9457)."
}
