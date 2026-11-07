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
import io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST
import org.springframework.web.context.request.WebRequest

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
