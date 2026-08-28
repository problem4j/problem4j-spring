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

package io.github.problem4j.spring.webmvc.resolver;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.resolver.AbstractProblemResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Handles {@link NoHandlerFoundException} thrown when no matching handler (controller method) is
 * found for a given request.
 *
 * <p>This typically occurs when the client requests a URL or HTTP method that does not correspond
 * to any mapped controller endpoint.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 404 (Not Found) response to
 * indicate that the requested resource or endpoint does not exist.
 *
 * @since 1.2.0
 */
public class NoHandlerFoundProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link NoHandlerFoundProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public NoHandlerFoundProblemResolver() {
    super(NoHandlerFoundException.class);
  }

  /**
   * Creates a new {@link NoHandlerFoundProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link
   *     io.github.problem4j.spring.web.config.DefaultProblemBeanPostProcessor
   *     ProblemBeanPostProcessor} now assigns the {@link ProblemFormat} after construction; use
   *     {@link #NoHandlerFoundProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public NoHandlerFoundProblemResolver(ProblemFormat problemFormat) {
    super(NoHandlerFoundException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} with {@link HttpStatus#NOT_FOUND} (HTTP 404) indicating no controller
   * handler matched the incoming request (URL + HTTP method). Other parameters ({@code context},
   * {@code headers}, {@code status}) are ignored because the semantics of {@link
   * NoHandlerFoundException} unambiguously map to 404.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link NoHandlerFoundException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 404 enforced)
   * @return problem with NOT_FOUND status
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.of(HttpStatus.NOT_FOUND.value());
  }
}
