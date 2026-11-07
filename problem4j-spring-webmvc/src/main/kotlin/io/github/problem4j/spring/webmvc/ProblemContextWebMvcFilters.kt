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

import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.ProblemContextSettings
import io.github.problem4j.spring.web.ProblemDsl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * DSL for customizing a [ProblemContextWebMvcFilter] without subclassing it.
 *
 * @see problemContextWebMvcFilter
 * @since 3.1.0
 */
@ProblemDsl
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

private typealias TraceIdFactory = (HttpServletRequest, HttpServletResponse) -> String

private typealias ContextCallback =
    (HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit

private class ProblemContextWebMvcFilterSpecImpl : ProblemContextWebMvcFilterSpec {

  var traceIdFactory: TraceIdFactory? = null
  val contextCallbacks = mutableListOf<ContextCallback>()
  val completeCallbacks = mutableListOf<ContextCallback>()

  override fun createTraceId(create: TraceIdFactory) {
    traceIdFactory = create
  }

  override fun onContext(callback: ContextCallback) {
    contextCallbacks.add(callback)
  }

  override fun onComplete(callback: ContextCallback) {
    completeCallbacks.add(callback)
  }
}

private class DslProblemContextWebMvcFilter(
    settings: ProblemContextSettings,
    private val traceIdFactory: TraceIdFactory?,
    private val contextCallbacks: List<ContextCallback>,
    private val completeCallbacks: List<ContextCallback>,
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
