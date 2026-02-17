package internal

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
