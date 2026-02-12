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
 * All fields except [name] are optional to allow partial definitions in `gradle.properties`.
 * Missing optional values are simply omitted from the generated publication metadata.
 *
 * @property name Human-readable license name.
 * @property url URL to the full license details.
 */
data class License(
    val name: String?,
    val url: String?,
    val distribution: String?,
    val comments: String?,
)

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
  val developers = mutableListOf<License>()
  var index = 0
  while (true) {
    developers.add(
        License(
            findProperty("internal.pom.licenses.$index.name") as? String ?: break,
            findProperty("internal.pom.licenses.$index.url") as? String,
            findProperty("internal.pom.licenses.$index.distribution") as? String,
            findProperty("internal.pom.licenses.$index.comments") as? String,
        )
    )
    index++
  }
  return developers
}
