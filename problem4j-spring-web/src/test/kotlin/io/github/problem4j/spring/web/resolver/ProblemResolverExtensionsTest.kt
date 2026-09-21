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

package io.github.problem4j.spring.web.resolver

import io.github.problem4j.core.ProblemContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

class ProblemResolverExtensionsTest {

  @Test
  fun givenExceptionSubtype_whenResolvingWithFunctionalResolver_thenTypedExceptionIsPassed() {
    val resolver =
        problemResolver<IllegalStateException> { _, ex ->
          status(500)
          detail(ex.message)
        }

    assertThat(resolver.exceptionClass).isEqualTo(IllegalStateException::class.java)

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalStateException("boom"),
            HttpHeaders.EMPTY,
            HttpStatus.INTERNAL_SERVER_ERROR,
        )

    assertThat(result.detail).isEqualTo("boom")
    assertThat(result.status).isEqualTo(500)
  }

  @Test
  fun givenDslResolver_whenResolving_thenBlockIsAppliedOverPassedStatus() {
    val resolver =
        problemResolver<IllegalStateException> { ex ->
          title("Conflict")
          detail(ex.message)
        }

    assertThat(resolver.exceptionClass).isEqualTo(IllegalStateException::class.java)

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalStateException("boom"),
            HttpHeaders.EMPTY,
            HttpStatus.CONFLICT,
        )

    assertThat(result.status).isEqualTo(409)
    assertThat(result.title).isEqualTo("Conflict")
    assertThat(result.detail).isEqualTo("boom")
  }

  @Test
  fun givenDslResolverOverridingStatus_whenResolving_thenStatusFromBlockIsUsed() {
    val resolver = problemResolver<IllegalStateException> { status(HttpStatus.BAD_REQUEST) }

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalStateException("boom"),
            HttpHeaders.EMPTY,
            HttpStatus.INTERNAL_SERVER_ERROR,
        )

    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isNull()
  }

  @Test
  fun givenDslResolverUsingImplicitParameter_whenResolving_thenTypedExceptionIsPassed() {
    val resolver = problemResolver<IllegalArgumentException> { extension("message", it.message) }

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalArgumentException("boom"),
            HttpHeaders.EMPTY,
            HttpStatus.BAD_REQUEST,
        )

    assertThat(result.status).isEqualTo(400)
    assertThat(result.extensions).containsEntry("message", "boom")
  }
}
