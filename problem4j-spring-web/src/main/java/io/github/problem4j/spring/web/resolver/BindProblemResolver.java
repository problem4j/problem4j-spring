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

import static io.github.problem4j.spring.web.parameter.ViolationSupport.VALIDATION_FAILED_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.BindingResultSupportAware;
import io.github.problem4j.spring.web.parameter.DefaultBindingResultSupport;
import io.github.problem4j.spring.web.parameter.ViolationSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindException;

/**
 * Due to {@link BindException} being subclassed by {@code MethodArgumentNotValidException}, this
 * implementation also covers that exceptions.
 *
 * <p>Quite obvious message, but worth to note that the only reason {@link BindProblemResolver} is
 * kept is due to backwards compatibility as Problem4J doesn't use any fields from that subclass at
 * the moment.
 *
 * <p>These exceptions indicate that incoming request parameters or bodies could not be bound to
 * target objects or did not pass validation constraints.
 *
 * <ul>
 *   <li>{@link BindException} - thrown for binding or validation errors on form or query
 *       parameters.
 *   <li>{@code MethodArgumentNotValidException} - thrown for validation failures on
 *       {@code @RequestBody} or {@code @ModelAttribute} method arguments.
 * </ul>
 *
 * <p>When used as a Spring bean, in addition to the {@link ProblemFormat} injected via {@link
 * AbstractProblemResolver}, the {@link BindingResultSupport} is assigned after construction by
 * {@link ProblemBeanPostProcessor} through {@link #setBindingResultSupport(BindingResultSupport)},
 * unless it was passed to the constructor.
 *
 * @see org.springframework.web.bind.MethodArgumentNotValidException
 * @see org.springframework.web.bind.annotation.ModelAttribute
 * @see org.springframework.web.bind.annotation.RequestBody
 * @since 1.2.0
 */
public class BindProblemResolver extends AbstractProblemResolver
    implements BindingResultSupportAware {

  private BindingResultSupport bindingResultSupport;

  /**
   * Whether {@link #setBindingResultSupport(BindingResultSupport)} may replace the {@link
   * BindingResultSupport}. It is {@code false} when the {@link BindingResultSupport} was passed
   * explicitly to a (deprecated) constructor, so it is kept as in 3.0.0, where constructor
   * arguments were never replaced afterward.
   */
  private final boolean bindingResultSupportAssignable;

  /**
   * Constructs a new {@link BindProblemResolver} with the default problem format and binding result
   * support.
   *
   * @since 1.2.0
   */
  public BindProblemResolver() {
    super(BindException.class);
    this.bindingResultSupport = new DefaultBindingResultSupport();
    this.bindingResultSupportAssignable = true;
  }

  /**
   * Constructs a new {@link BindProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns collaborators after
   *     construction; use {@link #BindProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public BindProblemResolver(ProblemFormat problemFormat) {
    super(BindException.class, problemFormat);
    this.bindingResultSupport = new DefaultBindingResultSupport();
    this.bindingResultSupportAssignable = true;
  }

  /**
   * Constructs a new {@link BindProblemResolver} with the specified problem format and binding
   * result support.
   *
   * @param problemFormat the problem format to use
   * @param bindingResultSupport the binding result support to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns collaborators after
   *     construction; use {@link #BindProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public BindProblemResolver(
      ProblemFormat problemFormat, BindingResultSupport bindingResultSupport) {
    super(BindException.class, problemFormat);
    this.bindingResultSupport = bindingResultSupport;
    this.bindingResultSupportAssignable = false;
  }

  /**
   * Replaces the {@link BindingResultSupport} used by this resolver, unless one was passed to the
   * constructor, in which case this method has no effect.
   *
   * @param bindingResultSupport the binding result support to use
   * @since 3.1.0
   */
  @Override
  public void setBindingResultSupport(BindingResultSupport bindingResultSupport) {
    if (bindingResultSupportAssignable) {
      this.bindingResultSupport = bindingResultSupport;
    }
  }

  /**
   * Resolves a {@link BindException} (or subclass) to a {@link Problem} with {@link
   * HttpStatus#BAD_REQUEST} and an {@code errors} extension listing field/global validation
   * violations produced by the underlying {@link BindException#getBindingResult()}.
   *
   * @param context problem context (ignored for binding violations)
   * @param ex the binding / validation exception (must be a {@link BindException})
   * @param headers HTTP response headers (unused here but part of the SPI)
   * @param status suggested HTTP status from caller (ignored; BAD_REQUEST is enforced)
   * @return problem with validation detail and violations
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    BindException e = (BindException) ex;
    return Problem.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(formatDetail(VALIDATION_FAILED_DETAIL))
        .extension(
            ViolationSupport.ERRORS_EXTENSION,
            bindingResultSupport.fetchViolations(e.getBindingResult()))
        .build();
  }
}
