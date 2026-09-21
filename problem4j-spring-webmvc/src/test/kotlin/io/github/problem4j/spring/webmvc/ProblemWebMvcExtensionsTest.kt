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
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
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
}
