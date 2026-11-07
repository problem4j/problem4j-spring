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

import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_REQUEST_PART_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.PARAM_EXTENSION;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Handles {@link MissingServletRequestPartProblemResolver} related exceptions, thrown when a
 * required part of a multipart request is missing.
 *
 * <p>This typically occurs when a controller method parameter is annotated with
 * {@code @RequestPart} and the client fails to include the expected file or form part in a
 * multipart/form-data request.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response to
 * indicate that the required request part was not provided.
 *
 * <p>Unlike {@link ServletRequestBindingProblemResolver}, this resolver is separate because its
 * exception ({@link MissingServletRequestPartException}} extends {@code ServletException} rather
 * than {@code ServletRequestBindingException}.
 *
 * @see jakarta.servlet.ServletException
 * @see org.springframework.web.bind.ServletRequestBindingException
 * @since 1.2.0
 */
public class MissingServletRequestPartProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link MissingServletRequestPartProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public MissingServletRequestPartProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link MissingServletRequestPartProblemResolver} with the specified problem
   * format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public MissingServletRequestPartProblemResolver(ProblemFormat problemFormat) {
    super(MissingServletRequestPartException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} representing a missing multipart request part with status {@link
   * HttpStatus#BAD_REQUEST}, a standardized detail message ({@code
   * ViolationSupport#MISSING_REQUEST_PART_DETAIL}) and an extension named {@code
   * ViolationSupport#PARAM_EXTENSION} containing the missing part's name.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link MissingServletRequestPartException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 400 enforced)
   * @return problem with status, detail and parameter extension
   * @see io.github.problem4j.spring.web.parameter.ViolationSupport#MISSING_REQUEST_PART_DETAIL
   * @see io.github.problem4j.spring.web.parameter.ViolationSupport#PARAM_EXTENSION
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    MissingServletRequestPartException e = (MissingServletRequestPartException) ex;
    return Problem.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(formatDetail(MISSING_REQUEST_PART_DETAIL))
        .extension(PARAM_EXTENSION, e.getRequestPartName())
        .build();
  }
}
