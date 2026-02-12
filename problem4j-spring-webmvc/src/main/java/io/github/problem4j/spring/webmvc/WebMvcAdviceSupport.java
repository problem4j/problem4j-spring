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
package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import org.slf4j.Logger;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

class WebMvcAdviceSupport {

  /**
   * Logs exception that occurred while processing exception occurred within controller advices.
   *
   * @param log logger name configured to appropriate controller advice class name
   * @param ex exception that occurred within controller
   * @param request request that was being handled while that exception occurred
   * @param e exception that occurred while processing exception {@code ex}
   */
  static void logAdviceException(Logger log, Exception ex, WebRequest request, Exception e) {
    String method = "<unknown>";
    String endpoint = "<unknown>";
    String traceId = null;

    if (request instanceof ServletWebRequest req && req.getRequest().getRequestURI() != null) {
      method = String.valueOf(req.getHttpMethod());
      endpoint = req.getRequest().getRequestURI();

      Object traceIdAttr = req.getAttribute(TRACE_ID_ATTRIBUTE, SCOPE_REQUEST);
      if (traceIdAttr != null) {
        traceId = traceIdAttr.toString();
      }
    }

    log.warn(
        "Unable to resolve problem response (method={}, endpoint={}, traceId={}, message={}, originalException=[{} : {}])",
        method,
        endpoint,
        traceId,
        e.getMessage(),
        ex.getClass().getName(),
        ex.getMessage(),
        e);
  }
}
