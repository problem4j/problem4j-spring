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
import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Thrown when the request body cannot be decoded (e.g. malformed JSON or invalid * content type).
 * Maps such errors to a {@link Problem} with {@code 400 Bad Request} status.
 *
 * <p>Maps decoding failures (e.g. malformed JSON or invalid request bodies) to a {@link Problem}
 * response with {@code 400 Bad Request} status.
 *
 * @since 1.2.0
 */
public class DecodingProblemResolver extends AbstractProblemResolver {

  /**
   * Constructs a new {@link DecodingProblemResolver} with the default problem format.
   *
   * @since 1.2.0
   */
  public DecodingProblemResolver() {
    super(DecodingException.class);
  }

  /**
   * Constructs a new {@link DecodingProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns the {@link
   *     ProblemFormat} after construction; use {@link #DecodingProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public DecodingProblemResolver(ProblemFormat problemFormat) {
    super(DecodingException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} for {@link DecodingException} with {@link HttpStatus#BAD_REQUEST}
   * status.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link DecodingException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 400 enforced)
   * @return problem with 400 status
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.of(HttpStatus.BAD_REQUEST.value());
  }
}
