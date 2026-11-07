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

import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE
import org.springframework.web.server.ServerWebExchange
import reactor.util.context.Context
import reactor.util.context.ContextView

/**
 * The [ProblemContext] assigned to this exchange by [ProblemContextWebFluxFilter], or `null` if
 * none is assigned (e.g. the filter is disabled or has not processed this exchange yet).
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webflux.problemContext
 * import org.springframework.web.bind.annotation.GetMapping
 * import org.springframework.web.bind.annotation.RestController
 * import org.springframework.web.server.ServerWebExchange
 *
 * @RestController
 * class OrderController {
 *
 *     @GetMapping("/orders")
 *     suspend fun orders(exchange: ServerWebExchange): List<Order> {
 *         log.info("Listing orders [traceId={}]", exchange.problemContext?.get("traceId"))
 *         return orderService.findAll()
 *     }
 * }
 * ```
 *
 * @since 3.1.0
 */
public val ServerWebExchange.problemContext: ProblemContext?
  get() = getAttribute<Any>(PROBLEM_CONTEXT_ATTRIBUTE) as? ProblemContext

/**
 * The [ProblemContext] propagated through Reactor [Context] by [ProblemContextWebFluxFilter], or
 * `null` if none is present (e.g. the filter is disabled or this pipeline is not downstream of it).
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.webflux.problemContext
 * import reactor.core.publisher.Mono
 *
 * fun auditOrder(order: Order): Mono<Void> =
 *     Mono.deferContextual { ctx ->
 *         auditClient.send(order.id, ctx.problemContext?.get("traceId"))
 *     }
 * ```
 *
 * @since 3.1.0
 */
public val ContextView.problemContext: ProblemContext?
  get() = getOrDefault<Any>(PROBLEM_CONTEXT_ATTRIBUTE, null) as? ProblemContext
