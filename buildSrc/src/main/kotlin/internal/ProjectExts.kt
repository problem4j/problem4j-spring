package internal

// This file contains extension functions for Gradle's Project API.

import org.gradle.api.Project

/**
 * Evaluates a structured list of developers defined in `gradle.properties`.
 *
 * Developers are resolved using zero-based indexing and the following property naming convention:
 * ```properties
 * internal.pom.developers.0.id=john.doe
 * internal.pom.developers.0.name=JohnDoe
 * internal.pom.developers.0.url=https://johndoe.me
 *
 * internal.pom.developers.1.id=otherdev
 * internal.pom.developers.1.name=Other Developer
 * ```
 *
 * Evaluation stops at the first missing mandatory `id` property. This allows the list to be
 * extended by simply adding the next index.
 *
 * @return An ordered list of resolved [Developer] definitions.
 * @receiver Gradle [Project] from which properties are read.
 */
fun Project.findDevelopers(): List<Developer> {
  val developers = mutableListOf<Developer>()
  var index = 0
  while (true) {
    developers.add(
        Developer(
            id = findProperty("internal.pom.developers.$index.id") as? String ?: break,
            name = findProperty("internal.pom.developers.$index.name") as? String,
            email = findProperty("internal.pom.developers.$index.email") as? String,
            url = findProperty("internal.pom.developers.$index.url") as? String,
            organization = findProperty("internal.pom.developers.$index.organization") as? String,
            organizationUrl =
                findProperty("internal.pom.developers.$index.organization-url") as? String,
        )
    )
    index++
  }
  return developers
}

/**
 * Evaluates a structured list of licenses defined in `gradle.properties`.
 *
 * Licenses are resolved using zero-based indexing and the following property naming convention:
 * ```properties
 * internal.pom.licenses.0.name=MIT License
 * internal.pom.licenses.0.url=https://opensource.org/license/MIT
 *
 * internal.pom.licenses.1.name=Apache-2.0 License
 * ```
 *
 * Evaluation stops at the first missing mandatory `name` property. This allows the list to be
 * extended by simply adding the next index.
 *
 * @return An ordered list of resolved [License] definitions.
 * @receiver Gradle [Project] from which properties are read.
 */
fun Project.findLicenses(): List<License> {
  val licenses = mutableListOf<License>()
  var index = 0
  while (true) {
    licenses.add(
        License(
            name = findProperty("internal.pom.licenses.$index.name") as? String ?: break,
            url = findProperty("internal.pom.licenses.$index.url") as? String,
            distribution = findProperty("internal.pom.licenses.$index.distribution") as? String,
            comments = findProperty("internal.pom.licenses.$index.comments") as? String,
        )
    )
    index++
  }
  return licenses
}

/**
 * Retrieves a boolean property from the project, interpreting any value other than `"false"` as
 * `true`.
 *
 * @param name The name of the property to retrieve.
 * @param defaultValue The default boolean value to return if the property is not set. Defaults to
 *   `false`.
 * @return The boolean value of the property, or [defaultValue] if the property is not set.
 * @receiver Gradle [Project] from which the property is read.
 */
fun Project.getBooleanProperty(name: String, defaultValue: Boolean = false): Boolean {
  return if (hasProperty(name)) findProperty(name)?.toString() != "false" else defaultValue
}
