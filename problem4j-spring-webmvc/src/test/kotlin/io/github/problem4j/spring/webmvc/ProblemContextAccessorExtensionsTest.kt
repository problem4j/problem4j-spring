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
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest

class ProblemContextAccessorExtensionsTest {

  private val context: ProblemContext = ProblemContext.create()
  private val request: WebRequest = ServletWebRequest(MockHttpServletRequest())

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
