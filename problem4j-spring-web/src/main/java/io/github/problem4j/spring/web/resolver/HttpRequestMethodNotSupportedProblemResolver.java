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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
 * <p>Always resolves to a {@link Problem} with status {@link HttpStatus#METHOD_NOT_ALLOWED}.
 *
 * @since 1.2.0
 */
public class HttpRequestMethodNotSupportedProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link HttpRequestMethodNotSupportedProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public HttpRequestMethodNotSupportedProblemResolver() {
    super(HttpRequestMethodNotSupportedException.class);
  }

  /**
   * Creates a new {@link HttpRequestMethodNotSupportedProblemResolver} with the specified problem
   * format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns the {@link
   *     ProblemFormat} after construction; use {@link
   *     #HttpRequestMethodNotSupportedProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public HttpRequestMethodNotSupportedProblemResolver(ProblemFormat problemFormat) {
    super(HttpRequestMethodNotSupportedException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} with {@link HttpStatus#METHOD_NOT_ALLOWED} (HTTP 405). Other
   * parameters ({@code context}, {@code headers}, {@code status}) are ignored because the status is
   * mandated by the semantics of {@link HttpRequestMethodNotSupportedException}.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpRequestMethodNotSupportedException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 405 enforced)
   * @return problem with 405 status
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.of(HttpStatus.METHOD_NOT_ALLOWED.value());
  }
}
