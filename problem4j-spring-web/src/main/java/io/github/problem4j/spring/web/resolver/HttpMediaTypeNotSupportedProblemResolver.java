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
import org.springframework.http.HttpStatusCode;
import org.springframework.web.HttpMediaTypeNotSupportedException;

/**
 * Handles {@link HttpMediaTypeNotSupportedException} thrown when a client sends a request with a
 * media type that the server cannot consume.
 *
 * <p>This typically occurs when the {@code Content-Type} header in the HTTP request does not match
 * any of the media types supported by the controller method or configured message converters.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 415 (Unsupported Media Type)
 * response to inform the client that the submitted content type is not supported.
 *
 * @since 1.2.0
 */
public class HttpMediaTypeNotSupportedProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link HttpMediaTypeNotSupportedProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public HttpMediaTypeNotSupportedProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link HttpMediaTypeNotSupportedProblemResolver} with the specified problem
   * format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public HttpMediaTypeNotSupportedProblemResolver(ProblemFormat problemFormat) {
    super(HttpMediaTypeNotSupportedException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} with {@code 415 Unsupported Media Type}. Other parameters are ignored
   * because the status is mandated by the semantics of {@link HttpMediaTypeNotSupportedException}.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpMediaTypeNotSupportedException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 415 enforced)
   * @return problem with 415 status
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return UNSUPPORTED_MEDIA_TYPE;
  }
}
