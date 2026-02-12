/*
 * Copyright (c) 2025-2026 Damian Malczewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
   */
  void inspect(
      ProblemContext context,
      Problem problem,
      Exception ex,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange);
}
