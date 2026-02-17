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
