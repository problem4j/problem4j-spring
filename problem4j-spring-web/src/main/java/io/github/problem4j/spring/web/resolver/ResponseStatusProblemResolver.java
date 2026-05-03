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
import io.github.problem4j.core.ProblemContext;
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
 *
 * @since 1.2.0
 */
public class ResponseStatusProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link ResponseStatusProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public ResponseStatusProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link ResponseStatusProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public ResponseStatusProblemResolver(ProblemFormat problemFormat) {
    super(ResponseStatusException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} reflecting {@link ResponseStatusException} and the HTTP status
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
   * @return problem with the exception's status code
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ResponseStatusException e = (ResponseStatusException) ex;
    return Problem.of(e.getStatusCode().value());
  }
}
