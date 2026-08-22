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
import org.springframework.http.HttpStatus;
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
 * <p>Always resolves to a problem with status {@link HttpStatus#BAD_REQUEST} and an {@code errors}
 * extension populated via {@link MethodValidationResultSupport} (one entry per violated parameter /
 * return value).
 *
 * @see jakarta.validation.ConstraintViolationException
 * @since 1.2.0
 */
public class MethodValidationProblemResolver extends AbstractProblemResolver
    implements MethodValidationResultSupportAware {

  private MethodValidationResultSupport methodValidationResultSupport;

  /**
   * Creates a new {@link MethodValidationProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public MethodValidationProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link MethodValidationProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public MethodValidationProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new DefaultMethodValidationResultSupport());
  }

  /**
   * Creates a new {@link MethodValidationProblemResolver} with the specified method validation
   * result support and default problem format.
   *
   * @param methodValidationResultSupport the support for extracting validation results
   * @since 3.1.0
   */
  public MethodValidationProblemResolver(
      MethodValidationResultSupport methodValidationResultSupport) {
    this(ProblemFormat.identity(), methodValidationResultSupport);
  }

  /**
   * Creates a new {@link MethodValidationProblemResolver} with the specified problem format and
   * method validation result support.
   *
   * @param problemFormat the problem format to use
   * @param methodValidationResultSupport the support for extracting validation results
   * @since 1.2.0
   */
  public MethodValidationProblemResolver(
      ProblemFormat problemFormat, MethodValidationResultSupport methodValidationResultSupport) {
    super(MethodValidationException.class, problemFormat);
    this.methodValidationResultSupport = methodValidationResultSupport;
  }

  /**
   * Replaces the {@link MethodValidationResultSupport} used by this resolver.
   *
   * @param methodValidationResultSupport the support for extracting validation results
   * @since 3.1.0
   */
  @Override
  public void setMethodValidationResultSupport(
      MethodValidationResultSupport methodValidationResultSupport) {
    this.methodValidationResultSupport = methodValidationResultSupport;
  }

  /**
   * Returns a {@link Problem} with status {@link HttpStatus#BAD_REQUEST} and an {@code errors}
   * extension describing each parameter or return value violation. Other parameters ({@code
   * context}, {@code headers}, {@code status}) are ignored for status selection; 400 is enforced.
   *
   * @param context problem context (unused)
   * @param ex the thrown {@link MethodValidationException}
   * @param headers HTTP headers (unused)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return problem with validation details and BAD_REQUEST status
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    MethodValidationException e = (MethodValidationException) ex;
    return Problem.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(ERRORS_EXTENSION, methodValidationResultSupport.fetchViolations(e))
        .build();
  }
}
