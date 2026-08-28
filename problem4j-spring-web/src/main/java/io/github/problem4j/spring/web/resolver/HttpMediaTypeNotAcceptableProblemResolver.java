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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

/**
 * Handles {@link HttpMediaTypeNotAcceptableException} thrown when a client requests a response
 * media type that the server cannot produce.
 *
 * <p>This typically occurs when the {@code Accept} header in the HTTP request does not match any of
 * the media types supported by the controller method or configured message converters.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 406 (Not Acceptable) response to
 * inform the client that the requested content type is not available.
 *
 * @since 1.2.0
 */
public class HttpMediaTypeNotAcceptableProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link HttpMediaTypeNotAcceptableProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public HttpMediaTypeNotAcceptableProblemResolver() {
    super(HttpMediaTypeNotAcceptableException.class);
  }

  /**
   * Creates a new {@link HttpMediaTypeNotAcceptableProblemResolver} with the specified problem
   * format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link
   *     io.github.problem4j.spring.web.config.DefaultProblemBeanPostProcessor
   *     ProblemBeanPostProcessor} now assigns the {@link ProblemFormat} after construction; use
   *     {@link #HttpMediaTypeNotAcceptableProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public HttpMediaTypeNotAcceptableProblemResolver(ProblemFormat problemFormat) {
    super(HttpMediaTypeNotAcceptableException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} with {@link HttpStatus#NOT_ACCEPTABLE} (HTTP 406). Other parameters
   * ({@code context}, {@code headers}, {@code status}) are ignored because the status is dictated
   * by the semantics of {@link HttpMediaTypeNotAcceptableException}.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpMediaTypeNotAcceptableException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 406 enforced)
   * @return problem with 406 status
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.of(HttpStatus.NOT_ACCEPTABLE.value());
  }
}
