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
import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;
import org.jspecify.annotations.Nullable;

/**
 * Convenience base class for {@link ProblemResolver}-s.
 *
 * <p>When used as a Spring bean under {@code problem4j-spring-web} autoconfiguration, the {@link
 * ProblemFormat} held by this resolver is assigned after construction by {@link
 * ProblemBeanPostProcessor} through {@link #setProblemFormat(ProblemFormat)}. Subclasses may have
 * further collaborators injected the same way; each documents its own.
 *
 * <p>For backwards compatibility with 3.0.0, a collaborator passed explicitly to a (deprecated)
 * constructor is never replaced afterward - its setter has no effect. Only collaborators created by
 * the resolver itself as defaults can be replaced.
 *
 * @since 1.2.0
 */
public abstract class AbstractProblemResolver implements ProblemResolver, ProblemFormatAware {

  private final Class<? extends Exception> clazz;

  private ProblemFormat problemFormat;

  /**
   * Whether {@link #setProblemFormat(ProblemFormat)} may replace the {@link ProblemFormat}. It is
   * {@code false} when the {@link ProblemFormat} was passed explicitly to a (deprecated)
   * constructor, so it is kept as in 3.0.0, where constructor arguments were never replaced
   * afterward.
   */
  private final boolean problemFormatAssignable;

  /**
   * Creates a resolver for the given exception type using {@link ProblemFormat#identity()} (no
   * detail transformation).
   *
   * @param clazz exception subtype this resolver is responsible for
   * @since 1.2.0
   */
  public AbstractProblemResolver(Class<? extends Exception> clazz) {
    this.clazz = clazz;
    this.problemFormat = ProblemFormat.identity();
    this.problemFormatAssignable = true;
  }

  /**
   * Creates a resolver for the given exception type with a custom {@link ProblemFormat} applied to
   * any detail text via {@link #formatDetail(String)}.
   *
   * <p>The given {@link ProblemFormat} is kept for the lifetime of this resolver and is not
   * replaced by {@link #setProblemFormat(ProblemFormat)}.
   *
   * @param clazz exception subtype this resolver is responsible for
   * @param problemFormat formatting strategy for detail (must not be {@code null})
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns the {@link
   *     ProblemFormat} after construction; use {@link #AbstractProblemResolver(Class)} and rely on
   *     {@link #setProblemFormat(ProblemFormat)}
   */
  @Deprecated(since = "3.1.0", forRemoval = true)
  public AbstractProblemResolver(Class<? extends Exception> clazz, ProblemFormat problemFormat) {
    this.clazz = clazz;
    this.problemFormat = problemFormat;
    this.problemFormatAssignable = false;
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
   * Returns the {@link ProblemFormat} currently used by this resolver, so that built-in subclasses
   * can propagate it to their helpers after {@link #setProblemFormat(ProblemFormat)}.
   *
   * @return the problem format in use
   */
  final ProblemFormat getProblemFormat() {
    return problemFormat;
  }

  /**
   * Replaces the {@link ProblemFormat} used by this resolver, unless one was passed to the
   * constructor, in which case this method has no effect.
   *
   * <p>Subclasses overriding this method should call {@code super}, so that a {@link ProblemFormat}
   * passed to the constructor is kept.
   *
   * @param problemFormat the problem format to use
   * @since 3.1.0
   */
  @Override
  public void setProblemFormat(ProblemFormat problemFormat) {
    if (problemFormatAssignable) {
      this.problemFormat = problemFormat;
    }
  }
}
