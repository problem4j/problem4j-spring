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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.multipart.MultipartException;

/**
 * Resolves {@link MultipartException}s into {@link Problem} responses with status 400
 * (BAD_REQUEST).
 *
 * <p>A {@link MultipartException} is thrown when a multipart request fails, e.g., due to exceeding
 * file size limits or parsing errors.
 *
 * @since 1.2.0
 */
public class MultipartProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link MultipartProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public MultipartProblemResolver() {
    super(MultipartException.class);
  }

  /**
   * Creates a new {@link MultipartProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link
   *     io.github.problem4j.spring.web.config.DefaultProblemBeanPostProcessor
   *     ProblemBeanPostProcessor} now assigns the {@link ProblemFormat} after construction; use
   *     {@link #MultipartProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public MultipartProblemResolver(ProblemFormat problemFormat) {
    super(MultipartException.class, problemFormat);
  }

  /**
   * Resolves the given {@link MultipartException} into a {@link Problem} with {@link
   * HttpStatus#BAD_REQUEST} status.
   *
   * @param context the {@link ProblemContext} providing information about the current request
   * @param ex the {@link MultipartException} to be resolved
   * @param headers the {@link HttpHeaders} of the current response
   * @param status the original {@link HttpStatusCode} that would have been returned
   * @return a {@link Problem} representing the error response
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.of(HttpStatus.BAD_REQUEST.value());
  }
}
