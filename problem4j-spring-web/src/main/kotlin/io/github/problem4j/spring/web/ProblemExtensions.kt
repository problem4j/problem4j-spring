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
@ProblemDslMarker
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
    Problem.builder().build(block)

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
public fun Problem.copy(block: ProblemBuilderSpec.() -> Unit): Problem = toBuilder().build(block)

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
 * Returns the HTTP status of this problem as a Spring [HttpStatusCode].
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.httpStatus
 * import io.github.problem4j.spring.web.buildProblem
 *
 * val problem = buildProblem { status(400) }
 * val status = problem.httpStatus
 * // status == HttpStatus.BAD_REQUEST
 * ```
 *
 * @throws IllegalArgumentException if the numeric status is not a valid HTTP status code
 * @since 3.1.0
 */
public val Problem.httpStatus: HttpStatusCode
  get() = HttpStatusCode.valueOf(status)

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
 * Associates the given value with the given key, or unsets it when [value] is `null`.
 *
 * Example:
 * ```
 * import io.github.problem4j.core.ProblemContext
 * import io.github.problem4j.spring.web.set
 *
 * val context = ProblemContext.create()
 * context["userId"] = "12345"
 * ```
 *
 * @param key the context key
 * @param value the value to associate, or `null` to unset it
 * @since 3.1.0
 */
public operator fun ProblemContext.set(key: String, value: String?) {
  put(key, value)
}

/**
 * Checks whether the context contains a value for the given key.
 *
 * Example:
 * ```
 * import io.github.problem4j.core.ProblemContext
 * import io.github.problem4j.spring.web.contains
 *
 * val context = ProblemContext.create().put("userId", "12345")
 * val hasUserId = "userId" in context
 * // hasUserId == true
 * ```
 *
 * @param key the context key
 * @return `true` when the context contains a value for this key, `false` otherwise
 * @since 3.1.0
 */
public operator fun ProblemContext.contains(key: String): Boolean = containsKey(key)

@PublishedApi
internal fun ProblemBuilder.build(block: ProblemBuilderSpec.() -> Unit): Problem =
    ProblemBuilderSpecImpl(this).apply(block).build()

@DslMarker internal annotation class ProblemDslMarker

private class ProblemBuilderSpecImpl(private var builder: ProblemBuilder) : ProblemBuilderSpec {

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
