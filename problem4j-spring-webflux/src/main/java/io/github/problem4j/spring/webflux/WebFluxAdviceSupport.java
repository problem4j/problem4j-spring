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

import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;

import org.slf4j.Logger;
import org.springframework.web.server.ServerWebExchange;

class WebFluxAdviceSupport {

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
}
