// Note that usage of version catalogs in build-logic is not as straightforward as in regular modules.
// For more information, see:
// https://docs.gradle.org/current/userguide/version_catalogs.html#sec:buildsrc-version-catalog
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(plugin(libs.plugins.errorprone))
    implementation(plugin(libs.plugins.idea.ext))
    implementation(plugin(libs.plugins.kotlin.jvm))
    implementation(plugin(libs.plugins.kotlin.kapt))
}

fun plugin(plugin: Provider<PluginDependency>): Provider<String> = plugin.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
