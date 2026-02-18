// Note that usage of version catalogs in buildSrc is not as straightforward as in regular modules.
// For more information, see:
// https://docs.gradle.org/current/userguide/version_catalogs.html#sec:buildsrc-version-catalog
plugins {
    `kotlin-dsl`
}

version = "current"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(plugin(libs.plugins.errorprone))
}

fun plugin(plugin: Provider<PluginDependency>): Provider<String> = plugin.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
