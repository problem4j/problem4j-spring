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
import org.springframework.http.HttpStatusCode

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

@PublishedApi
internal fun ProblemBuilder.applySpec(block: ProblemBuilderSpec.() -> Unit): Problem =
    ProblemBuilderSpecImpl(this).apply(block).build()
