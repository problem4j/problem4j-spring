package io.github.problem4j.spring.web

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemBuilder
import io.github.problem4j.core.ProblemException
import org.springframework.http.HttpStatusCode

/**
 * Builds a [Problem] with the given HTTP [status] using a [ProblemBuilder] DSL.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 *
 * val problem = problem(400) { title("Invalid Email").detail("invalid email") }
 * ```
 *
 * @param status HTTP status code for the resulting [Problem]
 * @param block configures the builder; defaults to no further configuration
 * @return the built [Problem]
 * @since 3.1.0
 */
public fun problem(status: Int, block: ProblemBuilder.() -> ProblemBuilder = { this }): Problem =
    Problem.builder().status(status).block().build()

/**
 * Builds a [Problem] with the given Spring [HttpStatusCode] using a [ProblemBuilder] DSL.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 * import org.springframework.http.HttpStatus
 *
 * val problem = problem(HttpStatus.BAD_REQUEST) { title("Invalid Email").detail("invalid email") }
 * ```
 *
 * @param status HTTP status code for the resulting [Problem]
 * @param block configures the builder; defaults to no further configuration
 * @return the built [Problem]
 * @since 3.1.0
 */
public fun problem(
    status: HttpStatusCode,
    block: ProblemBuilder.() -> ProblemBuilder = { this },
): Problem = problem(status.value(), block)

/**
 * Sets the HTTP status for this builder from a Spring [HttpStatusCode].
 *
 * Example:
 * ```
 * import io.github.problem4j.core.Problem
 * import io.github.problem4j.spring.web.status
 * import org.springframework.http.HttpStatus
 *
 * val problem = Problem.builder().status(HttpStatus.BAD_REQUEST).build()
 * ```
 *
 * @param status HTTP status code
 * @return this builder instance for chaining
 * @since 3.1.0
 */
public fun ProblemBuilder.status(status: HttpStatusCode): ProblemBuilder = status(status.value())

/**
 * Adds multiple custom extensions from varargs of [Pair].
 *
 * Example:
 * ```
 * import io.github.problem4j.core.Problem
 * import io.github.problem4j.spring.web.extensions
 *
 * val problem = Problem.builder().extensions("field" to "email", "reason" to "blank").build()
 * ```
 *
 * @param extensions extension key-value pairs to add
 * @return this builder instance for chaining
 * @since 3.1.0
 */
public fun ProblemBuilder.extensions(vararg extensions: Pair<String, *>): ProblemBuilder =
    extensions(mapOf(*extensions))

/**
 * Creates a [Problem] from this HTTP status code.
 *
 * @param detail optional detail message
 * @return a new [Problem] instance
 * @since 3.1.0
 */
public fun HttpStatusCode.toProblem(detail: String? = null): Problem = Problem.of(value(), detail)

/**
 * Wraps this [Problem] in a [ProblemException].
 *
 * @param message custom exception message; defaults to a message generated from the problem
 * @param cause root cause of the exception
 * @return a new [ProblemException] wrapping this [Problem]
 * @since 3.1.0
 */
public fun Problem.toException(
    message: String? = null,
    cause: Throwable? = null,
): ProblemException = ProblemException(message, this, cause)
