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

package io.github.problem4j.spring.webflux

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE
import io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class ProblemWebFluxExtensionsTest {

  private val context: ProblemContext = ProblemContext.create()
  private val problem: Problem = Problem.of(409)
  private val request: ServerWebExchange =
      MockServerWebExchange.from(MockServerHttpRequest.get("/"))

  @Test
  fun givenFullInspector_whenInspecting_thenArgumentsArePassed() {
    val captured = AtomicReference<List<Any>>()
    val ex = IllegalStateException("boom")
    val inspector = adviceWebFluxInspector { ctx, problem, ex, headers, status, request ->
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
    val inspector = adviceWebFluxInspector { ctx, problem, ex, request ->
      captured.set(listOf(ctx, problem, ex, request))
    }

    inspector.inspect(context, problem, ex, HttpHeaders.EMPTY, HttpStatus.CONFLICT, request)

    assertThat(captured.get()).containsExactly(context, problem, ex, request)
  }

  @Test
  fun givenShortInspector_whenInspecting_thenProblemAndExceptionArePassed() {
    val captured = AtomicReference<List<Any>>()
    val ex = IllegalStateException("boom")
    val inspector = adviceWebFluxInspector { problem, ex -> captured.set(listOf(problem, ex)) }

    inspector.inspect(context, problem, ex, HttpHeaders.EMPTY, HttpStatus.CONFLICT, request)

    assertThat(captured.get()).containsExactly(problem, ex)
  }

  @Test
  fun givenInspectorNotInvoked_whenCreating_thenLambdaIsNotCalled() {
    val captured = AtomicReference<Exception>()

    adviceWebFluxInspector { _, ex -> captured.set(ex) }

    assertThat(captured.get()).isNull()
  }

  @Test
  fun givenCreateTraceId_whenFiltering_thenCustomTraceIdIsUsed() {
    val filter = problemContextWebFluxFilter({ "X-Trace-Id" }) { createTraceId { _ -> "custom" } }
    val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

    filter.filter(exchange) { Mono.empty() }.block()

    assertThat(exchange.getAttribute<String>(TRACE_ID_ATTRIBUTE)).isEqualTo("custom")
    assertThat(exchange.response.headers.getFirst("X-Trace-Id")).isEqualTo("custom")
  }

  @Test
  fun givenCreateTraceIdAndTracingHeader_whenFiltering_thenHeaderTraceIdIsUsed() {
    val filter = problemContextWebFluxFilter({ "X-Trace-Id" }) { createTraceId { _ -> "custom" } }
    val exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/").header("X-Trace-Id", "from-header")
        )

    filter.filter(exchange) { Mono.empty() }.block()

    assertThat(exchange.getAttribute<String>(TRACE_ID_ATTRIBUTE)).isEqualTo("from-header")
  }

  @Test
  fun givenNoCustomizations_whenFiltering_thenDefaultTraceIdIsGenerated() {
    val filter = problemContextWebFluxFilter({ null }) {}
    val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

    filter.filter(exchange) { Mono.empty() }.block()

    assertThat(exchange.getAttribute<String>(TRACE_ID_ATTRIBUTE)).hasSize(32)
  }

  @Test
  fun givenCallbacks_whenFiltering_thenInvokedInOrderAroundChain() {
    val events = mutableListOf<String>()
    val filter =
        problemContextWebFluxFilter({ null }) {
          onContext { _, context -> events.add("context1:" + context.get("traceId")) }
          onContext { _, _ -> events.add("context2") }
          onComplete { _, context -> events.add("complete1:" + context.get("traceId")) }
          onComplete { _, _ -> events.add("complete2") }
          createTraceId { _ -> "abc" }
        }
    val chain = WebFilterChain { Mono.fromRunnable { events.add("chain") } }

    filter.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/")), chain).block()

    assertThat(events)
        .containsExactly("context1:abc", "context2", "chain", "complete1:abc", "complete2")
  }

  @Test
  fun givenContextWrite_whenFiltering_thenReactorContextIsEnriched() {
    val captured = AtomicReference<List<Any?>>()
    val filter =
        problemContextWebFluxFilter({ null }) {
          createTraceId { _ -> "abc" }
          contextWrite { ctx, _, context -> ctx.put("first", context.get("traceId") ?: "") }
          contextWrite { ctx, _, _ -> ctx.put("second", ctx.get<String>("first") + "!") }
        }
    val chain = WebFilterChain {
      Mono.deferContextual { ctx ->
        captured.set(
            listOf(
                ctx.get<String>(TRACE_ID_ATTRIBUTE),
                ctx.get<String>("first"),
                ctx.get<String>("second"),
            )
        )
        Mono.empty()
      }
    }

    filter.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/")), chain).block()

    assertThat(captured.get()).containsExactly("abc", "abc", "abc!")
  }

  @Test
  fun givenFailingChain_whenFiltering_thenOnCompleteIsInvokedAndErrorPropagates() {
    val captured = AtomicReference<ProblemContext>()
    val filter =
        problemContextWebFluxFilter({ null }) { onComplete { _, context -> captured.set(context) } }
    val chain = WebFilterChain { Mono.error(IllegalStateException("boom")) }

    assertThatThrownBy {
          filter.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/")), chain).block()
        }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("boom")
    assertThat(captured.get()).isNotNull()
  }

  @Test
  fun givenOnComplete_whenNotSubscribed_thenCallbackIsNotInvoked() {
    val captured = AtomicReference<ProblemContext>()
    val filter =
        problemContextWebFluxFilter({ null }) { onComplete { _, context -> captured.set(context) } }

    filter.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/"))) { Mono.empty() }

    assertThat(captured.get()).isNull()
  }

  @Test
  fun givenExistingContext_whenFiltering_thenCallbacksReceiveIt() {
    val existing = ProblemContext.create().put("traceId", "existing")
    val captured = mutableListOf<ProblemContext>()
    val filter =
        problemContextWebFluxFilter({ null }) {
          onContext { _, context -> captured.add(context) }
          onComplete { _, context -> captured.add(context) }
        }
    val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
    exchange.attributes[PROBLEM_CONTEXT_ATTRIBUTE] = existing

    filter.filter(exchange) { Mono.empty() }.block()

    assertThat(captured).containsExactly(existing, existing)
  }
}
