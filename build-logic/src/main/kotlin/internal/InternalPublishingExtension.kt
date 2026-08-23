/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
