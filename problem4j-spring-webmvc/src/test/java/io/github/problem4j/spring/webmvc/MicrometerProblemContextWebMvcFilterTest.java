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

package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.problem4j.core.ProblemContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler.TracingContext;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.ServerHttpObservationFilter;

class MicrometerProblemContextWebMvcFilterTest {

  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929b0e0e4736";
  private static final String SPAN_ID = "00f067aa0ba902b7";

  private final MicrometerProblemContextWebMvcFilter filter =
      new MicrometerProblemContextWebMvcFilter(() -> "X-Trace-Id");

  private final MockHttpServletRequest request = new MockHttpServletRequest();
  private final MockHttpServletResponse response = new MockHttpServletResponse();

  @Test
  void givenCurrentSpan_whenFilter_thenTraceIdAndSpanIdOfSpanAreUsed() throws Exception {
    withSpan(span(TRACE_ID, SPAN_ID));

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(request.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo(TRACE_ID);
    assertThat(problemContext().get("traceId")).isEqualTo(TRACE_ID);
    assertThat(problemContext().get("spanId")).isEqualTo(SPAN_ID);
    assertThat(response.getHeader("X-Trace-Id")).isEqualTo(TRACE_ID);
  }

  @Test
  void givenCurrentSpanAndTracingHeader_whenFilter_thenTraceIdOfSpanTakesPrecedence()
      throws Exception {
    withSpan(span(TRACE_ID, SPAN_ID));
    request.addHeader("X-Trace-Id", "header-trace");

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(request.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo(TRACE_ID);
  }

  @Test
  void givenCurrentSpanAndTraceIdAttribute_whenFilter_thenTraceIdOfAttributeTakesPrecedence()
      throws Exception {
    withSpan(span(TRACE_ID, SPAN_ID));
    request.setAttribute(TRACE_ID_ATTRIBUTE, "attribute-trace");

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(problemContext().get("traceId")).isEqualTo("attribute-trace");
    assertThat(problemContext().get("spanId")).isNull();
  }

  @Test
  void givenAssignedContextWithSpanId_whenFilter_thenSpanIdIsKept() throws Exception {
    withSpan(span(TRACE_ID, SPAN_ID));
    request.setAttribute(
        PROBLEM_CONTEXT_ATTRIBUTE,
        ProblemContext.create().put("traceId", TRACE_ID).put("spanId", "existing-span"));

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(problemContext().get("traceId")).isEqualTo(TRACE_ID);
    assertThat(problemContext().get("spanId")).isEqualTo("existing-span");
  }

  @Test
  void givenAssignedContextWithoutSpanId_whenFilter_thenSpanIdOfSameTraceIsAdded()
      throws Exception {
    withSpan(span(TRACE_ID, SPAN_ID));
    request.setAttribute(
        PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create().put("traceId", TRACE_ID));

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(problemContext().get("spanId")).isEqualTo(SPAN_ID);
  }

  @Test
  void givenNoopSpan_whenFilter_thenTracingHeaderIsUsed() throws Exception {
    withSpan(Span.NOOP);
    request.addHeader("X-Trace-Id", "header-trace");

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(problemContext().get("traceId")).isEqualTo("header-trace");
    assertThat(problemContext().get("spanId")).isNull();
  }

  @Test
  void givenObservationWithoutSpan_whenFilter_thenTracingHeaderIsUsed() throws Exception {
    request.setAttribute(
        ServerHttpObservationFilter.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
        new ServerRequestObservationContext(request, response));
    request.addHeader("X-Trace-Id", "header-trace");

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(problemContext().get("traceId")).isEqualTo("header-trace");
    assertThat(problemContext().get("spanId")).isNull();
  }

  @Test
  void givenNoObservationAndNoTracingHeader_whenFilter_thenTraceIdIsGenerated() throws Exception {
    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat((String) request.getAttribute(TRACE_ID_ATTRIBUTE)).matches("^[0-9a-f]{32}$");
    assertThat(problemContext().get("spanId")).isNull();
  }

  private void withSpan(Span span) {
    TracingContext tracingContext = new TracingContext();
    tracingContext.setSpan(span);
    ServerRequestObservationContext observationContext =
        new ServerRequestObservationContext(request, response);
    observationContext.put(TracingContext.class, tracingContext);
    request.setAttribute(
        ServerHttpObservationFilter.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE, observationContext);
  }

  private ProblemContext problemContext() {
    return (ProblemContext) Objects.requireNonNull(request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
  }

  private static Span span(String traceId, String spanId) {
    TraceContext traceContext = mock(TraceContext.class);
    when(traceContext.traceId()).thenReturn(traceId);
    when(traceContext.spanId()).thenReturn(spanId);
    Span span = mock(Span.class);
    when(span.context()).thenReturn(traceContext);
    return span;
  }
}
