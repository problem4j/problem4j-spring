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

package io.github.problem4j.spring.web;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.jspecify.annotations.Nullable;

/**
 * Convenience implementation for {@link ProblemPostProcessor} which doesn't transform input data.
 */
public class IdentityProblemPostProcessor implements ProblemPostProcessor {

  /**
   * Returns the given {@link Problem} unchanged.
   *
   * @param context optional problem context (ignored)
   * @param problem the problem instance to pass through (may be {@code null})
   * @return the same instance provided in {@code problem}
   */
  @Override
  public Problem process(@Nullable ProblemContext context, Problem problem) {
    return problem;
  }
}
