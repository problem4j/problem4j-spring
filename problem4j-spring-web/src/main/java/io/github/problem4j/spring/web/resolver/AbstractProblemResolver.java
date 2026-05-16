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

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.web.ProblemFormat;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

/**
 * Convenience base class for {@link ProblemResolver}-s.
 *
 * @since 1.2.0
 */
public abstract class AbstractProblemResolver implements ProblemResolver {

  /**
   * Pre-built {@link Problem} with {@link HttpStatus#BAD_REQUEST} (400) status.
   *
   * @since 3.1.0
   */
  protected static final Problem BAD_REQUEST = Problem.of(HttpStatus.BAD_REQUEST.value());

  /**
   * Pre-built {@link Problem} with {@link HttpStatus#NOT_FOUND} (404) status.
   *
   * @since 3.1.0
   */
  protected static final Problem NOT_FOUND = Problem.of(HttpStatus.NOT_FOUND.value());

  /**
   * Pre-built {@link Problem} with {@link HttpStatus#METHOD_NOT_ALLOWED} (405) status.
   *
   * @since 3.1.0
   */
  protected static final Problem METHOD_NOT_ALLOWED =
      Problem.of(HttpStatus.METHOD_NOT_ALLOWED.value());

  /**
   * Pre-built {@link Problem} with {@link HttpStatus#NOT_ACCEPTABLE} (406) status.
   *
   * @since 3.1.0
   */
  protected static final Problem NOT_ACCEPTABLE = Problem.of(HttpStatus.NOT_ACCEPTABLE.value());

  /**
   * Pre-built {@link Problem} with {@link HttpStatus#UNSUPPORTED_MEDIA_TYPE} (415) status.
   *
   * @since 3.1.0
   */
  protected static final Problem UNSUPPORTED_MEDIA_TYPE =
      Problem.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());

  /**
   * Pre-built {@link Problem} with {@link HttpStatus#INTERNAL_SERVER_ERROR} (500) status.
   *
   * @since 3.1.0
   */
  protected static final Problem INTERNAL_SERVER_ERROR =
      Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());

  private final Class<? extends Exception> clazz;

  private final ProblemFormat problemFormat;

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
}
