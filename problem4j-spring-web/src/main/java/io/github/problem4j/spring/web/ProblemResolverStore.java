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

import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.Optional;

/**
 * Registry that provides access to {@link ProblemResolver} instances based on exception types.
 *
 * <p>Implementations are responsible for locating the most specific resolver for a given {@link
 * Exception} class, typically by checking class assignability and caching results to improve
 * performance.
 */
public interface ProblemResolverStore {

  /**
   * Finds the most specific {@link ProblemResolver} for the given exception class.
   *
   * <p>This method searches the store for all resolvers whose keys are assignable from the provided
   * exception class. If multiple resolvers match, it returns the one closest in the class hierarchy
   * (i.e., the most specific resolver). If no resolver is found, an empty {@link Optional} is
   * returned. Results are cached to optimize repeated lookups for the same class.
   *
   * @param clazz the exception class for which to find a resolver
   * @return an {@link Optional} containing the most specific {@link ProblemResolver} if found,
   *     otherwise an empty {@link Optional}
   */
  Optional<ProblemResolver> findResolver(Class<? extends Exception> clazz);
}
