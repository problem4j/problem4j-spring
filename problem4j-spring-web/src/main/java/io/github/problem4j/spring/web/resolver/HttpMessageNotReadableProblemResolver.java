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
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
 */
public class HttpMessageNotReadableProblemResolver extends AbstractProblemResolver {

  private final JacksonErrorHelper jacksonErrorHelper;

  /** Creates a new {@link HttpMessageNotReadableProblemResolver} with default problem format. */
  public HttpMessageNotReadableProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link HttpMessageNotReadableProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
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
   */
  public HttpMessageNotReadableProblemResolver(
      ProblemFormat problemFormat, TypeNameMapper typeNameMapper) {
    super(HttpMessageNotReadableException.class, problemFormat);
    this.jacksonErrorHelper = new JacksonErrorHelper(problemFormat, typeNameMapper);
  }

  /**
   * Returns a {@link ProblemBuilder} with {@link HttpStatus#BAD_REQUEST} (HTTP 400). Other
   * parameters ({@code context}, {@code headers}, {@code status}) are ignored because a malformed
   * or unreadable request body always maps to a client error.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpMessageNotReadableException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 400 enforced)
   * @return builder pre-populated with 400 status
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    if (ex.getCause() instanceof MismatchedInputException e) {
      return jacksonErrorHelper.resolveMismatchedInput(e);
    }
    return Problem.builder().status(HttpStatus.BAD_REQUEST.value());
  }
}
