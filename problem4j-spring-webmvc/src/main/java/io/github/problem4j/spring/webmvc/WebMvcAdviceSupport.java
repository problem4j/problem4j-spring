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

package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import org.slf4j.Logger;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

final class WebMvcAdviceSupport {

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

  private WebMvcAdviceSupport() {}
}
