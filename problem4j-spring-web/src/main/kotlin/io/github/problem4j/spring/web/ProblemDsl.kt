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
import java.net.URI
import org.springframework.http.HttpStatusCode

/**
 * Marks the Kotlin DSLs of problem4j-spring. Prevents calling members of an outer DSL receiver
 * implicitly from within a nested DSL block.
 *
 * @since 3.1.0
 */
@DslMarker
@Target(AnnotationTarget.CLASS) //
public annotation class ProblemDsl

/**
 * Mutable DSL receiver used by [buildProblem] functions. Exposes all configuration methods of
 * [ProblemBuilder] except `build()`, which is invoked by [buildProblem] once the DSL block
 * completes. Each call in the block is applied, so methods do not need to be chained.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.buildProblem
 *
 * val problem = buildProblem {
 *     title("Invalid Input")
 *     status(400)
 *     detail("the provided email is invalid")
 *     extension("field", "email")
 * }
 * ```
 *
 * @since 3.1.0
 */
@ProblemDsl
public interface ProblemBuilderSpec {

  /**
   * Sets the problem type URI, identifying the kind of problem.
   *
   * @param type URI identifying the problem type, or `null` to fall back to [Problem.BLANK_TYPE]
   * @since 3.1.0
   */
  public fun type(type: URI?)

  /**
   * Sets the problem type from a string representation of a URI.
   *
   * @param type string URI identifying the problem type, or `null` to fall back to
   *   [Problem.BLANK_TYPE]
   * @throws IllegalArgumentException if [type] is not a valid URI
   * @since 3.1.0
   */
  public fun type(type: String?)

  /**
   * Sets the short, human-readable summary of the problem type. When not set or `null`, the title
   * is resolved from the HTTP status, if it corresponds to a known one.
   *
   * @param title the problem title, or `null` to unset it
   * @since 3.1.0
   */
  public fun title(title: String?)

  /**
   * Sets the HTTP status code of the problem.
   *
   * @param status numeric HTTP status code
   * @since 3.1.0
   */
  public fun status(status: Int)

  /**
   * Sets the HTTP status code of the problem from a Spring [HttpStatusCode].
   *
   * @param status Spring HTTP status code
   * @since 3.1.0
   */
  public fun status(status: HttpStatusCode)

  /**
   * Sets the human-readable explanation specific to this occurrence of the problem.
   *
   * @param detail the detail message, or `null` to unset it
   * @since 3.1.0
   */
  public fun detail(detail: String?)

  /**
   * Sets the URI identifying this specific occurrence of the problem.
   *
   * @param instance URI identifying the problem occurrence, or `null` to unset it
   * @since 3.1.0
   */
  public fun instance(instance: URI?)

  /**
   * Sets the instance URI from a string representation.
   *
   * @param instance string URI identifying the problem occurrence, or `null` to unset it
   * @throws IllegalArgumentException if [instance] is not a valid URI
   * @since 3.1.0
   */
  public fun instance(instance: String?)

  /**
   * Adds a single custom extension member. If [value] is `null`, an existing extension with the
   * same [name] is removed instead.
   *
   * @param name the extension key
   * @param value the extension value, or `null` to remove the extension
   * @since 3.1.0
   */
  public fun extension(name: String, value: Any?)

  /**
   * Adds a single custom extension member from a [Problem.Extension]. If its value is `null`, an
   * existing extension with the same name is removed instead.
   *
   * @param extension the extension to add
   * @since 3.1.0
   */
  public fun extension(extension: Problem.Extension)

  /**
   * Adds multiple custom extension members from a map. Entries with `null` values remove existing
   * extensions with the same key instead.
   *
   * @param extensions map of extension keys and values
   * @since 3.1.0
   */
  public fun extensions(extensions: Map<String, Any?>)

  /**
   * Adds multiple custom extension members from varargs of [Problem.Extension]. Extensions with
   * `null` values remove existing extensions with the same name instead.
   *
   * @param extensions extensions to add
   * @since 3.1.0
   */
  public fun extensions(vararg extensions: Problem.Extension)

  /**
   * Adds multiple custom extension members from a collection of [Problem.Extension]. Extensions
   * with `null` values remove existing extensions with the same name instead.
   *
   * @param extensions extensions to add
   * @since 3.1.0
   */
  public fun extensions(extensions: Iterable<Problem.Extension>)

  /**
   * Adds multiple custom extension members from varargs of [Pair], where each pair is a key and a
   * value. Pairs with `null` values remove existing extensions with the same key instead.
   *
   * Example:
   * ```
   * buildProblem {
   *     status(400)
   *     extensions("field" to "email", "reason" to "blank")
   * }
   * ```
   *
   * @param extensions extension key-value pairs to add
   * @since 3.1.0
   */
  public fun extensions(vararg extensions: Pair<String, *>)
}

/**
 * Builds a [Problem] using a [ProblemBuilderSpec] DSL.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.buildProblem
 *
 * val problem = buildProblem {
 *     title("Invalid Input")
 *     status(400)
 *     detail("the provided email is invalid")
 * }
 * ```
 *
 * @param block configures the builder
 * @return the built [Problem]
 * @since 3.1.0
 */
public fun buildProblem(block: ProblemBuilderSpec.() -> Unit): Problem =
    Problem.builder().applySpec(block)

internal class ProblemBuilderSpecImpl(private var builder: ProblemBuilder) : ProblemBuilderSpec {

  override fun type(type: URI?) {
    builder = builder.type(type)
  }

  override fun type(type: String?) {
    builder = builder.type(type)
  }

  override fun title(title: String?) {
    builder = builder.title(title)
  }

  override fun status(status: Int) {
    builder = builder.status(status)
  }

  override fun status(status: HttpStatusCode) {
    builder = builder.status(status)
  }

  override fun detail(detail: String?) {
    builder = builder.detail(detail)
  }

  override fun instance(instance: URI?) {
    builder = builder.instance(instance)
  }

  override fun instance(instance: String?) {
    builder = builder.instance(instance)
  }

  override fun extension(name: String, value: Any?) {
    builder = builder.extension(name, value)
  }

  override fun extension(extension: Problem.Extension) {
    builder = builder.extension(extension)
  }

  override fun extensions(extensions: Map<String, Any?>) {
    builder = builder.extensions(extensions)
  }

  override fun extensions(vararg extensions: Problem.Extension) {
    builder = builder.extensions(*extensions)
  }

  override fun extensions(extensions: Iterable<Problem.Extension>) {
    builder = builder.extensions(extensions)
  }

  override fun extensions(vararg extensions: Pair<String, *>) {
    builder = builder.extensions(*extensions)
  }

  fun build(): Problem = builder.build()
}
