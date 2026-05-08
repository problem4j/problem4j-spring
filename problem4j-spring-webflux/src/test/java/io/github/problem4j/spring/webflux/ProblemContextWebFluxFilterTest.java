/*
 * Copyright (c) 2025-2026 The Problem4J Authors
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
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class ProblemContextWebFluxFilterTest {

  @Test
  void givenNoTracingHeaderName_whenFilter_thenGeneratedTraceIdIsLowercase32HexCharacters() {
    ProblemContextWebFluxFilter filter = new ProblemContextWebFluxFilter(() -> null);
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE))
        .matches("^urn:uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
  }

  @Test
  void
      givenTracingHeaderNameConfigured_whenNoHeaderInRequest_thenGeneratedTraceIdIsLowercase32HexCharacters() {
    ProblemContextWebFluxFilter filter = new ProblemContextWebFluxFilter(() -> "X-Trace-Id");
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE))
        .matches("^urn:uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
  }

  @Test
  void
      givenTracingHeaderNameConfigured_whenHeaderPresentInRequest_thenUsesRequestHeaderAsTraceId() {
    ProblemContextWebFluxFilter filter = new ProblemContextWebFluxFilter(() -> "X-Trace-Id");
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/test").header("X-Trace-Id", "custom-trace-id").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo("custom-trace-id");
  }
}
