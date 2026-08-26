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

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;
import static io.github.problem4j.spring.web.ProblemMediaTypeSupport.resolveAccept;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * Shared helpers used by Problem4J's WebFlux advices to resolve the response content type and log
 * exceptions encountered while handling other exceptions.
 *
 * @since 3.0.1
 */
public final class WebFluxAdviceSupport {

  /**
   * Resolves the {@code Problem} content type from the {@code Accept} header of the given exchange.
   *
   * @param exchange the current server web exchange
   * @return the resolved content type
   * @since 3.0.1
   */
  public static MediaType resolveContentType(ServerWebExchange exchange) {
    return resolveAccept(exchange.getRequest().getHeaders().getAccept());
  }

  /**
   * Resolves the {@code Problem} content type from the {@code Accept} header of the given request.
   *
   * @param request the current server request
   * @return the resolved content type
   * @since 3.0.1
   */
  public static MediaType resolveContentType(ServerRequest request) {
    return resolveAccept(request.headers().accept());
  }

  /**
   * Logs exception that occurred while processing exception occurred within controller advices.
   *
   * @param log logger name configured to appropriate controller advice class name
   * @param ex exception that occurred within controller
   * @param exchange exchange that was being handled while that exception occurred
   * @param e exception that occurred while processing exception {@code ex}
   */
  static void logAdviceException(
      Logger log, Exception ex, ServerWebExchange exchange, Exception e) {
    log.warn(
        "Unable to resolve problem response (method={}, path={}, traceId={}, message={}, originalException=[{} : {}])",
        exchange.getRequest().getMethod(),
        exchange.getRequest().getPath(),
        exchange.getAttribute(TRACE_ID_ATTRIBUTE),
        e.getMessage(),
        ex.getClass().getName(),
        ex.getMessage(),
        e);
  }

  private WebFluxAdviceSupport() {}
}
