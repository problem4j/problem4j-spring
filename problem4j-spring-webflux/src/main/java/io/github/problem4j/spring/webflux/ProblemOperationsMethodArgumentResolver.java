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

import io.github.problem4j.spring.web.support.ProblemOperations;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.SyncHandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Resolves controller method arguments of {@link ProblemOperations} type, without any annotation.
 *
 * <p>Resolves to the {@link ProblemOperations} used for handling exceptions, so that a controller
 * can build {@code Problem}s the same way as exception advices do. A parameter declared as a
 * subtype (e.g. {@code ProblemTemplate}) is supported as long as the {@link ProblemOperations} in
 * use is an instance of it. Parameters wrapped in {@link Optional} or {@link Mono} are supported
 * too.
 *
 * <pre>{@code
 * @GetMapping("/orders/{id}")
 * public Mono<Order> getOrder(@PathVariable String id, ProblemOperations problemOperations) {
 *   // ...
 * }
 * }</pre>
 *
 * @since 3.2.0
 */
public class ProblemOperationsMethodArgumentResolver implements SyncHandlerMethodArgumentResolver {

  private final ProblemOperations problemOperations;

  /**
   * Creates a new {@code ProblemOperationsMethodArgumentResolver}.
   *
   * @param problemOperations the {@link ProblemOperations} to resolve arguments to
   * @since 3.2.0
   */
  public ProblemOperationsMethodArgumentResolver(ProblemOperations problemOperations) {
    this.problemOperations = problemOperations;
  }

  /**
   * Checks whether the given parameter is of {@link ProblemOperations} type, or its subtype that
   * the {@link ProblemOperations} in use is an instance of.
   *
   * @param parameter the method parameter to check
   * @return {@code true} if the parameter can be resolved to the {@link ProblemOperations} in use
   * @since 3.2.0
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    Class<?> type = parameter.getParameterType();
    if (Optional.class.equals(type) || Mono.class.equals(type)) {
      type = parameter.nested().getNestedParameterType();
    }
    return ProblemOperations.class.isAssignableFrom(type) && type.isInstance(problemOperations);
  }

  /**
   * Resolves the {@link ProblemOperations} in use.
   *
   * @param parameter the method parameter to resolve
   * @param bindingContext the binding context to use
   * @param exchange the current exchange
   * @return the {@link ProblemOperations} in use, wrapped in {@link Optional} or {@link Mono} if
   *     the parameter is of {@code Optional} or {@code Mono} type
   * @since 3.2.0
   */
  @Override
  public Object resolveArgumentValue(
      MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
    Class<?> type = parameter.getParameterType();
    if (Optional.class.equals(type)) {
      return Optional.of(problemOperations);
    }
    if (Mono.class.equals(type)) {
      return Mono.just(problemOperations);
    }
    return problemOperations;
  }
}
