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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

/**
 * Handles {@link ErrorResponseException} thrown when a controller or framework component raises an
 * error represented by an {@code ErrorResponse}.
 *
 * <p>This exception is typically used by Spring MVC or WebFlux to signal HTTP errors such as 400,
 * 404, or 500, carrying both an {@link HttpStatusCode} and structured error details.
 *
 * <p>It may be thrown programmatically from application code or internally by Spring when request
 * processing fails and an {@code ErrorResponse} needs to be returned to the client.
 *
 * @see org.springframework.web.ErrorResponse
 */
public class ErrorResponseProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link ErrorResponseProblemResolver} with default problem format. */
  public ErrorResponseProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link ErrorResponseProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public ErrorResponseProblemResolver(ProblemFormat problemFormat) {
    super(ErrorResponseException.class, problemFormat);
  }

  /**
   * Converts the given {@link ErrorResponseException} into a {@link ProblemBuilder} by copying its
   * body fields ({@code type}, {@code title}, {@code detail}, {@code instance}) and status code.
   * Any additional properties present in the underlying {@code ErrorResponse} are added as
   * extensions.
   *
   * <p>The provided {@code status} parameter is ignored in favor of the status contained in the
   * exception. {@code headers} and {@code context} are currently not used but are part of the SPI.
   *
   * <p>Deprecation of {@link ProblemBuilder#extension(java.util.Map)} is ignored, as this library
   * is supposed to work with {@code problem4j-core:1.3.x} (any version from {@code 1.3.x}
   * generation).
   *
   * <p>TODO: resolve deprecation while releasing {@code problem4j-spring:2.2.0}
   *
   * @param context problem context (unused)
   * @param ex the {@link ErrorResponseException} to convert
   * @param headers HTTP response headers (unused)
   * @param status suggested status from caller (ignored)
   * @return a builder representing the problem described by the exception
   * @see org.springframework.web.ErrorResponse
   */
  @Override
  @SuppressWarnings("deprecation")
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ErrorResponseException e = (ErrorResponseException) ex;
    ProblemBuilder builder =
        Problem.builder()
            .type(e.getBody().getType())
            .title(e.getBody().getTitle())
            .status(e.getStatusCode().value())
            .detail(e.getBody().getDetail())
            .instance(e.getBody().getInstance());

    if (e.getBody().getProperties() != null) {
      builder = builder.extension(e.getBody().getProperties());
    }

    return builder;
  }
}
