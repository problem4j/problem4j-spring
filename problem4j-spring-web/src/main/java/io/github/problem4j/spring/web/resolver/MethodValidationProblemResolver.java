/*
 * Copyright (c) 2025-2026 The Problem4J Authors
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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.parameter.DefaultMethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.method.MethodValidationException;

/**
 * Handles {@link MethodValidationException} thrown when method-level Bean Validation fails.
 *
 * <p>This exception is raised for methods annotated with {@code @Validated} or containing
 * {@code @Constraint}-annotated parameters or return values that do not satisfy declared validation
 * rules.
 *
 * <p>When method validation adaptation is enabled (e.g. via {@code @EnableMethodValidation}),
 * Spring intercepts method invocations, delegates to a Bean Validation provider, and wraps any
 * resulting {@code ConstraintViolationException} in a {@link MethodValidationException}.
 *
 * <p>This allows framework components and exception handlers to deal with a consistent,
 * Spring-specific exception type instead of the raw Jakarta exception.
 *
 * <p>Always resolves to a problem with status {@link ProblemStatus#BAD_REQUEST} and an {@code
 * errors} extension populated via {@link MethodValidationResultSupport} (one entry per violated
 * parameter / return value).
 *
 * @see jakarta.validation.ConstraintViolationException
 */
public class MethodValidationProblemResolver extends AbstractProblemResolver {

  private final MethodValidationResultSupport methodValidationResultSupport;

  /** Creates a new {@link MethodValidationProblemResolver} with default problem format. */
  public MethodValidationProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link MethodValidationProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public MethodValidationProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new DefaultMethodValidationResultSupport());
  }

  /**
   * Creates a new {@link MethodValidationProblemResolver} with the specified problem format and
   * method validation result support.
   *
   * @param problemFormat the problem format to use
   * @param methodValidationResultSupport the support for extracting validation results
   */
  public MethodValidationProblemResolver(
      ProblemFormat problemFormat, MethodValidationResultSupport methodValidationResultSupport) {
    super(MethodValidationException.class, problemFormat);
    this.methodValidationResultSupport = methodValidationResultSupport;
  }

  /**
   * Converts the {@link MethodValidationException} into a {@link ProblemBuilder} with status {@link
   * ProblemStatus#BAD_REQUEST} and an {@code errors} extension describing each parameter or return
   * value violation. Other parameters ({@code context}, {@code headers}, {@code status}) are
   * ignored for status selection; 400 is enforced.
   *
   * @param context problem context (unused)
   * @param ex the thrown {@link MethodValidationException}
   * @param headers HTTP headers (unused)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return builder pre-populated with validation details and BAD_REQUEST status
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    MethodValidationException e = (MethodValidationException) ex;
    return Problem.builder()
        .status(ProblemStatus.BAD_REQUEST)
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(ERRORS_EXTENSION, methodValidationResultSupport.fetchViolations(e));
  }
}
