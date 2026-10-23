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

import io.github.problem4j.core.ProblemContext

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
