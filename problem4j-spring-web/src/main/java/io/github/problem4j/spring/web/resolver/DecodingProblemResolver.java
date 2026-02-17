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
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * Thrown when the request body cannot be decoded (e.g. malformed JSON or invalid * content type).
 * Maps such errors to a {@link Problem} with {@code 400 Bad Request} status.
 *
 * <p>Maps decoding failures (e.g. malformed JSON or invalid request bodies) to a {@link Problem}
 * response with {@code 400 Bad Request} status.
 */
public class DecodingProblemResolver extends AbstractProblemResolver {

  /** Constructs a new {@link DecodingProblemResolver} with the default problem format. */
  public DecodingProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Constructs a new {@link DecodingProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public DecodingProblemResolver(ProblemFormat problemFormat) {
    super(DecodingException.class, problemFormat);
  }

  /**
   * Builds a {@link ProblemBuilder} for {@link DecodingException} with {@link
   * ProblemStatus#BAD_REQUEST} status.
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.builder().status(ProblemStatus.BAD_REQUEST);
  }
}
