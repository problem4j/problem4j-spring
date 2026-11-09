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
import io.github.problem4j.spring.web.ProblemBuilderSpecImpl
import io.github.problem4j.spring.web.ProblemResolverSpec
import io.github.problem4j.spring.web.ProblemResolverSpecImpl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode

/**
 * Creates a [ProblemResolver] for exception type [E] from a lambda, without subclassing
 * [AbstractProblemResolver]. The builder is pre-populated with the status recommended by the
 * caller.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.resolver.ProblemResolver
 * import io.github.problem4j.spring.web.resolver.problemResolver
 * import org.springframework.context.annotation.Bean
 * import org.springframework.http.HttpHeaders
 *
 * @Bean
 * fun illegalStateProblemResolver(): ProblemResolver =
 *     problemResolver<IllegalStateException> { ex ->
 *         status(409)
 *         detail(formatDetail(ex.message))
 *         extension("something", context["something"])
 *         headers.set(HttpHeaders.RETRY_AFTER, "120")
 *     }
 * ```
 *
 * @param resolve configures the builder from the exception being resolved
 * @return a [ProblemResolver] handling exceptions of type [E]
 * @since 3.1.0
 */
public inline fun <reified E : Exception> problemResolver(
    noinline resolve: ProblemResolverSpec.(E) -> Unit,
): ProblemResolver = DslProblemResolver(E::class.java, resolve)

@PublishedApi
internal class DslProblemResolver<E : Exception>(
    private val type: Class<E>,
    private val resolve: ProblemResolverSpec.(E) -> Unit,
) : AbstractProblemResolver(type) {

  override fun resolve(
      context: ProblemContext,
      ex: Exception,
      headers: HttpHeaders,
      status: HttpStatusCode,
  ): Problem {
    val spec =
        ProblemResolverSpecImpl(
            ProblemBuilderSpecImpl(Problem.builder().status(status.value())),
            context,
            headers,
            ::formatDetail,
        )
    spec.resolve(type.cast(ex))
    return spec.build()
  }
}
