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

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemContextSettings;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler.TracingContext;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class MicrometerProblemContextWebFluxFilterTest {

  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929b0e0e4736";
  private static final String SPAN_ID = "00f067aa0ba902b7";

  private final MicrometerProblemContextWebFluxFilter filter =
      new MicrometerProblemContextWebFluxFilter(tracingHeaderName("X-Trace-Id"));

  @Test
  void givenCurrentSpan_whenFilter_thenTraceIdAndSpanIdOfSpanAreUsed() {
    MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/"));
    withSpan(exchange, span(TRACE_ID, SPAN_ID));

    filter(exchange);

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo(TRACE_ID);
    assertThat(problemContext(exchange).get("traceId")).isEqualTo(TRACE_ID);
    assertThat(problemContext(exchange).get("spanId")).isEqualTo(SPAN_ID);
    assertThat(exchange.getResponse().getHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);
  }

  @Test
  void givenCurrentSpanAndTracingHeader_whenFilter_thenTraceIdOfSpanTakesPrecedence() {
    MockServerWebExchange exchange =
        exchange(MockServerHttpRequest.get("/").header("X-Trace-Id", "header-trace"));
    withSpan(exchange, span(TRACE_ID, SPAN_ID));

    filter(exchange);

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo(TRACE_ID);
  }

  @Test
  void givenCurrentSpanAndTraceIdAttribute_whenFilter_thenTraceIdOfAttributeTakesPrecedence() {
    MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/"));
    withSpan(exchange, span(TRACE_ID, SPAN_ID));
    exchange.getAttributes().put(TRACE_ID_ATTRIBUTE, "attribute-trace");

    filter(exchange);

    assertThat(problemContext(exchange).get("traceId")).isEqualTo("attribute-trace");
    assertThat(problemContext(exchange).get("spanId")).isNull();
  }

  @Test
  void givenAssignedContextWithSpanId_whenFilter_thenSpanIdIsKept() {
    MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/"));
    withSpan(exchange, span(TRACE_ID, SPAN_ID));
    exchange
        .getAttributes()
        .put(
            PROBLEM_CONTEXT_ATTRIBUTE,
            ProblemContext.create().put("traceId", TRACE_ID).put("spanId", "existing-span"));

    filter(exchange);

    assertThat(problemContext(exchange).get("traceId")).isEqualTo(TRACE_ID);
    assertThat(problemContext(exchange).get("spanId")).isEqualTo("existing-span");
  }

  @Test
  void givenAssignedContextWithoutSpanId_whenFilter_thenSpanIdOfSameTraceIsAdded() {
    MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/"));
    withSpan(exchange, span(TRACE_ID, SPAN_ID));
    exchange
        .getAttributes()
        .put(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create().put("traceId", TRACE_ID));

    filter(exchange);

    assertThat(problemContext(exchange).get("spanId")).isEqualTo(SPAN_ID);
  }

  @Test
  void givenNoopSpan_whenFilter_thenTracingHeaderIsUsed() {
    MockServerWebExchange exchange =
        exchange(MockServerHttpRequest.get("/").header("X-Trace-Id", "header-trace"));
    withSpan(exchange, Span.NOOP);

    filter(exchange);

    assertThat(problemContext(exchange).get("traceId")).isEqualTo("header-trace");
    assertThat(problemContext(exchange).get("spanId")).isNull();
  }

  @Test
  void givenObservationWithoutSpan_whenFilter_thenTracingHeaderIsUsed() {
    MockServerWebExchange exchange =
        exchange(MockServerHttpRequest.get("/").header("X-Trace-Id", "header-trace"));
    exchange
        .getAttributes()
        .put(
            ServerRequestObservationContext.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
            observationContext(exchange));

    filter(exchange);

    assertThat(problemContext(exchange).get("traceId")).isEqualTo("header-trace");
    assertThat(problemContext(exchange).get("spanId")).isNull();
  }

  @Test
  void givenNoObservationAndNoTracingHeader_whenFilter_thenTraceIdIsGenerated() {
    MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/"));

    filter(exchange);

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE)).matches("^[0-9a-f]{32}$");
    assertThat(problemContext(exchange).get("spanId")).isNull();
  }

  private void filter(MockServerWebExchange exchange) {
    filter.filter(exchange, ex -> Mono.empty()).block();
  }

  private static MockServerWebExchange exchange(MockServerHttpRequest.BaseBuilder<?> request) {
    return MockServerWebExchange.from(request);
  }

  private static void withSpan(MockServerWebExchange exchange, Span span) {
    TracingContext tracingContext = new TracingContext();
    tracingContext.setSpan(span);
    ServerRequestObservationContext observationContext = observationContext(exchange);
    observationContext.put(TracingContext.class, tracingContext);
    exchange
        .getAttributes()
        .put(
            ServerRequestObservationContext.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
            observationContext);
  }

  private static ServerRequestObservationContext observationContext(
      MockServerWebExchange exchange) {
    return new ServerRequestObservationContext(
        exchange.getRequest(), exchange.getResponse(), exchange.getAttributes());
  }

  private static ProblemContext problemContext(MockServerWebExchange exchange) {
    return (ProblemContext)
        Objects.requireNonNull(exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
  }

  private static Span span(String traceId, String spanId) {
    TraceContext traceContext = mock(TraceContext.class);
    when(traceContext.traceId()).thenReturn(traceId);
    when(traceContext.spanId()).thenReturn(spanId);
    Span span = mock(Span.class);
    when(span.context()).thenReturn(traceContext);
    return span;
  }

  private static ProblemContextSettings tracingHeaderName(String headerName) {
    return new ProblemContextSettings() {
      @Override
      public String getTracingHeaderName() {
        return headerName;
      }
    };
  }
}
