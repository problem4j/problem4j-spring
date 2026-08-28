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
import io.github.problem4j.spring.web.parameter.Violation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Handles {@link ConstraintViolationException} thrown when one or more Bean Validation constraints
 * are violated.
 *
 * <p>Relates with {@code MethodValidationException} (see {@link MethodValidationProblemResolver}).
 *
 * <p>This exception indicates that method parameters, return values, or other validated elements
 * failed to satisfy declared {@code @Valid} or {@code @Constraint} annotations.
 *
 * @see org.springframework.validation.method.MethodValidationException
 * @since 1.2.0
 */
public class ConstraintViolationProblemResolver extends AbstractProblemResolver {

  /**
   * Constructs a new {@link ConstraintViolationProblemResolver} with the default problem format.
   *
   * @since 1.2.0
   */
  public ConstraintViolationProblemResolver() {
    super(ConstraintViolationException.class);
  }

  /**
   * Constructs a new {@link ConstraintViolationProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link
   *     io.github.problem4j.spring.web.config.DefaultProblemBeanPostProcessor
   *     ProblemBeanPostProcessor} now assigns the {@link ProblemFormat} after construction; use
   *     {@link #ConstraintViolationProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public ConstraintViolationProblemResolver(ProblemFormat problemFormat) {
    super(ConstraintViolationException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} with {@link HttpStatus#BAD_REQUEST}, a formatted {@code detail}, and
   * an {@code errors} extension listing each constraint violation (property and message) extracted
   * from the exception.
   *
   * @param context problem context (ignored)
   * @param ex the thrown {@link ConstraintViolationException}
   * @param headers HTTP headers (unused here)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return problem representing validation failure
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ConstraintViolationException e = (ConstraintViolationException) ex;
    List<Violation> errors = extractViolations(e);

    return Problem.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(ERRORS_EXTENSION, errors)
        .build();
  }

  /**
   * Converts each {@link ConstraintViolation} into a {@link Violation} capturing the leaf property
   * name and its validation message.
   *
   * @since 1.2.0
   */
  private List<Violation> extractViolations(ConstraintViolationException e) {
    return e.getConstraintViolations().stream()
        .map(violation -> new Violation(fetchViolationProperty(violation), violation.getMessage()))
        .toList();
  }

  /**
   * Returns the simple (leaf) property name from a violation's {@link Path}. If the path or its
   * terminal node name is absent, returns an empty string.
   *
   * @since 1.2.0
   */
  private String fetchViolationProperty(ConstraintViolation<?> violation) {
    if (violation.getPropertyPath() == null) {
      return "";
    }

    String lastElement = null;
    for (Path.Node node : violation.getPropertyPath()) {
      lastElement = node.getName();
    }

    return lastElement != null ? lastElement : "";
  }
}
