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
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.HttpRequestMethodNotSupportedException;

/**
 * Handles {@link HttpRequestMethodNotSupportedException} thrown when a client sends an HTTP request
 * using a method not supported by the target handler.
 *
 * <p>This typically occurs when the request uses a method (e.g., POST, GET, PUT, DELETE) that the
 * controller or endpoint does not allow.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 405 (Method Not Allowed)
 * response, often including the list of supported methods in the {@code Allow} header.
 *
 * <p>Always resolves to a {@link Problem} with status {@link ProblemStatus#METHOD_NOT_ALLOWED}.
 */
public class HttpRequestMethodNotSupportedProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link HttpRequestMethodNotSupportedProblemResolver} with default problem format.
   */
  public HttpRequestMethodNotSupportedProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link HttpRequestMethodNotSupportedProblemResolver} with the specified problem
   * format.
   *
   * @param problemFormat the problem format to use
   */
  public HttpRequestMethodNotSupportedProblemResolver(ProblemFormat problemFormat) {
    super(HttpRequestMethodNotSupportedException.class, problemFormat);
  }

  /**
   * Returns a {@link ProblemBuilder} with {@link ProblemStatus#METHOD_NOT_ALLOWED} (HTTP 405).
   * Other parameters ({@code context}, {@code headers}, {@code status}) are ignored because the
   * status is mandated by the semantics of {@link HttpRequestMethodNotSupportedException}.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpRequestMethodNotSupportedException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 405 enforced)
   * @return builder pre-populated with 405 status
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.builder().status(ProblemStatus.METHOD_NOT_ALLOWED);
  }
}
