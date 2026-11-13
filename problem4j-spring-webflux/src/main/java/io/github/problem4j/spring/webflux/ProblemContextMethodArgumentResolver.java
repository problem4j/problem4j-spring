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
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.SyncHandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Resolves controller method arguments of {@link ProblemContext} type, without any annotation.
 *
 * <p>Resolves to the {@link ProblemContext} assigned to the current exchange by {@link
 * ProblemContextWebFilter}, the same one used for creating {@code Problem} responses. If none is
 * assigned (e.g. the filter is disabled), resolves to {@code null}.
 *
 * <p>Parameters declared as {@link ProblemContext}, {@code Optional<ProblemContext>} or {@code
 * Mono<ProblemContext>} are supported. The wrappers resolve to an empty {@link Optional} or an
 * empty {@link Mono} if no {@link ProblemContext} is assigned.
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
public class ProblemContextMethodArgumentResolver implements SyncHandlerMethodArgumentResolver {

  /**
   * Creates a new {@code ProblemContextMethodArgumentResolver}.
   *
   * @since 3.1.0
   */
  public ProblemContextMethodArgumentResolver() {}

  /**
   * Checks whether the given parameter is of {@link ProblemContext}, {@code
   * Optional<ProblemContext>} or {@code Mono<ProblemContext>} type.
   *
   * @param parameter the method parameter to check
   * @return {@code true} if the parameter is of {@link ProblemContext}, {@code
   *     Optional<ProblemContext>} or {@code Mono<ProblemContext>} type
   * @since 3.1.0
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    Class<?> type = parameter.getParameterType();
    return (Optional.class.equals(type) || Mono.class.equals(type))
        ? ProblemContext.class.equals(parameter.nested().getNestedParameterType())
        : ProblemContext.class.equals(type);
  }

  /**
   * Resolves the {@link ProblemContext} of the current exchange.
   *
   * @param parameter the method parameter to resolve
   * @param bindingContext the binding context to use
   * @param exchange the current exchange
   * @return the {@link ProblemContext} of the current exchange, or {@code null} if none is
   *     assigned, wrapped in {@link Optional} or {@link Mono} if the parameter is of {@code
   *     Optional<ProblemContext>} or {@code Mono<ProblemContext>} type
   * @since 3.1.0
   */
  @Override
  public @Nullable Object resolveArgumentValue(
      MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
    ProblemContext context =
        exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) instanceof ProblemContext attribute
            ? attribute
            : null;
    Class<?> type = parameter.getParameterType();
    if (Optional.class.equals(type)) {
      return Optional.ofNullable(context);
    }
    if (Mono.class.equals(type)) {
      return Mono.justOrEmpty(context);
    }
    return context;
  }
}
