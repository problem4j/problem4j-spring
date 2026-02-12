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

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Gradle extension describing a publishable artifact.
 *
 * This extension is intended to be used from build scripts to configure artifact-level metadata in
 * a lazy, configuration-cache-friendly way.
 *
 * All properties are modeled using Gradle [org.gradle.api.provider.Property] to:
 * - support lazy evaluation
 * - enable convention values
 * - remain compatible with configuration cache
 *
 * @constructor Injected by Gradle via [org.gradle.api.model.ObjectFactory].
 */
abstract class InternalPublishingExtension @Inject constructor(objects: ObjectFactory) {

  /**
   * Human-readable display name of the artifact. Used for publishing metadata and documentation.
   */
  val displayName: Property<String> = objects.property(String::class.java)

  /**
   * Description of the artifact. Appears in generated POM metadata and repository listings. Adds
   * context for consumers about the purpose and content of the artifact.
   */
  val description: Property<String> = objects.property(String::class.java)
}
