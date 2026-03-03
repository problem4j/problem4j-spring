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
 */
public class HttpMediaTypeNotAcceptableProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link HttpMediaTypeNotAcceptableProblemResolver} with default problem format.
   */
  public HttpMediaTypeNotAcceptableProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link HttpMediaTypeNotAcceptableProblemResolver} with the specified problem
   * format.
   *
   * @param problemFormat the problem format to use
   */
  public HttpMediaTypeNotAcceptableProblemResolver(ProblemFormat problemFormat) {
    super(HttpMediaTypeNotAcceptableException.class, problemFormat);
  }

  /**
   * Returns a {@link ProblemBuilder} with {@link HttpStatus#NOT_ACCEPTABLE} (HTTP 406). Other
   * parameters ({@code context}, {@code headers}, {@code status}) are ignored because the status is
   * dictated by the semantics of {@link HttpMediaTypeNotAcceptableException}.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpMediaTypeNotAcceptableException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 406 enforced)
   * @return builder pre-populated with 406 status
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.builder().status(HttpStatus.NOT_ACCEPTABLE.value());
  }
}
