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

/**
 * Creates a copy of this [Problem] with the changes applied by a [ProblemBuilderSpec] DSL. The
 * original [Problem] is left unchanged.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.copy
 * import io.github.problem4j.spring.web.buildProblem
 *
 * val original = buildProblem {
 *     status(400)
 *     detail("the provided email is invalid")
 * }
 * val copy = original.copy { extension("field", "email") }
 * ```
 *
 * @param block configures the builder, pre-populated with the fields of this [Problem]
 * @return the new [Problem]
 * @since 3.1.0
 */
public fun Problem.copy(block: ProblemBuilderSpec.() -> Unit): Problem =
    toBuilder().applySpec(block)

/**
 * Returns the value of the extension with the given name, or `null` when no such extension is
 * present.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.get
 * import io.github.problem4j.spring.web.buildProblem
 *
 * val problem = buildProblem {
 *     status(400)
 *     extension("field", "email")
 * }
 * val field = problem["field"]
 * // field == "email"
 * ```
 *
 * @param name the extension key
 * @return the extension value, or `null` when absent
 * @since 3.1.0
 */
public operator fun Problem.get(name: String): Any? = extensions[name]

/**
 * Returns the value of the extension with the given name cast to [T], or `null` when no such
 * extension is present or its value is not a [T].
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.extension
 * import io.github.problem4j.spring.web.buildProblem
 *
 * val problem = buildProblem {
 *     status(400)
 *     extension("attempts", 3)
 * }
 * val attempts = problem.extension<Int>("attempts")
 * // attempts == 3
 * ```
 *
 * @param name the extension key
 * @param T expected type of the extension value
 * @return the extension value, or `null` when absent or of another type
 * @since 3.1.0
 */
public inline fun <reified T> Problem.extension(name: String): T? = extensions[name] as? T

/**
 * Checks whether an extension with the given name is present.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.contains
 * import io.github.problem4j.spring.web.buildProblem
 *
 * val problem = buildProblem {
 *     status(400)
 *     extension("field", "email")
 * }
 * val hasField = "field" in problem
 * // hasField == true
 * ```
 *
 * @param name the extension key
 * @return `true` when an extension with this name is present, `false` otherwise
 * @since 3.1.0
 */
public operator fun Problem.contains(name: String): Boolean = extensions.containsKey(name)

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
