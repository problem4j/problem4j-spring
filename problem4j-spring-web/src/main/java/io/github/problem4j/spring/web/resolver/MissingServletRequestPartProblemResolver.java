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

import static io.github.problem4j.spring.web.ProblemSupport.MISSING_REQUEST_PART_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.PARAM_EXTENSION;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Handles {@link MissingServletRequestPartProblemResolver} related exceptions, thrown when a
 * required part of a multipart request is missing.
 *
 * <p>This typically occurs when a controller method parameter is annotated with
 * {@code @RequestPart} and the client fails to include the expected file or form part in a
 * multipart/form-data request.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response to
 * indicate that the required request part was not provided.
 *
 * <p>Unlike {@link ServletRequestBindingProblemResolver}, this resolver is separate because its
 * exception ({@link MissingServletRequestPartException}} extends {@code ServletException} rather
 * than {@code ServletRequestBindingException}.
 *
 * @see jakarta.servlet.ServletException
 * @see org.springframework.web.bind.ServletRequestBindingException
 */
public class MissingServletRequestPartProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link MissingServletRequestPartProblemResolver} with default problem format. */
  public MissingServletRequestPartProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link MissingServletRequestPartProblemResolver} with the specified problem
   * format.
   *
   * @param problemFormat the problem format to use
   */
  public MissingServletRequestPartProblemResolver(ProblemFormat problemFormat) {
    super(MissingServletRequestPartException.class, problemFormat);
  }

  /**
   * Builds a {@link ProblemBuilder} representing a missing multipart request part.
   *
   * <p>Always returns a builder with status {@link HttpStatus#BAD_REQUEST}, a standardized detail
   * message ({@code ProblemSupport#MISSING_REQUEST_PART_DETAIL}) and an extension named {@code
   * ProblemSupport#PARAM_EXTENSION} containing the missing part's name.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link MissingServletRequestPartException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 400 enforced)
   * @return builder pre-populated with status, detail and parameter extension
   * @see io.github.problem4j.spring.web.ProblemSupport#MISSING_REQUEST_PART_DETAIL
   * @see io.github.problem4j.spring.web.ProblemSupport#PARAM_EXTENSION
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    MissingServletRequestPartException e = (MissingServletRequestPartException) ex;
    return Problem.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(formatDetail(MISSING_REQUEST_PART_DETAIL))
        .extension(PARAM_EXTENSION, e.getRequestPartName());
  }
}
