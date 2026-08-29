import com.diffplug.spotless.LineEnding
import internal.getBooleanProperty

plugins {
    id("internal.idea-convention")
    id("jacoco-report-aggregation")
    alias(libs.plugins.nmcp).apply(false)
    alias(libs.plugins.nmcp.aggregation)
    alias(libs.plugins.spotless)
}

dependencies {
    nmcpAggregation(project(":problem4j-spring-bom"))
    nmcpAggregation(project(":problem4j-spring-web"))
    nmcpAggregation(project(":problem4j-spring-webflux"))
    nmcpAggregation(project(":problem4j-spring-webmvc"))

    jacocoAggregation(project(":problem4j-spring-web"))
    jacocoAggregation(project(":problem4j-spring-webflux"))
    jacocoAggregation(project(":problem4j-spring-webmvc"))
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("PUBLISHING_USERNAME")
        password = System.getenv("PUBLISHING_PASSWORD")

        publishingType = "USER_MANAGED"
    }
}

reporting {
    reports {
        register<JacocoCoverageReport>("testCodeCoverageReport") {
            testSuiteName = "test"
        }
    }
}

spotless {
    val licenseHeader = "${rootProject.rootDir}/gradle/license-header.java"
    val updateLicenseYear = project.getBooleanProperty("spotless.license-year-enabled")

    java {
        target("**/src/**/*.java")
        licenseHeaderFile(licenseHeader).updateYearWithLatest(updateLicenseYear)

        googleJavaFormat()
        forbidWildcardImports()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    format("javaMisc") {
        target("**/src/**/package-info.java", "**/src/**/module-info.java")

        // License headers in these files are not formatted with standard java group, so we need to use custom settings.
        // The regex is designed to find out where the code starts in these files, so the license header can be placed
        // before it.
        //
        // The code starts with either:
        //
        // - any annotation (ex. @NullMarked before package declaration),
        // - package, module or import declaration,
        // - "/**" in case of a pre-package (or pre-module) JavaDoc.
        val delimiter = "^(@|package|import|module|/\\*\\*)"

        licenseHeaderFile(licenseHeader, delimiter).updateYearWithLatest(updateLicenseYear)
    }

    kotlin {
        target("**/src/**/*.kt")

        ktfmt().metaStyle()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")

        ktlint().editorConfigOverride(mapOf("max_line_length" to "120"))
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    format("yaml") {
        target("**/*.yml", "**/*.yaml")

        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    format("misc") {
        target("**/.gitattributes", "**/.gitignore")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}

tasks.named<Task>("check").configure {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}

defaultTasks("spotlessApply", "build")
