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

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE
import io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE
import jakarta.servlet.FilterChain
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest

class ProblemWebMvcExtensionsTest {

  private val context: ProblemContext = ProblemContext.create()
  private val problem: Problem = Problem.of(409)
  private val request: WebRequest = ServletWebRequest(MockHttpServletRequest())

  @Test
  fun givenFullInspector_whenInspecting_thenArgumentsArePassed() {
    val captured = AtomicReference<List<Any>>()
    val ex = IllegalStateException("boom")
    val inspector = adviceWebMvcInspector { ctx, problem, ex, headers, status, request ->
      captured.set(listOf(ctx, problem, ex, headers, status, request))
    }

    inspector.inspect(context, problem, ex, HttpHeaders.EMPTY, HttpStatus.CONFLICT, request)

    assertThat(captured.get())
        .containsExactly(context, problem, ex, HttpHeaders.EMPTY, HttpStatus.CONFLICT, request)
  }

  @Test
  fun givenInspectorWithoutHeadersAndStatus_whenInspecting_thenRemainingArgumentsArePassed() {
    val captured = AtomicReference<List<Any>>()
    val ex = IllegalStateException("boom")
    val inspector = adviceWebMvcInspector { ctx, problem, ex, request ->
      captured.set(listOf(ctx, problem, ex, request))
    }

    inspector.inspect(context, problem, ex, HttpHeaders.EMPTY, HttpStatus.CONFLICT, request)

    assertThat(captured.get()).containsExactly(context, problem, ex, request)
  }

  @Test
  fun givenShortInspector_whenInspecting_thenProblemAndExceptionArePassed() {
    val captured = AtomicReference<List<Any>>()
    val ex = IllegalStateException("boom")
    val inspector = adviceWebMvcInspector { problem, ex -> captured.set(listOf(problem, ex)) }

    inspector.inspect(context, problem, ex, HttpHeaders.EMPTY, HttpStatus.CONFLICT, request)

    assertThat(captured.get()).containsExactly(problem, ex)
  }

  @Test
  fun givenInspectorNotInvoked_whenCreating_thenLambdaIsNotCalled() {
    val captured = AtomicReference<Exception>()

    adviceWebMvcInspector { _, ex -> captured.set(ex) }

    assertThat(captured.get()).isNull()
  }

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
  fun givenFailingChainAndFailingOnComplete_whenFiltering_thenChainExceptionPropagatesWithSuppressed() {
    val filter =
        problemContextWebMvcFilter({ null }) {
          onComplete { _, _, _ -> throw IllegalArgumentException("callback") }
        }
    val chain = FilterChain { _, _ -> throw IllegalStateException("boom") }

    assertThatThrownBy {
          filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), chain)
        }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("boom")
        .hasSuppressedException(IllegalArgumentException("callback"))
  }

  @Test
  fun givenFailingOnComplete_whenFiltering_thenCallbackExceptionPropagates() {
    val filter =
        problemContextWebMvcFilter({ null }) {
          onComplete { _, _, _ -> throw IllegalArgumentException("callback") }
        }

    assertThatThrownBy {
          filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), MockFilterChain())
        }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("callback")
  }

  @Test
  fun givenResponseCompleteCallbacks_whenFiltering_thenInvokedInOrderBeforeOnComplete() {
    val events = mutableListOf<String>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onComplete { _, _, _ -> events.add("complete") }
          onResponseComplete { _, _, context ->
            events.add("responseComplete1:" + context.get("traceId"))
          }
          onResponseComplete { _, _, _ -> events.add("responseComplete2") }
          createTraceId { _, _ -> "abc" }
        }
    val chain = FilterChain { _, _ -> events.add("chain") }

    filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), chain)

    assertThat(events)
        .containsExactly("chain", "responseComplete1:abc", "responseComplete2", "complete")
  }

  @Test
  fun givenFailingChain_whenFiltering_thenOnResponseCompleteIsInvokedAndExceptionPropagates() {
    val captured = AtomicReference<ProblemContext>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onResponseComplete { _, _, context -> captured.set(context) }
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
  fun givenFailingOnResponseComplete_whenFiltering_thenOnCompleteIsInvokedAndExceptionPropagates() {
    val events = mutableListOf<String>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onResponseComplete { _, _, _ -> throw IllegalArgumentException("responseComplete") }
          onComplete { _, _, _ -> events.add("complete") }
        }

    assertThatThrownBy {
          filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), MockFilterChain())
        }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("responseComplete")
    assertThat(events).containsExactly("complete")
  }

  @Test
  fun givenFailingOnResponseCompleteAndOnComplete_whenFiltering_thenSecondIsSuppressedByFirst() {
    val filter =
        problemContextWebMvcFilter({ null }) {
          onResponseComplete { _, _, _ -> throw IllegalArgumentException("responseComplete") }
          onComplete { _, _, _ -> throw IllegalArgumentException("complete") }
        }

    assertThatThrownBy {
          filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), MockFilterChain())
        }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("responseComplete")
        .hasSuppressedException(IllegalArgumentException("complete"))
  }

  @Test
  fun givenAsyncStartedByChain_whenFiltering_thenOnCompleteRunsImmediatelyAndOnResponseCompleteIsDeferred() {
    val events = mutableListOf<String>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onContext { _, _, _ -> events.add("context") }
          onComplete { _, _, _ -> events.add("complete") }
          onResponseComplete { _, _, context ->
            events.add("responseComplete:" + context.get("traceId"))
          }
          createTraceId { _, _ -> "abc" }
        }
    val request = MockHttpServletRequest().apply { isAsyncSupported = true }
    val chain = FilterChain { req, _ ->
      req.startAsync()
      events.add("chain")
    }

    filter.doFilter(request, MockHttpServletResponse(), chain)

    assertThat(events).containsExactly("context", "chain", "complete")

    request.asyncContext!!.complete()

    assertThat(events).containsExactly("context", "chain", "complete", "responseComplete:abc")
  }

  @Test
  fun givenAsyncStartedByChainAndFailingOnResponseComplete_whenAsyncCompletes_thenExceptionIsNotPropagated() {
    val filter =
        problemContextWebMvcFilter({ null }) {
          onResponseComplete { _, _, _ -> throw IllegalArgumentException("callback") }
        }
    val request = MockHttpServletRequest().apply { isAsyncSupported = true }
    val chain = FilterChain { req, _ -> req.startAsync() }

    filter.doFilter(request, MockHttpServletResponse(), chain)

    assertThatCode { request.asyncContext!!.complete() }.doesNotThrowAnyException()
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

  @Test
  fun givenContextAttribute_whenReadingFromServletRequest_thenContextIsReturned() {
    val request =
        MockHttpServletRequest().apply { setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, context) }

    assertThat(request.problemContext).isSameAs(context)
  }

  @Test
  fun givenNoContextAttribute_whenReadingFromServletRequest_thenNullIsReturned() {
    assertThat(MockHttpServletRequest().problemContext).isNull()
  }

  @Test
  fun givenNonContextAttribute_whenReadingFromServletRequest_thenNullIsReturned() {
    val request = MockHttpServletRequest().apply { setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, "text") }

    assertThat(request.problemContext).isNull()
  }

  @Test
  fun givenContextAttribute_whenReadingFromWebRequest_thenContextIsReturned() {
    val servletRequest =
        MockHttpServletRequest().apply { setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, context) }

    assertThat(ServletWebRequest(servletRequest).problemContext).isSameAs(context)
  }

  @Test
  fun givenNoContextAttribute_whenReadingFromWebRequest_thenNullIsReturned() {
    assertThat(request.problemContext).isNull()
  }

  @Test
  fun givenNonContextAttribute_whenReadingFromWebRequest_thenNullIsReturned() {
    val servletRequest =
        MockHttpServletRequest().apply { setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, "text") }

    assertThat(ServletWebRequest(servletRequest).problemContext).isNull()
  }

  @Test
  fun givenFilteredRequest_whenReadingFromServletRequest_thenFilterContextIsReturned() {
    val captured = AtomicReference<ProblemContext>()
    val filter =
        problemContextWebMvcFilter({ null }) {
          onContext { request, _, context ->
            assertThat(request.problemContext).isSameAs(context)
            captured.set(context)
          }
        }
    val request = MockHttpServletRequest()

    filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

    assertThat(request.problemContext).isNotNull().isSameAs(captured.get())
  }
}
