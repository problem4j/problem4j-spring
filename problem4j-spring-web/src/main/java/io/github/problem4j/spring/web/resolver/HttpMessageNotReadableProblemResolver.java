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
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapperAware;
import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Handles {@link HttpMessageNotReadableException} thrown when an HTTP request body cannot be read
 * or parsed.
 *
 * <p>This typically occurs for requests with malformed JSON, XML, or other payloads that cannot be
 * converted by the configured {@code HttpMessageConverter}s.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response to
 * indicate that the request body is invalid or unreadable.
 *
 * <p>When used as a Spring bean, in addition to the {@link ProblemFormat} injected via {@link
 * AbstractProblemResolver}, the {@link TypeNameMapper} is assigned after construction by {@link
 * ProblemBeanPostProcessor} through {@link #setTypeNameMapper(TypeNameMapper)}, unless it was
 * passed to the constructor.
 *
 * @see org.springframework.http.converter.HttpMessageConverter
 * @since 1.2.0
 */
public class HttpMessageNotReadableProblemResolver extends AbstractProblemResolver
    implements TypeNameMapperAware {

  private final JacksonErrorHelper jacksonErrorHelper;

  /**
   * Whether {@link #setTypeNameMapper(TypeNameMapper)} may replace the {@link TypeNameMapper}. It
   * is {@code false} when the {@link TypeNameMapper} was passed explicitly to a (deprecated)
   * constructor, so it is kept as in 3.0.0, where constructor arguments were never replaced
   * afterward.
   */
  private final boolean typeNameMapperAssignable;

  /**
   * Creates a new {@link HttpMessageNotReadableProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public HttpMessageNotReadableProblemResolver() {
    super(HttpMessageNotReadableException.class);
    this.jacksonErrorHelper =
        new JacksonErrorHelper(ProblemFormat.identity(), new SimpleTypeNameMapper());
    this.typeNameMapperAssignable = true;
  }

  /**
   * Creates a new {@link HttpMessageNotReadableProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns collaborators after
   *     construction; use {@link #HttpMessageNotReadableProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public HttpMessageNotReadableProblemResolver(ProblemFormat problemFormat) {
    super(HttpMessageNotReadableException.class, problemFormat);
    this.jacksonErrorHelper = new JacksonErrorHelper(problemFormat, new SimpleTypeNameMapper());
    this.typeNameMapperAssignable = true;
  }

  /**
   * Creates a new {@link HttpMessageNotReadableProblemResolver} with the specified problem format
   * and type name mapper.
   *
   * @param problemFormat the problem format to use
   * @param typeNameMapper the type mapper to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns collaborators after
   *     construction; use {@link #HttpMessageNotReadableProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public HttpMessageNotReadableProblemResolver(
      ProblemFormat problemFormat, TypeNameMapper typeNameMapper) {
    super(HttpMessageNotReadableException.class, problemFormat);
    this.jacksonErrorHelper = new JacksonErrorHelper(problemFormat, typeNameMapper);
    this.typeNameMapperAssignable = false;
  }

  /**
   * Replaces the {@link ProblemFormat} used by this resolver, unless one was passed to the
   * constructor, in which case this method has no effect.
   *
   * @param problemFormat the problem format to use
   * @since 3.1.0
   */
  @Override
  public void setProblemFormat(ProblemFormat problemFormat) {
    super.setProblemFormat(problemFormat);
    jacksonErrorHelper.setProblemFormat(getProblemFormat());
  }

  /**
   * Replaces the {@link TypeNameMapper} used by this resolver, unless one was passed to the
   * constructor, in which case this method has no effect.
   *
   * @param typeNameMapper the type name mapper to use
   * @since 3.1.0
   */
  @Override
  public void setTypeNameMapper(TypeNameMapper typeNameMapper) {
    if (typeNameMapperAssignable) {
      jacksonErrorHelper.setTypeNameMapper(typeNameMapper);
    }
  }

  /**
   * Returns a {@link Problem} with {@link HttpStatus#BAD_REQUEST} (HTTP 400). Other parameters
   * ({@code context}, {@code headers}, {@code status}) are ignored because a malformed or
   * unreadable request body always maps to a client error.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link HttpMessageNotReadableException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 400 enforced)
   * @return problem with 400 status
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    if (ex.getCause() instanceof MismatchedInputException e) {
      return jacksonErrorHelper.resolveMismatchedInput(e);
    }
    return Problem.of(HttpStatus.BAD_REQUEST.value());
  }
}
