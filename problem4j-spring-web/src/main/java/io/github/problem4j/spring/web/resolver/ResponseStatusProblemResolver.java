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

import static io.github.problem4j.spring.web.ProblemSupport.resolveStatus;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

/**
 * Handles {@link ResponseStatusException} thrown to signal a specific HTTP status and optional
 * reason from application code or framework components.
 *
 * <p>This exception can be thrown directly in controllers, services, or other layers to indicate
 * errors such as 404 (Not Found), 403 (Forbidden), or 500 (Internal Server Error) without relying
 * on checked exceptions or custom error types.
 *
 * <p>The handler is responsible for translating the exception into the corresponding HTTP response
 * with the specified status code, reason, and any additional details.
 */
public class ResponseStatusProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link ResponseStatusProblemResolver} with default problem format. */
  public ResponseStatusProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link ResponseStatusProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public ResponseStatusProblemResolver(ProblemFormat problemFormat) {
    super(ResponseStatusException.class, problemFormat);
  }

  /**
   * Builds a {@link ProblemBuilder} reflecting {@link ResponseStatusException} and the HTTP status
   * carried by it. Ignores provided {@code status}, {@code headers}, and {@code context}; the
   * resolver always uses {@link ResponseStatusException#getStatusCode()}.
   *
   * <p>The exception's reason/message is intentionally not propagated here (can be added by a
   * custom subclass if desired) to avoid leaking internal details unless explicitly configured.
   *
   * @param context problem context (unused)
   * @param ex the {@link ResponseStatusException} to convert
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored)
   * @return builder pre-populated with the exception's status code
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ResponseStatusException e = (ResponseStatusException) ex;
    return Problem.builder().status(resolveStatus(e.getStatusCode()));
  }
}
