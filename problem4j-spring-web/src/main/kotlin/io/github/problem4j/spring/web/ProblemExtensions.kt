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

package io.github.problem4j.spring.web

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemBuilder
import io.github.problem4j.core.ProblemContext
import org.springframework.http.HttpStatusCode

/**
 * Builds a [Problem] with the given HTTP [status].
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 *
 * val problem = problem(400)
 * ```
 *
 * @param status HTTP status code for the resulting [Problem]
 * @return the built [Problem]
 * @since 3.1.0
 */
public fun problem(status: Int): Problem = Problem.builder().status(status).build()

/**
 * Builds a [Problem] with the given HTTP [status] using a [ProblemBuilder] DSL.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 *
 * val problem = problem(400) {
 *     title("Invalid Input")
 *     detail("the provided email is invalid")
 * }
 * ```
 *
 * @param status HTTP status code for the resulting [Problem]
 * @param block configures the builder
 * @return the built [Problem]
 * @since 3.1.0
 */
public fun problem(status: Int, block: ProblemBuilder.() -> ProblemBuilder): Problem =
    Problem.builder().status(status).block().build()

/**
 * Builds a [Problem] with the given Spring [HttpStatusCode].
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 * import org.springframework.http.HttpStatus
 *
 * val problem = problem(HttpStatus.BAD_REQUEST)
 * ```
 *
 * @param status HTTP status code for the resulting [Problem]
 * @return the built [Problem]
 * @since 3.1.0
 */
public fun problem(status: HttpStatusCode): Problem = problem(status.value())

/**
 * Builds a [Problem] with the given Spring [HttpStatusCode] using a [ProblemBuilder] DSL.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 * import org.springframework.http.HttpStatus
 *
 * val problem = problem(HttpStatus.BAD_REQUEST) {
 *     title("Invalid Input")
 *     detail("the provided email is invalid")
 * }
 * ```
 *
 * @param status HTTP status code for the resulting [Problem]
 * @param block configures the builder
 * @return the built [Problem]
 * @since 3.1.0
 */
public fun problem(
    status: HttpStatusCode,
    block: ProblemBuilder.() -> ProblemBuilder,
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
 * val problem =
 *     Problem.builder()
 *         .status(HttpStatus.BAD_REQUEST)
 *         .build()
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
 * val problem =
 *     Problem.builder()
 *         .extensions("field" to "email", "reason" to "blank")
 *         .build()
 * ```
 *
 * @param extensions extension key-value pairs to add
 * @return this builder instance for chaining
 * @since 3.1.0
 */
public fun ProblemBuilder.extensions(vararg extensions: Pair<String, *>): ProblemBuilder =
    extensions(mapOf(*extensions))

/**
 * Enables destructuring of [Problem.Extension] into its name component.
 *
 * Example:
 * ```
 * import io.github.problem4j.core.Problem
 * import io.github.problem4j.spring.web.component1
 * import io.github.problem4j.spring.web.component2
 *
 * val extension = Problem.extension("field", "email")
 * val (name, _) = extension
 * // name == "field"
 * ```
 *
 * @return the name associated with this extension
 * @since 3.1.0
 */
public operator fun Problem.Extension.component1(): String = name

/**
 * Enables destructuring of [Problem.Extension] into its value component.
 *
 * Example:
 * ```
 * import io.github.problem4j.core.Problem
 * import io.github.problem4j.spring.web.component1
 * import io.github.problem4j.spring.web.component2
 *
 * val extension = Problem.extension("field", "email")
 * val (_, value) = extension
 * // value == "email"
 * ```
 *
 * @return the value associated with this extension
 * @since 3.1.0
 */
public operator fun Problem.Extension.component2(): Any? = value

/**
 * Associates multiple custom context entries from varargs of [Pair].
 *
 * Example:
 * ```
 * import io.github.problem4j.core.ProblemContext
 * import io.github.problem4j.spring.web.putAll
 *
 * val context =
 *     ProblemContext.create()
 *         .putAll("userId" to "12345", "traceId" to "abcde")
 * ```
 *
 * @param entries key-value pairs to add
 * @return this context instance for chaining
 * @since 3.1.0
 */
public fun ProblemContext.putAll(vararg entries: Pair<String, String?>): ProblemContext =
    putAll(mapOf(*entries))

/**
 * Creates a [Problem] from this HTTP status code.
 *
 * @return a new [Problem] instance
 * @since 3.1.0
 */
public fun HttpStatusCode.toProblem(): Problem = Problem.of(value())

/**
 * Creates a [Problem] from this HTTP status code with the given detail message.
 *
 * @param detail detail message
 * @return a new [Problem] instance
 * @since 3.1.0
 */
public fun HttpStatusCode.toProblem(detail: String?): Problem = Problem.of(value(), detail)
