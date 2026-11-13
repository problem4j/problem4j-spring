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

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;

import io.github.problem4j.core.ProblemContext;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves controller method arguments of {@link ProblemContext} type, without any annotation.
 *
 * <p>Resolves to the {@link ProblemContext} assigned to the current request by {@link
 * ProblemContextWebMvcFilter}, the same one used for creating {@code Problem} responses. If none is
 * assigned (e.g. the filter is disabled), resolves to {@code null}.
 *
 * <p>Parameters declared as {@link ProblemContext} or {@code Optional<ProblemContext>} are
 * supported. The latter resolves to an empty {@link Optional} if no {@link ProblemContext} is
 * assigned.
 *
 * <pre>{@code
 * @GetMapping("/orders/{id}")
 * public Order getOrder(@PathVariable String id, ProblemContext context) {
 *   log.info("Looking up order {} [traceId={}]", id, context.get("traceId"));
 *   // ...
 * }
 * }</pre>
 *
 * @since 3.1.0
 */
public class ProblemContextMethodArgumentResolver implements HandlerMethodArgumentResolver {

  /**
   * Creates a new {@code ProblemContextMethodArgumentResolver}.
   *
   * @since 3.1.0
   */
  public ProblemContextMethodArgumentResolver() {}

  /**
   * Checks whether the given parameter is of {@link ProblemContext} or {@code
   * Optional<ProblemContext>} type.
   *
   * @param parameter the method parameter to check
   * @return {@code true} if the parameter is of {@link ProblemContext} or {@code
   *     Optional<ProblemContext>} type
   * @since 3.1.0
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return ProblemContext.class.equals(parameter.nestedIfOptional().getNestedParameterType());
  }

  /**
   * Resolves the {@link ProblemContext} of the current request.
   *
   * @param parameter the method parameter to resolve
   * @param mavContainer the model and view container of the current request
   * @param webRequest the current request
   * @param binderFactory a factory for creating data binder instances
   * @return the {@link ProblemContext} of the current request, or {@code null} if none is assigned,
   *     wrapped in {@link Optional} if the parameter is of {@code Optional<ProblemContext>} type
   * @since 3.1.0
   */
  @Override
  public @Nullable Object resolveArgument(
      MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    ProblemContext context =
        webRequest.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
                instanceof ProblemContext attribute
            ? attribute
            : null;
    return Optional.class.equals(parameter.getParameterType())
        ? Optional.ofNullable(context)
        : context;
  }
}
