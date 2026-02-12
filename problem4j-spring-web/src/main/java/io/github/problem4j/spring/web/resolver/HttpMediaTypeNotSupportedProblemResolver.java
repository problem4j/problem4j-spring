/*
 * Copyright (c) 2025-2026 Damian Malczewski
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
 */
public class HttpMediaTypeNotSupportedProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link HttpMediaTypeNotSupportedProblemResolver} with default problem format. */
  public HttpMediaTypeNotSupportedProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link HttpMediaTypeNotSupportedProblemResolver} with the specified problem
   * format.
   *
   * @param problemFormat the problem format to use
   */
  public HttpMediaTypeNotSupportedProblemResolver(ProblemFormat problemFormat) {
    super(HttpMediaTypeNotSupportedException.class, problemFormat);
  }

  /**
   * Returns a {@link ProblemBuilder} with status {@link ProblemStatus#UNSUPPORTED_MEDIA_TYPE} (HTTP
   * 415). Other parameters are ignored because the status is mandated by the semantics of {@link
   * HttpMediaTypeNotSupportedException}.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpMediaTypeNotSupportedException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 415 enforced)
   * @return builder pre-populated with 415 status
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.builder().status(ProblemStatus.UNSUPPORTED_MEDIA_TYPE);
  }
}
