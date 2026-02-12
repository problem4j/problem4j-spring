/*
 * Copyright (c) 2025-2026 Damian Malczewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.ProblemSupport.ERRORS_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.VALIDATION_FAILED_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.resolveStatus;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.parameter.DefaultMethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
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
 */
public class HandlerMethodValidationProblemResolver extends AbstractProblemResolver {

  private final MethodValidationResultSupport methodValidationResultSupport;

  /**
   * Creates a new {@link HandlerMethodValidationProblemResolver} with the default problem format
   * and default method validation result support.
   */
  public HandlerMethodValidationProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link HandlerMethodValidationProblemResolver} with the specified problem format
   * and default method validation result support.
   *
   * @param problemFormat the problem format to use
   */
  public HandlerMethodValidationProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new DefaultMethodValidationResultSupport());
  }

  /**
   * Creates a new {@link HandlerMethodValidationProblemResolver} with the specified problem format
   * and method validation result support.
   *
   * @param problemFormat the problem format to use
   * @param methodValidationResultSupport the support for extracting validation results
   */
  public HandlerMethodValidationProblemResolver(
      ProblemFormat problemFormat, MethodValidationResultSupport methodValidationResultSupport) {
    super(HandlerMethodValidationException.class, problemFormat);
    this.methodValidationResultSupport = methodValidationResultSupport;
  }

  /**
   * Builds a {@link ProblemBuilder} for a {@link HandlerMethodValidationException}. If the provided
   * status is 5xx, returns a minimal problem with that status only. Otherwise, includes validation
   * violations collected by {@link MethodValidationResultSupport} and preserves the caller-provided
   * status.
   *
   * @param context problem context (unused for method validation aggregation)
   * @param ex the thrown validation exception (must be {@link HandlerMethodValidationException})
   * @param headers HTTP headers (unused)
   * @param status suggested HTTP status from caller (controls 4xx vs 5xx branch)
   * @return builder representing validation failure (4xx) or minimal error (5xx)
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    HandlerMethodValidationException e = (HandlerMethodValidationException) ex;
    if (status.is5xxServerError()) {
      return Problem.builder().status(resolveStatus(status));
    }
    return Problem.builder()
        .status(resolveStatus(status))
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(ERRORS_EXTENSION, methodValidationResultSupport.fetchViolations(e));
  }
}
