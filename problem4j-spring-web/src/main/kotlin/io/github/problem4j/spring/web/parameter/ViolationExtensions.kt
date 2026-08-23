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

package io.github.problem4j.spring.web.parameter

/**
 * Creates a [Violation] for the given [field] and [error] message.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.parameter.violation
 *
 * val violation = violation("age", "must be positive")
 * ```
 *
 * @param field name of the violating field; defaults to `null`
 * @param error error message describing the violation; defaults to `null`
 * @return a [Violation] with the given field and error
 * @since 3.1.0
 */
public fun violation(field: String? = null, error: String? = null): Violation =
    Violation(field, error)

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
