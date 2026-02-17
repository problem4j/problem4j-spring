package internal

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
