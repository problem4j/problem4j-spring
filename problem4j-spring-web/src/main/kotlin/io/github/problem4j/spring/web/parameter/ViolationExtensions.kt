package io.github.problem4j.spring.web.parameter

/**
 * Enables destructuring of [Violation] into its field component.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.parameter.Violation
 * import io.github.problem4j.spring.web.parameter.component1
 * import io.github.problem4j.spring.web.parameter.component2
 *
 * val violation = Violation("age", "must be positive")
 * val (field, _) = violation
 * // field == "age"
 * ```
 *
 * @return the field name associated with this violation
 * @since 3.1.0
 */
public operator fun Violation.component1(): String? = field

/**
 * Enables destructuring of [Violation] into its error component.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.parameter.Violation
 * import io.github.problem4j.spring.web.parameter.component1
 * import io.github.problem4j.spring.web.parameter.component2
 *
 * val violation = Violation("age", "must be positive")
 * val (_, error) = violation
 * // error == "must be positive"
 * ```
 *
 * @return the error message associated with this violation
 * @since 3.1.0
 */
public operator fun Violation.component2(): String? = error
