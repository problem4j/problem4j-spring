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
import io.github.problem4j.spring.web.IdentityProblemFormat;
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
 */
public class ConstraintViolationProblemResolver extends AbstractProblemResolver {

  /**
   * Constructs a new {@link ConstraintViolationProblemResolver} with the default problem format.
   */
  public ConstraintViolationProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Constructs a new {@link ConstraintViolationProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public ConstraintViolationProblemResolver(ProblemFormat problemFormat) {
    super(ConstraintViolationException.class, problemFormat);
  }

  /**
   * Builds a {@link ProblemBuilder} with {@link HttpStatus#BAD_REQUEST}, a formatted {@code
   * detail}, and an {@code errors} extension listing each constraint violation (property and
   * message) extracted from the exception.
   *
   * @param context problem context (ignored)
   * @param ex the thrown {@link ConstraintViolationException}
   * @param headers HTTP headers (unused here)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return populated problem builder
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ConstraintViolationException e = (ConstraintViolationException) ex;
    List<Violation> errors = extractViolations(e);

    return Problem.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(ERRORS_EXTENSION, errors);
  }

  /**
   * Converts each {@link ConstraintViolation} into a {@link Violation} capturing the leaf property
   * name and its validation message.
   */
  private List<Violation> extractViolations(ConstraintViolationException e) {
    return e.getConstraintViolations().stream()
        .map(violation -> new Violation(fetchViolationProperty(violation), violation.getMessage()))
        .toList();
  }

  /**
   * Returns the simple (leaf) property name from a violation's {@link Path}. If the path or its
   * terminal node name is absent, returns an empty string.
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
