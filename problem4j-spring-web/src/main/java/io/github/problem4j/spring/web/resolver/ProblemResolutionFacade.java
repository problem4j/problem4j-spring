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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * Resolves exceptions not handled by any other exception handler into {@link Problem} responses.
 * Used by error handlers of WebMVC and WebFlux.
 *
 * <p>The result is not post-processed - callers apply {@code ProblemPostProcessor} themselves.
 *
 * @see DefaultProblemResolutionFacade
 * @since 3.1.0
 */
@FunctionalInterface
public interface ProblemResolutionFacade {

  /**
   * Returns a {@link ProblemResolutionFacade} that ignores the exception and resolves only the HTTP
   * status of the error.
   *
   * @return a {@link ProblemResolutionFacade} resolving only the HTTP status
   * @since 3.1.0
   */
  static ProblemResolutionFacade statusOnly() {
    return (context, ex, headers, status) -> Problem.of(status.value());
  }

  /**
   * Resolves given exception into a {@link Problem}.
   *
   * @param context the current problem context
   * @param ex the exception to resolve
   * @param headers response headers, which resolvers may modify
   * @param status the HTTP status determined for the error by the server, passed to resolvers and
   *     used if nothing else resolves the exception
   * @return the resolved {@link Problem}
   * @since 3.1.0
   */
  Problem resolve(ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status);
}
