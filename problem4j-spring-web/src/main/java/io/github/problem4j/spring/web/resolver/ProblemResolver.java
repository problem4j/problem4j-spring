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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * Represents a resolver from a specific {@link Exception} to a {@link ProblemBuilder}, can be
 * further extended or executed to create {@link Problem} response.
 *
 * <p>Implementations are supposed to be stateless.
 */
public interface ProblemResolver {

  /**
   * Returns the type of {@link Exception} this resolver supports.
   *
   * @return supported exception class
   */
  Class<? extends Exception> getExceptionClass();

  /**
   * Resolves the given exception into a {@link ProblemBuilder}.
   *
   * @param context problem context
   * @param ex exception to resolve
   * @param headers to be included in HTTP response
   * @param status HTTP status recommented by caller
   * @return problem builder representing the resolved problem
   */
  ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status);

  /**
   * Resolves the given exception into an immutable {@link Problem}.
   *
   * <p>This method is a convenient shortcut for callers who do not need to modify the problem.
   * Internally, it delegates to {@code #resolveBuilder(ProblemContext, Exception, HttpHeaders,
   * HttpStatusCode)} and immediately calls {@link ProblemBuilder#build()} to produce a fully
   * immutable {@link Problem}.
   *
   * <p>Use {@link #resolveBuilder(ProblemContext, Exception, HttpHeaders, HttpStatusCode)} directly
   * if you need to further customize or enrich the problem before building.
   *
   * @param context problem context
   * @param ex exception to resolve
   * @param headers HTTP headers to include in the response
   * @param status HTTP status recommended by the caller
   * @return an immutable {@link Problem} representing the resolved error
   */
  default Problem resolveProblem(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return resolveBuilder(context, ex, headers, status).build();
  }
}
