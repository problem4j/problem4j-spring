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

import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProblemContextWebMvcFilterTest {

  @Test
  void givenNoTracingHeaderName_whenFilter_thenGeneratedTraceIdIsLowercase32HexCharacters()
      throws Exception {
    ProblemContextWebMvcFilter filter = new ProblemContextWebMvcFilter(() -> null);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat((String) request.getAttribute(TRACE_ID_ATTRIBUTE)).matches("^[0-9a-f]{32}$");
  }

  @Test
  void
      givenTracingHeaderNameConfigured_whenNoHeaderInRequest_thenGeneratedTraceIdIsLowercase32HexCharacters()
          throws Exception {
    ProblemContextWebMvcFilter filter = new ProblemContextWebMvcFilter(() -> "X-Trace-Id");
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat((String) request.getAttribute(TRACE_ID_ATTRIBUTE)).matches("^[0-9a-f]{32}$");
  }

  @Test
  void givenTracingHeaderNameConfigured_whenHeaderPresentInRequest_thenUsesRequestHeaderAsTraceId()
      throws Exception {
    ProblemContextWebMvcFilter filter = new ProblemContextWebMvcFilter(() -> "X-Trace-Id");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Trace-Id", "custom-trace-id");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat((String) request.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo("custom-trace-id");
  }
}
