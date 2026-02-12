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

/** Convenience base class for {@link ProblemResolver}-s. */
public abstract class AbstractProblemResolver implements ProblemResolver {

  private final Class<? extends Exception> clazz;

  private final ProblemFormat problemFormat;

  /**
   * Creates a resolver for the given exception type using {@link IdentityProblemFormat} (no detail
   * transformation).
   *
   * @param clazz exception subtype this resolver is responsible for
   */
  public AbstractProblemResolver(Class<? extends Exception> clazz) {
    this(clazz, new IdentityProblemFormat());
  }

  /**
   * Creates a resolver for the given exception type with a custom {@link ProblemFormat} applied to
   * any detail text via {@link #formatDetail(String)}.
   *
   * @param clazz exception subtype this resolver is responsible for
   * @param problemFormat formatting strategy for detail (must not be {@code null})
   */
  public AbstractProblemResolver(Class<? extends Exception> clazz, ProblemFormat problemFormat) {
    this.clazz = clazz;
    this.problemFormat = problemFormat;
  }

  /** Returns the configured exception class this resolver supports. */
  @Override
  public Class<? extends Exception> getExceptionClass() {
    return clazz;
  }

  /**
   * Default implementation that returns a builder with status {@link
   * ProblemStatus#INTERNAL_SERVER_ERROR}. Subclasses should override to populate fields like {@code
   * type}, {@code title}, {@code detail}, and extensions.
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Builds the final {@link Problem} instance using the {@link #resolveBuilder} result. Subclasses
   * typically customize only {@code resolveBuilder}, not this method. Overwritten to explicitly
   * make it {@code final}, so {@code resolveBuilder} and {@code resolveProblem} never diverge.
   */
  @Override
  public final Problem resolveProblem(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return resolveBuilder(context, ex, headers, status).build();
  }

  /**
   * Applies the configured {@link ProblemFormat} to a detail string (may return unchanged value if
   * using {@link IdentityProblemFormat}).
   *
   * @param detail original detail (nullable)
   * @return formatted detail (never null if input not null)
   */
  protected String formatDetail(String detail) {
    return problemFormat.formatDetail(detail);
  }
}
