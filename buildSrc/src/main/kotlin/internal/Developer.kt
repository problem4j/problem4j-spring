/*
 * Copyright (c) 2025-2026 Damian Malczewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package internal

import org.gradle.api.Project

/**
 * Represents a single developer entry used in publishing metadata (e.g. POM developers section).
 *
 * All fields except [id] are optional to allow partial definitions in `gradle.properties`. Missing
 * optional values are simply omitted from the generated publication metadata.
 *
 * @property id Unique developer identifier (required).
 * @property name Human-readable developer name.
 * @property email Contact email address.
 * @property url Developer personal or profile URL.
 * @property organization Organization the developer belongs to.
 * @property organizationUrl URL of the organization.
 */
data class Developer(
    val id: String,
    val name: String?,
    val email: String?,
    val url: String?,
    val organization: String?,
    val organizationUrl: String?,
)

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
            findProperty("internal.pom.developers.$index.id") as? String ?: break,
            findProperty("internal.pom.developers.$index.name") as? String,
            findProperty("internal.pom.developers.$index.url") as? String,
            findProperty("internal.pom.developers.$index.email") as? String,
            findProperty("internal.pom.developers.$index.organization") as? String,
            findProperty("internal.pom.developers.$index.organization-url") as? String,
        )
    )
    index++
  }
  return developers
}
