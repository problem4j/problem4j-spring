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

package io.github.problem4j.spring.webflux

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ServerWebExchange

/**
 * Creates an [AdviceWebFluxInspector] from a lambda.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webflux.AdviceWebFluxInspector
 * import io.github.problem4j.spring.webflux.adviceWebFluxInspector
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun loggingInspector(): AdviceWebFluxInspector =
 *     adviceWebFluxInspector { ctx, problem, ex, headers, status, exchange ->
 *         log.warn("Resolved {} to {}", ex.message, problem.status)
 *     }
 * ```
 *
 * @param inspect observes the resolved [Problem] with all arguments of
 *   [AdviceWebFluxInspector.inspect]
 * @return an [AdviceWebFluxInspector] delegating to [inspect]
 * @since 3.1.0
 */
public fun adviceWebFluxInspector(
    inspect:
        (
            ProblemContext,
            Problem,
            Exception,
            HttpHeaders,
            HttpStatusCode,
            ServerWebExchange,
        ) -> Unit,
): AdviceWebFluxInspector = AdviceWebFluxInspector(inspect)

/**
 * Creates an [AdviceWebFluxInspector] from a lambda.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webflux.AdviceWebFluxInspector
 * import io.github.problem4j.spring.webflux.adviceWebFluxInspector
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun loggingInspector(): AdviceWebFluxInspector =
 *     adviceWebFluxInspector { ctx, problem, ex, exchange ->
 *         log.warn("Resolved {} to {}", ex.message, problem.status)
 *     }
 * ```
 *
 * @param inspect observes the resolved [Problem] together with the exception that caused it
 * @return an [AdviceWebFluxInspector] delegating to [inspect]
 * @since 3.1.0
 */
public fun adviceWebFluxInspector(
    inspect: (ProblemContext, Problem, Exception, ServerWebExchange) -> Unit,
): AdviceWebFluxInspector = AdviceWebFluxInspector { context, problem, ex, _, _, exchange ->
  inspect(context, problem, ex, exchange)
}

/**
 * Creates an [AdviceWebFluxInspector] from a lambda.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webflux.AdviceWebFluxInspector
 * import io.github.problem4j.spring.webflux.adviceWebFluxInspector
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun loggingInspector(): AdviceWebFluxInspector =
 *     adviceWebFluxInspector { problem, ex ->
 *         log.warn("Resolved {} to {}", ex.message, problem.status)
 *     }
 * ```
 *
 * @param inspect observes the resolved [Problem] together with the exception that caused it
 * @return an [AdviceWebFluxInspector] delegating to [inspect]
 * @since 3.1.0
 */
public fun adviceWebFluxInspector(inspect: (Problem, Exception) -> Unit): AdviceWebFluxInspector =
    AdviceWebFluxInspector { _, problem, ex, _, _, _ ->
      inspect(problem, ex)
    }
