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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
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
 * @since 1.2.0
 */
public class ErrorResponseProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link ErrorResponseProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public ErrorResponseProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link ErrorResponseProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public ErrorResponseProblemResolver(ProblemFormat problemFormat) {
    super(ErrorResponseException.class, problemFormat);
  }

  /**
   * Converts the given {@link ErrorResponseException} into a {@link Problem} by copying its body
   * fields ({@code type}, {@code title}, {@code detail}, {@code instance}) and status code. Any
   * additional properties present in the underlying {@code ErrorResponse} are added as extensions.
   *
   * <p>The provided {@code status} parameter is ignored in favor of the status contained in the
   * exception. {@code headers} and {@code context} are currently not used but are part of the SPI.
   *
   * @param context problem context (unused)
   * @param ex the {@link ErrorResponseException} to convert
   * @param headers HTTP response headers (unused)
   * @param status suggested status from caller (ignored)
   * @return problem representing the error described by the exception
   * @see org.springframework.web.ErrorResponse
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
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
      builder = builder.extensions(e.getBody().getProperties());
    }

    return builder.build();
  }
}
