/*
 * Copyright 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.webflux;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebExchange;

/**
 * Provides a hook for observing the details of a Problem response in a WebFlux application before
 * it is returned to the client.
 *
 * <p>Implementations of this interface can peek at the context of a Problem, including the original
 * exception, HTTP headers, status, and the current {@link ServerWebExchange}, without modifying the
 * response.
 *
 * <p>Typical use cases include logging, monitoring, or auditing error responses.
 *
 * @since 1.2.0
 */
public interface AdviceWebFluxInspector {

  /**
   * Observe the details of a Problem response before it is sent to the client.
   *
   * @param context the {@link ProblemContext} containing information about the current error
   *     handling context
   * @param problem the {@link Problem} object representing the response body
   * @param ex the original {@link Exception} that triggered the Problem
   * @param headers the HTTP headers that will be included in the response
   * @param status the HTTP status code for the response
   * @param exchange the current {@link ServerWebExchange} associated with the handling
   * @since 1.2.0
   */
  void inspect(
      ProblemContext context,
      Problem problem,
      Exception ex,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange);
}
