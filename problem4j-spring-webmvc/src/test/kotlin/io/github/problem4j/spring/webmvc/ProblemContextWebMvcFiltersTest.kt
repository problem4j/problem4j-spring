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

package io.github.problem4j.spring.webmvc

import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE
import io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE
import jakarta.servlet.FilterChain
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class ProblemContextWebMvcFiltersTest {

  @Test
  fun givenCreateTraceId_whenFiltering_thenCustomTraceIdIsUsed() {
    val filter = problemContextWebMvcFilter({ "X-Trace-Id" }) { createTraceId { _, _ -> "custom" } }
    val request = MockHttpServletRequest()
    val response = MockHttpServletResponse()

    filter.doFilter(request, response, MockFilterChain())

    assertThat(request.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo("custom")
    assertThat(response.getHeader("X-Trace-Id")).isEqualTo("custom")
  }

  @Test
  fun givenCreateTraceIdAndTracingHeader_whenFiltering_thenHeaderTraceIdIsUsed() {
    val filter = problemContextWebMvcFilter({ "X-Trace-Id" }) { createTraceId { _, _ -> "custom" } }
    val request = MockHttpServletRequest().apply { addHeader("X-Trace-Id", "from-header") }

    filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

    assertThat(request.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo("from-header")
  }

  @Test
  fun givenNoCustomizations_whenFiltering_thenDefaultTraceIdIsGenerated() {
    val filter = problemContextWebMvcFilter({ null }) {}
    val request = MockHttpServletRequest()

    filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

    assertThat(request.getAttribute(TRACE_ID_ATTRIBUTE) as String).hasSize(32)
  }

  @Test
  fun givenCallbacks_whenFiltering_thenInvokedInOrderAroundChain() {
    val events = mutableListOf<String>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onContext { _, _, context -> events.add("context1:" + context.get("traceId")) }
          onContext { _, _, _ -> events.add("context2") }
          onComplete { _, _, context -> events.add("complete1:" + context.get("traceId")) }
          onComplete { _, _, _ -> events.add("complete2") }
          createTraceId { _, _ -> "abc" }
        }
    val chain = FilterChain { _, _ -> events.add("chain") }

    filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), chain)

    assertThat(events)
        .containsExactly("context1:abc", "context2", "chain", "complete1:abc", "complete2")
  }

  @Test
  fun givenOnContext_whenFiltering_thenContextIsAlreadyAssignedToRequest() {
    val captured = AtomicReference<Any>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onContext { request, _, _ ->
            captured.set(request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE))
          }
        }
    val request = MockHttpServletRequest()

    filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

    assertThat(captured.get()).isSameAs(request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE))
  }

  @Test
  fun givenFailingChain_whenFiltering_thenOnCompleteIsInvokedAndExceptionPropagates() {
    val captured = AtomicReference<ProblemContext>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onComplete { _, _, context -> captured.set(context) }
        }
    val chain = FilterChain { _, _ -> throw IllegalStateException("boom") }

    assertThatThrownBy {
          filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), chain)
        }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("boom")
    assertThat(captured.get()).isNotNull()
  }

  @Test
  fun givenExistingContext_whenFiltering_thenCallbacksReceiveIt() {
    val existing = ProblemContext.create().put("traceId", "existing")
    val captured = mutableListOf<ProblemContext>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onContext { _, _, context -> captured.add(context) }
          onComplete { _, _, context -> captured.add(context) }
        }
    val request =
        MockHttpServletRequest().apply { setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, existing) }

    filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

    assertThat(captured).containsExactly(existing, existing)
  }
}
