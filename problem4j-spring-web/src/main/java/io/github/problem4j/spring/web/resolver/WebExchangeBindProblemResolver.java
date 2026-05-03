/*
 * Copyright 2025-2026 The Problem4J Authors
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

import static io.github.problem4j.spring.web.parameter.ViolationSupport.VALIDATION_FAILED_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.DefaultBindingResultSupport;
import io.github.problem4j.spring.web.parameter.ViolationSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * Handles {@link WebExchangeBindException} thrown when binding and validation of request data in a
 * WebFlux application fails.
 *
 * <p>This typically occurs when request parameters, path variables, or body content cannot be bound
 * to a target object or violate validation constraints.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response, often
 * including details about which fields failed binding or validation.
 *
 * @since 1.2.0
 */
public class WebExchangeBindProblemResolver extends AbstractProblemResolver {

  private final BindingResultSupport bindingResultSupport;

  /**
   * Creates a new {@link WebExchangeBindProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public WebExchangeBindProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link WebExchangeBindProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public WebExchangeBindProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new DefaultBindingResultSupport());
  }

  /**
   * Creates a new {@link WebExchangeBindProblemResolver} with the specified problem format and
   * binding result support.
   *
   * @param problemFormat the problem format to use
   * @param bindingResultSupport the support for extracting bind results
   * @since 1.2.0
   */
  public WebExchangeBindProblemResolver(
      ProblemFormat problemFormat, BindingResultSupport bindingResultSupport) {
    super(WebExchangeBindException.class, problemFormat);
    this.bindingResultSupport = bindingResultSupport;
  }

  /**
   * Converts the {@link WebExchangeBindException} into a {@link Problem} with status {@link
   * HttpStatus#BAD_REQUEST} and an {@code errors} extension listing field/global validation
   * violations extracted from its {@code BindingResult}.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link WebExchangeBindException}
   * @param headers HTTP headers (unused)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return problem with validation detail and violations
   * @see org.springframework.validation.BindingResult
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    WebExchangeBindException e = (WebExchangeBindException) ex;
    return Problem.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(
            ViolationSupport.ERRORS_EXTENSION,
            bindingResultSupport.fetchViolations(e.getBindingResult()))
        .build();
  }
}
