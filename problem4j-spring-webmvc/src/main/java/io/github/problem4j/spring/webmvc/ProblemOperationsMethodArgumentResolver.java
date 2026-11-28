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

package io.github.problem4j.spring.webmvc;

import io.github.problem4j.spring.web.support.ProblemOperations;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves controller method arguments of {@link ProblemOperations} type, without any annotation.
 *
 * <p>Resolves to the {@link ProblemOperations} used for handling exceptions, so that a controller
 * can build {@code Problem}s the same way as exception advices do. A parameter declared as a
 * subtype (e.g. {@code ProblemTemplate}) is supported as long as the {@link ProblemOperations} in
 * use is an instance of it.
 *
 * <p>The {@link ProblemOperations} is looked up lazily. If none is available, resolves to {@code
 * null}. Parameters declared as {@code Optional<ProblemOperations>} are supported, and resolve to
 * an empty {@link Optional} if none is available.
 *
 * <pre>{@code
 * @GetMapping("/orders/{id}")
 * public Order getOrder(@PathVariable String id, ProblemOperations problemOperations) {
 *   // ...
 * }
 * }</pre>
 *
 * @since 3.2.0
 */
public class ProblemOperationsMethodArgumentResolver implements HandlerMethodArgumentResolver {

  private final ObjectProvider<ProblemOperations> problemOperations;

  /**
   * Creates a new {@code ProblemOperationsMethodArgumentResolver}.
   *
   * @param problemOperations the provider of {@link ProblemOperations} to resolve arguments to
   * @since 3.2.0
   */
  public ProblemOperationsMethodArgumentResolver(
      ObjectProvider<ProblemOperations> problemOperations) {
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
    Class<?> type = parameter.nestedIfOptional().getNestedParameterType();
    if (!ProblemOperations.class.isAssignableFrom(type)) {
      return false;
    }
    ProblemOperations operations = problemOperations.getIfAvailable();
    return operations == null || type.isInstance(operations);
  }

  /**
   * Resolves the {@link ProblemOperations} in use.
   *
   * @param parameter the method parameter to resolve
   * @param mavContainer the model and view container of the current request
   * @param webRequest the current request
   * @param binderFactory a factory for creating data binder instances
   * @return the {@link ProblemOperations} in use, or {@code null} if none is available, wrapped in
   *     {@link Optional} if the parameter is of {@code Optional} type
   * @since 3.2.0
   */
  @Override
  public @Nullable Object resolveArgument(
      MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    ProblemOperations operations = problemOperations.getIfAvailable();
    return Optional.class.equals(parameter.getParameterType())
        ? Optional.ofNullable(operations)
        : operations;
  }
}
