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

import static io.github.problem4j.spring.web.ProblemSupport.VALIDATION_FAILED_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemSupport;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.DefaultBindingResultSupport;
import org.springframework.http.HttpHeaders;
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
 */
public class WebExchangeBindProblemResolver extends AbstractProblemResolver {

  private final BindingResultSupport bindingResultSupport;

  /** Creates a new {@link WebExchangeBindProblemResolver} with default problem format. */
  public WebExchangeBindProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link WebExchangeBindProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
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
   */
  public WebExchangeBindProblemResolver(
      ProblemFormat problemFormat, BindingResultSupport bindingResultSupport) {
    super(WebExchangeBindException.class, problemFormat);
    this.bindingResultSupport = bindingResultSupport;
  }

  /**
   * Converts the {@link WebExchangeBindException} into a {@link ProblemBuilder} with status {@link
   * ProblemStatus#BAD_REQUEST} and an {@code errors} extension listing field/global validation
   * violations extracted from its {@code BindingResult}.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link WebExchangeBindException}
   * @param headers HTTP headers (unused)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return builder populated with validation detail and violations
   * @see org.springframework.validation.BindingResult
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    WebExchangeBindException e = (WebExchangeBindException) ex;
    return Problem.builder()
        .status(ProblemStatus.BAD_REQUEST)
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(
            ProblemSupport.ERRORS_EXTENSION,
            bindingResultSupport.fetchViolations(e.getBindingResult()));
  }
}
