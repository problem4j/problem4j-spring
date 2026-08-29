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

package io.github.problem4j.spring.web.resolver

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode

/**
 * Creates a [ProblemResolver] for exception type [E] from a lambda, without subclassing
 * [AbstractProblemResolver].
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 * import io.github.problem4j.spring.web.resolver.ProblemResolver
 * import io.github.problem4j.spring.web.resolver.problemResolver
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun illegalStateProblemResolver(): ProblemResolver =
 *     problemResolver<IllegalStateException> { _, ex, _, status ->
 *         problem(500) { detail(ex.message) }
 *     }
 * ```
 *
 * @param resolve resolves the exception into a [Problem]
 * @return a [ProblemResolver] handling exceptions of type [E]
 * @since 3.1.0
 */
public inline fun <reified E : Exception> problemResolver(
    crossinline resolve: (ProblemContext, E, HttpHeaders, HttpStatusCode) -> Problem,
): ProblemResolver =
    object : AbstractProblemResolver(E::class.java) {
      override fun resolve(
          context: ProblemContext,
          ex: Exception,
          headers: HttpHeaders,
          status: HttpStatusCode,
      ) = resolve(context, ex as E, headers, status)
    }
