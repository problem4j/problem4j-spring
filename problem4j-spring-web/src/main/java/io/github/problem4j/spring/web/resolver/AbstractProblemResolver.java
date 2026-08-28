/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.problem4j.spring.web.resolver;

import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemFormatAware;
import org.jspecify.annotations.Nullable;

/**
 * Convenience base class for {@link ProblemResolver}-s.
 *
 * @since 1.2.0
 */
public abstract class AbstractProblemResolver implements ProblemResolver, ProblemFormatAware {

  private final Class<? extends Exception> clazz;

  private ProblemFormat problemFormat;

  /**
   * Creates a resolver for the given exception type using {@link ProblemFormat#identity()} (no
   * detail transformation).
   *
   * @param clazz exception subtype this resolver is responsible for
   * @since 1.2.0
   */
  public AbstractProblemResolver(Class<? extends Exception> clazz) {
    this(clazz, ProblemFormat.identity());
  }

  /**
   * Creates a resolver for the given exception type with a custom {@link ProblemFormat} applied to
   * any detail text via {@link #formatDetail(String)}.
   *
   * @param clazz exception subtype this resolver is responsible for
   * @param problemFormat formatting strategy for detail (must not be {@code null})
   * @since 1.2.0
   */
  public AbstractProblemResolver(Class<? extends Exception> clazz, ProblemFormat problemFormat) {
    this.clazz = clazz;
    this.problemFormat = problemFormat;
  }

  /**
   * Returns the configured exception class this resolver supports.
   *
   * @return the exception subtype handled by this resolver
   * @since 1.2.0
   */
  @Override
  public Class<? extends Exception> getExceptionClass() {
    return clazz;
  }

  /**
   * Applies the configured {@link ProblemFormat} to a detail string.
   *
   * @param detail original detail (nullable)
   * @return formatted detail (never null if input not null)
   * @since 1.2.0
   */
  protected final @Nullable String formatDetail(@Nullable String detail) {
    return problemFormat.formatDetail(detail);
  }

  /**
   * Replaces the {@link ProblemFormat} used by this resolver.
   *
   * @param problemFormat the problem format to use
   * @since 3.1.0
   */
  @Override
  public void setProblemFormat(ProblemFormat problemFormat) {
    this.problemFormat = problemFormat;
  }
}
