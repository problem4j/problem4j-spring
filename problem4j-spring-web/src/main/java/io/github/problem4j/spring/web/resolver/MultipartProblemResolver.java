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
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.multipart.MultipartException;

/**
 * Resolves {@link MultipartException}s into {@link Problem} responses with status 400
 * (BAD_REQUEST).
 *
 * <p>A {@link MultipartException} is thrown when a multipart request fails, e.g., due to exceeding
 * file size limits or parsing errors.
 */
public class MultipartProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link MultipartProblemResolver} with default problem format. */
  public MultipartProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link MultipartProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public MultipartProblemResolver(ProblemFormat problemFormat) {
    super(MultipartException.class, problemFormat);
  }

  /**
   * Resolves the given {@link MultipartException} into a {@link ProblemBuilder}.
   *
   * <p>The resulting {@link Problem} will have a {@link ProblemStatus#BAD_REQUEST} status.
   *
   * @param context the {@link ProblemContext} providing information about the current request
   * @param ex the {@link MultipartException} to be resolved
   * @param headers the {@link HttpHeaders} of the current response
   * @param status the original {@link HttpStatusCode} that would have been returned
   * @return a {@link ProblemBuilder} for building the {@link Problem} response
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.builder().status(ProblemStatus.BAD_REQUEST);
  }
}
