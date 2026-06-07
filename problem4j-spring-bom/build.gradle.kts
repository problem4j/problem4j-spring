plugins {
    id("java-platform")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    constraints {
        api(libs.problem4j.core)
        api(libs.problem4j.jackson2)
        api(libs.problem4j.jackson3)
        api(project(":problem4j-spring-web"))
        api(project(":problem4j-spring-webflux"))
        api(project(":problem4j-spring-webmvc"))
    }
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Problem4J Spring BOM"
    description = "BOM of Spring integration for library implementing RFC7807 (and RFC9457)"
}
