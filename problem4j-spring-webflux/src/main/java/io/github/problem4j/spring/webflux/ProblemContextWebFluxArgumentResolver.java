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

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;

import io.github.problem4j.core.ProblemContext;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.SyncHandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

/**
 * Resolves controller method arguments of {@link ProblemContext} type, without any annotation.
 *
 * <p>Resolves to the {@link ProblemContext} assigned to the current exchange by {@link
 * ProblemContextWebFluxFilter}, the same one used for creating {@code Problem} responses. If none
 * is assigned (e.g. the filter is disabled), resolves to {@code null}.
 *
 * <p>Only parameters declared exactly as {@link ProblemContext} are supported, wrappers like {@code
 * Optional<ProblemContext>} are not.
 *
 * <pre>{@code
 * @GetMapping("/orders/{id}")
 * public Mono<Order> getOrder(@PathVariable String id, ProblemContext context) {
 *   log.info("Looking up order {} [traceId={}]", id, context.get("traceId"));
 *   // ...
 * }
 * }</pre>
 *
 * @since 3.1.0
 */
public class ProblemContextWebFluxArgumentResolver implements SyncHandlerMethodArgumentResolver {

  /**
   * Creates a new {@code ProblemContextWebFluxArgumentResolver}.
   *
   * @since 3.1.0
   */
  public ProblemContextWebFluxArgumentResolver() {}

  /**
   * Checks whether the given parameter is of {@link ProblemContext} type.
   *
   * @param parameter the method parameter to check
   * @return {@code true} if the parameter is of {@link ProblemContext} type
   * @since 3.1.0
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return ProblemContext.class.equals(parameter.getParameterType());
  }

  /**
   * Resolves the {@link ProblemContext} of the current exchange.
   *
   * @param parameter the method parameter to resolve
   * @param bindingContext the binding context to use
   * @param exchange the current exchange
   * @return the {@link ProblemContext} of the current exchange, or {@code null} if none is assigned
   * @since 3.1.0
   */
  @Override
  public @Nullable Object resolveArgumentValue(
      MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
    return exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) instanceof ProblemContext context
        ? context
        : null;
  }
}
