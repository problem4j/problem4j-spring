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

package io.github.problem4j.spring.webmvc

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.web.context.request.WebRequest

/**
 * Creates an [AdviceWebMvcInspector] from a lambda.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webmvc.AdviceWebMvcInspector
 * import io.github.problem4j.spring.webmvc.adviceWebMvcInspector
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun loggingInspector(): AdviceWebMvcInspector =
 *     adviceWebMvcInspector { ctx, problem, ex, headers, status, request ->
 *         log.warn("Resolved {} to {}", ex.message, problem.status)
 *     }
 * ```
 *
 * @param inspect observes the resolved [Problem] with all arguments of
 *   [AdviceWebMvcInspector.inspect]
 * @return an [AdviceWebMvcInspector] delegating to [inspect]
 * @since 3.1.0
 */
public fun adviceWebMvcInspector(
    inspect: (ProblemContext, Problem, Exception, HttpHeaders, HttpStatusCode, WebRequest) -> Unit,
): AdviceWebMvcInspector = AdviceWebMvcInspector(inspect)

/**
 * Creates an [AdviceWebMvcInspector] from a lambda.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webmvc.AdviceWebMvcInspector
 * import io.github.problem4j.spring.webmvc.adviceWebMvcInspector
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun loggingInspector(): AdviceWebMvcInspector =
 *     adviceWebMvcInspector { ctx, problem, ex, request ->
 *         log.warn("Resolved {} to {}", ex.message, problem.status)
 *     }
 * ```
 *
 * @param inspect observes the resolved [Problem] together with the exception that caused it
 * @return an [AdviceWebMvcInspector] delegating to [inspect]
 * @since 3.1.0
 */
public fun adviceWebMvcInspector(
    inspect: (ProblemContext, Problem, Exception, WebRequest) -> Unit,
): AdviceWebMvcInspector = AdviceWebMvcInspector { context, problem, ex, _, _, request ->
  inspect(context, problem, ex, request)
}

/**
 * Creates an [AdviceWebMvcInspector] from a lambda.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webmvc.AdviceWebMvcInspector
 * import io.github.problem4j.spring.webmvc.adviceWebMvcInspector
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun loggingInspector(): AdviceWebMvcInspector =
 *     adviceWebMvcInspector { problem, ex ->
 *         log.warn("Resolved {} to {}", ex.message, problem.status)
 *     }
 * ```
 *
 * @param inspect observes the resolved [Problem] together with the exception that caused it
 * @return an [AdviceWebMvcInspector] delegating to [inspect]
 * @since 3.1.0
 */
public fun adviceWebMvcInspector(inspect: (Problem, Exception) -> Unit): AdviceWebMvcInspector =
    AdviceWebMvcInspector { _, problem, ex, _, _, _ ->
      inspect(problem, ex)
    }
