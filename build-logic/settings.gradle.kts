// Note that usage of version catalogs in build-logic is not as straightforward as in regular modules.
// For more information, see:
// https://docs.gradle.org/current/userguide/version_catalogs.html#sec:buildsrc-version-catalog
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
