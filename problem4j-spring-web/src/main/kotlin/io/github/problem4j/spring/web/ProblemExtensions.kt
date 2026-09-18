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
import java.net.URI
import org.springframework.http.HttpStatusCode

/**
 * Mutable DSL receiver used by [problem] functions. Exposes all configuration methods of
 * [ProblemBuilder] except `build()`, which is invoked by [problem] once the DSL block completes.
 * Each call in the block is applied, so methods do not need to be chained.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 *
 * val problem = problem(400) {
 *     title("Invalid Input")
 *     detail("the provided email is invalid")
 *     extension("field", "email")
 * }
 * ```
 *
 * @since 3.1.0
 */
@ProblemDslMarker
public interface ProblemBuilderDsl {

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
   * Sets the HTTP status code of the problem, overriding the one passed to [problem].
   *
   * @param status numeric HTTP status code
   * @since 3.1.0
   */
  public fun status(status: Int)

  /**
   * Sets the HTTP status code of the problem from a Spring [HttpStatusCode], overriding the one
   * passed to [problem].
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
   * problem(400) {
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
 * Builds a [Problem] with the given HTTP [status] using a [ProblemBuilderDsl] DSL.
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
public fun problem(status: Int, block: ProblemBuilderDsl.() -> Unit): Problem =
    ProblemBuilderDslImpl(Problem.builder().status(status)).apply(block).build()

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
 * Builds a [Problem] with the given Spring [HttpStatusCode] using a [ProblemBuilderDsl] DSL.
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
    block: ProblemBuilderDsl.() -> Unit,
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
 * DSL marker for the [problem] builder blocks. Stops a nested block from implicitly calling a
 * method of an enclosing block's receiver, so each call in a nested `problem { }` always applies to
 * the innermost [ProblemBuilderDsl].
 */
@DslMarker
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
internal annotation class ProblemDslMarker

private class ProblemBuilderDslImpl(private var builder: ProblemBuilder) : ProblemBuilderDsl {

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
