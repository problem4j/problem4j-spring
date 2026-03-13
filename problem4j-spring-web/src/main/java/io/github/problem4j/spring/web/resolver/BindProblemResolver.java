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

import static io.github.problem4j.spring.web.ProblemSupport.VALIDATION_FAILED_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemSupport;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.DefaultBindingResultSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindException;

/**
 * Due to {@link BindException} being subclassed by {@code MethodArgumentNotValidException}, this
 * implementation also covers that exceptions.
 *
 * <p>Quite obvious message, but worth to note that the only reason {@link BindProblemResolver} is
 * kept is due to backwards compatibility as Problem4J doesn't use any fields from that subclass at
 * the moment.
 *
 * <p>These exceptions indicate that incoming request parameters or bodies could not be bound to
 * target objects or did not pass validation constraints.
 *
 * <ul>
 *   <li>{@link BindException} - thrown for binding or validation errors on form or query
 *       parameters.
 *   <li>{@code MethodArgumentNotValidException} - thrown for validation failures on
 *       {@code @RequestBody} or {@code @ModelAttribute} method arguments.
 * </ul>
 *
 * @see org.springframework.web.bind.MethodArgumentNotValidException
 * @see org.springframework.web.bind.annotation.ModelAttribute
 * @see org.springframework.web.bind.annotation.RequestBody
 */
public class BindProblemResolver extends AbstractProblemResolver {

  private final BindingResultSupport bindingResultSupport;

  /**
   * Constructs a new {@link BindProblemResolver} with the default problem format and binding result
   * support.
   */
  public BindProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Constructs a new {@link BindProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public BindProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new DefaultBindingResultSupport());
  }

  /**
   * Constructs a new {@link BindProblemResolver} with the specified problem format and binding
   * result support.
   *
   * @param problemFormat the problem format to use
   * @param bindingResultSupport the binding result support to use
   */
  public BindProblemResolver(
      ProblemFormat problemFormat, BindingResultSupport bindingResultSupport) {
    super(BindException.class, problemFormat);
    this.bindingResultSupport = bindingResultSupport;
  }

  /**
   * Resolves a {@link BindException} (or subclass) to a {@link ProblemBuilder} with {@link
   * HttpStatus#BAD_REQUEST} and an {@code errors} extension listing field/global validation
   * violations produced by the underlying {@link BindException#getBindingResult()}.
   *
   * @param context problem context (ignored for binding violations)
   * @param ex the binding / validation exception (must be a {@link BindException})
   * @param headers HTTP response headers (unused here but part of the SPI)
   * @param status suggested HTTP status from caller (ignored; BAD_REQUEST is enforced)
   * @return builder pre-populated with validation detail and violations
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    BindException e = (BindException) ex;
    return Problem.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(
            ProblemSupport.ERRORS_EXTENSION,
            bindingResultSupport.fetchViolations(e.getBindingResult()));
  }
}
