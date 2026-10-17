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
import io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE
import io.github.problem4j.spring.web.ProblemContextSettings
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST
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

/**
 * DSL for customizing a [ProblemContextWebMvcFilter] without subclassing it.
 *
 * @see problemContextWebMvcFilter
 * @since 3.1.0
 */
public interface ProblemContextWebMvcFilterSpec {

  /**
   * Replaces the default trace identifier generation (random UUID without dashes). Used only when
   * no trace identifier is found in request attributes or in the configured tracing header.
   *
   * @param create generates a new trace identifier
   * @since 3.1.0
   */
  public fun createTraceId(create: (HttpServletRequest, HttpServletResponse) -> String)

  /**
   * Registers a callback invoked after the [ProblemContext] is assigned to request attributes and
   * before the rest of the filter chain is processed. Callbacks run in registration order.
   *
   * @param callback receives the request, response and the assigned [ProblemContext]
   * @since 3.1.0
   */
  public fun onContext(callback: (HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit)

  /**
   * Registers a callback invoked after the rest of the filter chain is processed, regardless of
   * whether it completed normally or threw. Useful for cleaning up state set up in [onContext]
   * (e.g. MDC entries). Callbacks run in registration order.
   *
   * @param callback receives the request, response and the assigned [ProblemContext]
   * @since 3.1.0
   */
  public fun onComplete(callback: (HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit)
}

/**
 * Creates a [ProblemContextWebMvcFilter] customized with a [ProblemContextWebMvcFilterSpec] DSL.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.ProblemContextSettings
 * import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
 * import io.github.problem4j.spring.webmvc.problemContextWebMvcFilter
 * import org.slf4j.MDC
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun problemContextWebMvcFilter(settings: ProblemContextSettings): ProblemContextWebMvcFilter =
 *     problemContextWebMvcFilter(settings) {
 *         onContext { _, _, context -> MDC.put("traceId", context["traceId"]) }
 *         onComplete { _, _, _ -> MDC.remove("traceId") }
 *     }
 * ```
 *
 * @param settings the context settings to use
 * @param configure customizes the filter
 * @return a [ProblemContextWebMvcFilter] applying the configured customizations
 * @since 3.1.0
 */
public fun problemContextWebMvcFilter(
    settings: ProblemContextSettings,
    configure: ProblemContextWebMvcFilterSpec.() -> Unit,
): ProblemContextWebMvcFilter {
  val spec = ProblemContextWebMvcFilterSpecImpl().apply(configure)
  return DslProblemContextWebMvcFilter(
      settings,
      spec.traceIdFactory,
      spec.contextCallbacks.toList(),
      spec.completeCallbacks.toList(),
  )
}

private class ProblemContextWebMvcFilterSpecImpl : ProblemContextWebMvcFilterSpec {

  var traceIdFactory: ((HttpServletRequest, HttpServletResponse) -> String)? = null
  val contextCallbacks =
      mutableListOf<(HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit>()
  val completeCallbacks =
      mutableListOf<(HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit>()

  override fun createTraceId(create: (HttpServletRequest, HttpServletResponse) -> String) {
    traceIdFactory = create
  }

  override fun onContext(
      callback: (HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit,
  ) {
    contextCallbacks.add(callback)
  }

  override fun onComplete(
      callback: (HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit,
  ) {
    completeCallbacks.add(callback)
  }
}

private class DslProblemContextWebMvcFilter(
    settings: ProblemContextSettings,
    private val traceIdFactory: ((HttpServletRequest, HttpServletResponse) -> String)?,
    private val contextCallbacks:
        List<(HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit>,
    private val completeCallbacks:
        List<(HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit>,
) : ProblemContextWebMvcFilter(settings) {

  override fun doFilterInternal(
      request: HttpServletRequest,
      response: HttpServletResponse,
      filterChain: FilterChain,
  ) {
    try {
      super.doFilterInternal(request, response, filterChain)
    } finally {
      // absent only if building the context failed, in which case onContext never ran either
      val context = request.problemContext
      if (context != null) {
        completeCallbacks.forEach { it(request, response, context) }
      }
    }
  }

  override fun createNewTraceId(
      request: HttpServletRequest,
      response: HttpServletResponse,
  ): String = traceIdFactory?.invoke(request, response) ?: super.createNewTraceId(request, response)

  override fun assignContextAttributes(
      request: HttpServletRequest,
      response: HttpServletResponse,
      context: ProblemContext,
  ) {
    super.assignContextAttributes(request, response, context)
    contextCallbacks.forEach { it(request, response, context) }
  }
}

/**
 * The [ProblemContext] assigned to this request by [ProblemContextWebMvcFilter], or `null` if none
 * is assigned (e.g. the filter is disabled or has not processed this request yet).
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webmvc.problemContext
 * import jakarta.servlet.http.HttpServletRequest
 * import org.springframework.web.bind.annotation.GetMapping
 * import org.springframework.web.bind.annotation.RestController
 *
 * @RestController
 * class OrderController {
 *
 *     @GetMapping("/orders")
 *     fun orders(request: HttpServletRequest): List<Order> {
 *         log.info("Listing orders [traceId={}]", request.problemContext?.get("traceId"))
 *         return orderService.findAll()
 *     }
 * }
 * ```
 *
 * @since 3.1.0
 */
public val HttpServletRequest.problemContext: ProblemContext?
  get() = getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) as? ProblemContext

/**
 * The [ProblemContext] assigned to this request by [ProblemContextWebMvcFilter], or `null` if none
 * is assigned (e.g. the filter is disabled or has not processed this request yet).
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webmvc.problemContext
 * import org.springframework.web.bind.annotation.ControllerAdvice
 * import org.springframework.web.bind.annotation.ExceptionHandler
 * import org.springframework.web.context.request.WebRequest
 *
 * @ControllerAdvice
 * class ReportingAdvice {
 *
 *     @ExceptionHandler(ReportGenerationException::class)
 *     fun handle(ex: ReportGenerationException, request: WebRequest): ResponseEntity<Any> {
 *         alerting.notify(ex, request.problemContext?.get("traceId"))
 *         return ResponseEntity.internalServerError().build()
 *     }
 * }
 * ```
 *
 * @since 3.1.0
 */
public val WebRequest.problemContext: ProblemContext?
  get() = getAttribute(PROBLEM_CONTEXT_ATTRIBUTE, SCOPE_REQUEST) as? ProblemContext
