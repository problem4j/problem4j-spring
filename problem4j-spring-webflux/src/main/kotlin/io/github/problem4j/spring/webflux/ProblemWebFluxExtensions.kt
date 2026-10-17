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
import io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE
import io.github.problem4j.spring.web.ProblemContextSettings
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context

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

/**
 * DSL for customizing a [ProblemContextWebFluxFilter] without subclassing it.
 *
 * @see problemContextWebFluxFilter
 * @since 3.1.0
 */
public interface ProblemContextWebFluxFilterSpec {

  /**
   * Replaces the default trace identifier generation (random UUID without dashes). Used only when
   * no trace identifier is found in exchange attributes or in the configured tracing header.
   *
   * @param create generates a new trace identifier
   * @since 3.1.0
   */
  public fun createTraceId(create: (ServerWebExchange) -> String)

  /**
   * Registers a callback invoked after the [ProblemContext] is assigned to exchange attributes and
   * before the rest of the filter chain is subscribed to. Callbacks run in registration order.
   *
   * @param callback receives the exchange and the assigned [ProblemContext]
   * @since 3.1.0
   */
  public fun onContext(callback: (ServerWebExchange, ProblemContext) -> Unit)

  /**
   * Registers a function enriching the Reactor [Context] propagated to the rest of the filter
   * chain, applied after the default problem context and trace ID entries are written. Functions
   * are applied in registration order.
   *
   * @param write returns the updated Reactor [Context]
   * @since 3.1.0
   */
  public fun contextWrite(write: (Context, ServerWebExchange, ProblemContext) -> Context)

  /**
   * Registers a callback invoked when the rest of the filter chain terminates, whether it
   * completed, failed or was cancelled. Callbacks run in registration order.
   *
   * @param callback receives the exchange and the assigned [ProblemContext]
   * @since 3.1.0
   */
  public fun onComplete(callback: (ServerWebExchange, ProblemContext) -> Unit)
}

/**
 * Creates a [ProblemContextWebFluxFilter] customized with a [ProblemContextWebFluxFilterSpec] DSL.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.ProblemContextSettings
 * import io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter
 * import io.github.problem4j.spring.webflux.problemContextWebFluxFilter
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun problemContextWebFluxFilter(settings: ProblemContextSettings): ProblemContextWebFluxFilter =
 *     problemContextWebFluxFilter(settings) {
 *         contextWrite { ctx, exchange, _ -> ctx.put("tenant", exchange.request.headers.getFirst("X-Tenant") ?: "none") }
 *         onComplete { exchange, _ -> log.debug("Completed {}", exchange.request.path) }
 *     }
 * ```
 *
 * @param settings the context settings to use
 * @param configure customizes the filter
 * @return a [ProblemContextWebFluxFilter] applying the configured customizations
 * @since 3.1.0
 */
public fun problemContextWebFluxFilter(
    settings: ProblemContextSettings,
    configure: ProblemContextWebFluxFilterSpec.() -> Unit,
): ProblemContextWebFluxFilter {
  val spec = ProblemContextWebFluxFilterSpecImpl().apply(configure)
  return DslProblemContextWebFluxFilter(
      settings,
      spec.traceIdFactory,
      spec.contextCallbacks.toList(),
      spec.contextWrites.toList(),
      spec.completeCallbacks.toList(),
  )
}

private class ProblemContextWebFluxFilterSpecImpl : ProblemContextWebFluxFilterSpec {

  var traceIdFactory: ((ServerWebExchange) -> String)? = null
  val contextCallbacks = mutableListOf<(ServerWebExchange, ProblemContext) -> Unit>()
  val contextWrites = mutableListOf<(Context, ServerWebExchange, ProblemContext) -> Context>()
  val completeCallbacks = mutableListOf<(ServerWebExchange, ProblemContext) -> Unit>()

  override fun createTraceId(create: (ServerWebExchange) -> String) {
    traceIdFactory = create
  }

  override fun onContext(callback: (ServerWebExchange, ProblemContext) -> Unit) {
    contextCallbacks.add(callback)
  }

  override fun contextWrite(write: (Context, ServerWebExchange, ProblemContext) -> Context) {
    contextWrites.add(write)
  }

  override fun onComplete(callback: (ServerWebExchange, ProblemContext) -> Unit) {
    completeCallbacks.add(callback)
  }
}

private class DslProblemContextWebFluxFilter(
    settings: ProblemContextSettings,
    private val traceIdFactory: ((ServerWebExchange) -> String)?,
    private val contextCallbacks: List<(ServerWebExchange, ProblemContext) -> Unit>,
    private val contextWrites: List<(Context, ServerWebExchange, ProblemContext) -> Context>,
    private val completeCallbacks: List<(ServerWebExchange, ProblemContext) -> Unit>,
) : ProblemContextWebFluxFilter(settings) {

  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    val result = super.filter(exchange, chain)
    val context = exchange.getAttribute<Any>(PROBLEM_CONTEXT_ATTRIBUTE) as? ProblemContext
    if (context == null || completeCallbacks.isEmpty()) {
      return result
    }
    return result.doFinally { completeCallbacks.forEach { it(exchange, context) } }
  }

  override fun createNewTraceId(exchange: ServerWebExchange): String =
      traceIdFactory?.invoke(exchange) ?: super.createNewTraceId(exchange)

  override fun assignContextAttributes(exchange: ServerWebExchange, context: ProblemContext) {
    super.assignContextAttributes(exchange, context)
    contextCallbacks.forEach { it(exchange, context) }
  }

  override fun contextWrite(
      ctx: Context,
      exchange: ServerWebExchange,
      context: ProblemContext,
  ): Context =
      contextWrites.fold(super.contextWrite(ctx, exchange, context)) { acc, write ->
        write(acc, exchange, context)
      }
}
