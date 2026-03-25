// Note that usage of version catalogs in buildSrc is not as straightforward as in regular modules.
// For more information, see:
// https://docs.gradle.org/current/userguide/version_catalogs.html#sec:buildsrc-version-catalog
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "buildSrc"
