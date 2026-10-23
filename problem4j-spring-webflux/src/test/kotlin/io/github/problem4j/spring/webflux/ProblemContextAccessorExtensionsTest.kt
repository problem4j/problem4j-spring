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

import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context

class ProblemContextAccessorExtensionsTest {

  private val context: ProblemContext = ProblemContext.create()
  private val request: ServerWebExchange =
      MockServerWebExchange.from(MockServerHttpRequest.get("/"))

  @Test
  fun givenContextAttribute_whenReadingFromExchange_thenContextIsReturned() {
    val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
    exchange.attributes[PROBLEM_CONTEXT_ATTRIBUTE] = context

    assertThat(exchange.problemContext).isSameAs(context)
  }

  @Test
  fun givenNoContextAttribute_whenReadingFromExchange_thenNullIsReturned() {
    assertThat(request.problemContext).isNull()
  }

  @Test
  fun givenNonContextAttribute_whenReadingFromExchange_thenNullIsReturned() {
    val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
    exchange.attributes[PROBLEM_CONTEXT_ATTRIBUTE] = "text"

    assertThat(exchange.problemContext).isNull()
  }

  @Test
  fun givenContextEntry_whenReadingFromReactorContext_thenContextIsReturned() {
    assertThat(Context.of(PROBLEM_CONTEXT_ATTRIBUTE, context).problemContext).isSameAs(context)
  }

  @Test
  fun givenNoContextEntry_whenReadingFromReactorContext_thenNullIsReturned() {
    assertThat(Context.empty().problemContext).isNull()
  }

  @Test
  fun givenNonContextEntry_whenReadingFromReactorContext_thenNullIsReturned() {
    assertThat(Context.of(PROBLEM_CONTEXT_ATTRIBUTE, "text").problemContext).isNull()
  }

  @Test
  fun givenFilteredExchange_whenReadingDownstream_thenFilterContextIsReturned() {
    val captured = AtomicReference<List<ProblemContext?>>()
    val filter = problemContextWebFluxFilter({ null }) {}
    val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
    val chain = WebFilterChain { ex ->
      Mono.deferContextual { ctx ->
        captured.set(listOf(ex.problemContext, ctx.problemContext))
        Mono.empty()
      }
    }

    filter.filter(exchange, chain).block()

    assertThat(captured.get()).doesNotContainNull().containsOnly(exchange.problemContext)
  }
}
