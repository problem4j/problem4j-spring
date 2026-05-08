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
