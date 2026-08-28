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

package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.parameter.ViolationSupport.ERRORS_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.VALIDATION_FAILED_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.parameter.DefaultMethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupportAware;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Resolves {@link HandlerMethodValidationException} (Spring's aggregated method validation errors)
 * into a {@link Problem} representation.
 *
 * <p>For 4xx statuses it produces a validation problem containing an {@code errors} extension with
 * parameter violations (via {@link MethodValidationResultSupport}). For 5xx statuses it returns
 * only a basic problem with the resolved status, avoiding leaking validation details when the
 * server indicates an internal failure.
 *
 * <p>When used as a Spring bean, in addition to the {@link ProblemFormat} injected via {@link
 * AbstractProblemResolver}, the {@link MethodValidationResultSupport} is assigned after
 * construction by {@link io.github.problem4j.spring.web.config.ProblemBeanPostProcessor
 * ProblemBeanPostProcessor} through {@link
 * #setMethodValidationResultSupport(MethodValidationResultSupport)}.
 *
 * @since 1.2.0
 */
public class HandlerMethodValidationProblemResolver extends AbstractProblemResolver
    implements MethodValidationResultSupportAware {

  private MethodValidationResultSupport methodValidationResultSupport;

  /**
   * Creates a new {@link HandlerMethodValidationProblemResolver} with the default problem format
   * and default method validation result support.
   *
   * @since 1.2.0
   */
  public HandlerMethodValidationProblemResolver() {
    super(HandlerMethodValidationException.class);
    this.methodValidationResultSupport = new DefaultMethodValidationResultSupport();
  }

  /**
   * Creates a new {@link HandlerMethodValidationProblemResolver} with the specified problem format
   * and default method validation result support.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link
   *     io.github.problem4j.spring.web.config.ProblemBeanPostProcessor ProblemBeanPostProcessor}
   *     now assigns collaborators after construction; use {@link
   *     #HandlerMethodValidationProblemResolver()}
   */
  @Deprecated(since = "3.1.0", forRemoval = true)
  public HandlerMethodValidationProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new DefaultMethodValidationResultSupport());
  }

  /**
   * Creates a new {@link HandlerMethodValidationProblemResolver} with the specified problem format
   * and method validation result support.
   *
   * @param problemFormat the problem format to use
   * @param methodValidationResultSupport the support for extracting validation results
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link
   *     io.github.problem4j.spring.web.config.ProblemBeanPostProcessor ProblemBeanPostProcessor}
   *     now assigns collaborators after construction; use {@link
   *     #HandlerMethodValidationProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public HandlerMethodValidationProblemResolver(
      ProblemFormat problemFormat, MethodValidationResultSupport methodValidationResultSupport) {
    super(HandlerMethodValidationException.class, problemFormat);
    this.methodValidationResultSupport = methodValidationResultSupport;
  }

  /**
   * Replaces the {@link MethodValidationResultSupport} used by this resolver.
   *
   * @param methodValidationResultSupport the method validation result support to use
   * @since 3.1.0
   */
  @Override
  public void setMethodValidationResultSupport(
      MethodValidationResultSupport methodValidationResultSupport) {
    this.methodValidationResultSupport = methodValidationResultSupport;
  }

  /**
   * Returns a {@link Problem} for a {@link HandlerMethodValidationException}. If the provided
   * status is 5xx, returns a minimal problem with that status only. Otherwise, includes validation
   * violations collected by {@link MethodValidationResultSupport} and preserves the caller-provided
   * status.
   *
   * @param context problem context (unused for method validation aggregation)
   * @param ex the thrown validation exception (must be {@link HandlerMethodValidationException})
   * @param headers HTTP headers (unused)
   * @param status suggested HTTP status from caller (controls 4xx vs 5xx branch)
   * @return problem representing validation failure (4xx) or minimal error (5xx)
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    HandlerMethodValidationException e = (HandlerMethodValidationException) ex;
    if (status.is5xxServerError()) {
      return Problem.of(status.value());
    }
    return Problem.builder()
        .status(status.value())
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(ERRORS_EXTENSION, methodValidationResultSupport.fetchViolations(e))
        .build();
  }
}
