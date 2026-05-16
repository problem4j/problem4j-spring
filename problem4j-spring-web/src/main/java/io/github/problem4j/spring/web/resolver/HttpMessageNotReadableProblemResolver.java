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
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Handles {@link HttpMessageNotReadableException} thrown when an HTTP request body cannot be read
 * or parsed.
 *
 * <p>This typically occurs for requests with malformed JSON, XML, or other payloads that cannot be
 * converted by the configured {@code HttpMessageConverter}s.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response to
 * indicate that the request body is invalid or unreadable.
 *
 * @see org.springframework.http.converter.HttpMessageConverter
 * @since 1.2.0
 */
public class HttpMessageNotReadableProblemResolver extends AbstractProblemResolver {

  private final JacksonErrorHelper jacksonErrorHelper;

  /**
   * Creates a new {@link HttpMessageNotReadableProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public HttpMessageNotReadableProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link HttpMessageNotReadableProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public HttpMessageNotReadableProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new SimpleTypeNameMapper());
  }

  /**
   * Creates a new {@link HttpMessageNotReadableProblemResolver} with the specified problem format
   * and type name mapper.
   *
   * @param problemFormat the problem format to use
   * @param typeNameMapper the type mapper to use
   * @since 1.2.0
   */
  public HttpMessageNotReadableProblemResolver(
      ProblemFormat problemFormat, TypeNameMapper typeNameMapper) {
    super(HttpMessageNotReadableException.class, problemFormat);
    this.jacksonErrorHelper = new JacksonErrorHelper(problemFormat, typeNameMapper);
  }

  /**
   * Returns a {@link Problem} with {@code 400 Bad Request}. Other parameters ({@code context},
   * {@code headers}, {@code status}) are ignored because a malformed or unreadable request body
   * always maps to a client error.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpMessageNotReadableException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 400 enforced)
   * @return problem with 400 status
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    if (ex.getCause() instanceof MismatchedInputException e) {
      return jacksonErrorHelper.resolveMismatchedInput(e);
    }
    return BAD_REQUEST;
  }
}
