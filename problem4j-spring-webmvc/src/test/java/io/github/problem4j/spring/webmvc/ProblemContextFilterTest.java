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
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemContextSettings;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProblemContextFilterTest {

  @Test
  void givenNoTracingHeaderName_whenFilter_thenGeneratedTraceIdIsLowercase32HexCharacters()
      throws Exception {
    ProblemContextFilter filter = new ProblemContextFilter(new ProblemContextSettings() {});
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat((String) request.getAttribute(TRACE_ID_ATTRIBUTE)).matches("^[0-9a-f]{32}$");
  }

  @Test
  void
      givenTracingHeaderNameConfigured_whenNoHeaderInRequest_thenGeneratedTraceIdIsLowercase32HexCharacters()
          throws Exception {
    ProblemContextFilter filter = new ProblemContextFilter(tracingHeaderName("X-Trace-Id"));
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat((String) request.getAttribute(TRACE_ID_ATTRIBUTE)).matches("^[0-9a-f]{32}$");
  }

  @Test
  void givenTracingHeaderNameConfigured_whenHeaderPresentInRequest_thenUsesRequestHeaderAsTraceId()
      throws Exception {
    ProblemContextFilter filter = new ProblemContextFilter(tracingHeaderName("X-Trace-Id"));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Trace-Id", "custom-trace-id");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat((String) request.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo("custom-trace-id");
  }

  private static ProblemContextSettings tracingHeaderName(String headerName) {
    return new ProblemContextSettings() {
      @Override
      public String getTracingHeaderName() {
        return headerName;
      }
    };
  }

  @Test
  void givenDefaultOrder_whenSettingOrder_thenOrderIsChanged() {
    ProblemContextSettings settings = new ProblemContextSettings() {};
    ProblemContextFilter filter = new ProblemContextFilter(settings);

    assertThat(filter.getOrder()).isEqualTo(ProblemContextFilter.PRECEDENCE);
    assertThat(ProblemContextFilter.PRECEDENCE).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 10);
    assertThat(filter.getSettings()).isSameAs(settings);
    filter.setOrder(42);
    assertThat(filter.getOrder()).isEqualTo(42);
  }

  @Test
  void givenCustomizer_whenFilter_thenCustomizerAddsEntryToStoredContext() throws Exception {
    ProblemContextFilter filter =
        new ProblemContextFilter(
            new ProblemContextSettings() {},
            List.of((context, req, res) -> context.put("requestId", "req-1")));
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    ProblemContext context =
        requireNonNull((ProblemContext) request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
    assertThat(context.get("requestId")).isEqualTo("req-1");
    assertThat(context.get("traceId")).matches("^[0-9a-f]{32}$");
  }

  @Test
  void givenCustomizerOverridingTraceId_whenFilter_thenAttributesAndHeaderUseOverriddenTraceId()
      throws Exception {
    ProblemContextFilter filter =
        new ProblemContextFilter(
            tracingHeaderName("X-Trace-Id"),
            List.of((context, req, res) -> context.put("traceId", "overridden")));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Trace-Id", "from-header");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    ProblemContext context =
        requireNonNull((ProblemContext) request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
    assertThat(context.get("traceId")).isEqualTo("overridden");
    assertThat((String) request.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo("overridden");
    assertThat(response.getHeader("X-Trace-Id")).isEqualTo("overridden");
  }

  @Test
  void givenMultipleCustomizers_whenFilter_thenInvokedInGivenOrderWithRequestAndResponse()
      throws Exception {
    ProblemContextFilter filter =
        new ProblemContextFilter(
            new ProblemContextSettings() {},
            List.of(
                (context, req, res) -> context.put("user", req.getHeader("X-User") + "-first"),
                (context, req, res) -> context.put("user", context.get("user") + "-second")));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-User", "alice");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    ProblemContext context =
        requireNonNull((ProblemContext) request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
    assertThat(context.get("user")).isEqualTo("alice-first-second");
  }

  @Test
  void givenContextAlreadyInRequest_whenFilter_thenCustomizersAreNotInvoked() throws Exception {
    ProblemContextFilter filter =
        new ProblemContextFilter(
            new ProblemContextSettings() {},
            List.of((context, req, res) -> context.put("requestId", "req-1")));
    MockHttpServletRequest request = new MockHttpServletRequest();
    ProblemContext existing = ProblemContext.create().put("traceId", "existing");
    request.setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, existing);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE)).isSameAs(existing);
    assertThat(existing.toMap()).containsOnlyKeys("traceId");
  }

  @Test
  void givenNoCustomizers_whenFilter_thenContextHasOnlyTraceId() throws Exception {
    ProblemContextFilter filter = new ProblemContextFilter(new ProblemContextSettings() {});
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    ProblemContext context =
        requireNonNull((ProblemContext) request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
    assertThat(context.toMap()).containsOnlyKeys("traceId");
  }
}
