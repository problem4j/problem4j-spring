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

package io.github.problem4j.spring.webmvc.resolver;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.resolver.AbstractProblemResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Handles {@link NoResourceFoundException} thrown when a requested static resource cannot be found
 * in application.
 *
 * <p>This typically occurs when the client requests a URL that is mapped to static resources (e.g.,
 * files under {@code /static} or {@code /public}) but no matching resource exists.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 404 (Not Found) response to
 * indicate that the requested resource is not available.
 */
public class NoResourceFoundProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link NoResourceFoundProblemResolver} with default problem format. */
  public NoResourceFoundProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link NoResourceFoundProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public NoResourceFoundProblemResolver(ProblemFormat problemFormat) {
    super(NoResourceFoundException.class, problemFormat);
  }

  /**
   * Returns a {@link ProblemBuilder} with {@link HttpStatus#NOT_FOUND} (HTTP 404) indicating the
   * requested static resource could not be located. Other parameters ({@code context}, {@code
   * headers}, {@code status}) are ignored because the exception semantics unambiguously map to 404.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link NoResourceFoundException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 404 enforced)
   * @return builder pre-populated with NOT_FOUND status
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.builder().status(HttpStatus.NOT_FOUND.value());
  }
}
