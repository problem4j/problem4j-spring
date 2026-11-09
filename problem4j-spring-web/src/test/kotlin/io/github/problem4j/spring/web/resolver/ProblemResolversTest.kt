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
import io.github.problem4j.spring.web.DefaultProblemFormat
import io.github.problem4j.spring.web.ProblemFormatAware
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

class ProblemResolversTest {

  @Test
  fun givenExceptionSubtype_whenResolvingWithFunctionalResolver_thenTypedExceptionIsPassed() {
    val resolver =
        problemResolver<IllegalStateException> { ex ->
          status(500)
          detail(ex.message)
        }

    assertThat(resolver.exceptionClass).isEqualTo(IllegalStateException::class.java)

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalStateException("boom"),
            HttpHeaders(),
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

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalStateException("boom"),
            HttpHeaders(),
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
            HttpHeaders(),
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
            HttpHeaders(),
            HttpStatus.BAD_REQUEST,
        )

    assertThat(result.status).isEqualTo(400)
    assertThat(result.extensions).containsEntry("message", "boom")
  }

  @Test
  fun givenDslResolverReadingContext_whenResolving_thenContextValueIsUsed() {
    val resolver = problemResolver<IllegalStateException> { extension("userId", context["userId"]) }

    val result =
        resolver.resolve(
            ProblemContext.create().put("userId", "u-123"),
            IllegalStateException("boom"),
            HttpHeaders(),
            HttpStatus.CONFLICT,
        )

    assertThat(result.extensions).containsEntry("userId", "u-123")
  }

  @Test
  fun givenDslResolverSettingHeader_whenResolving_thenHeaderIsAddedToPassedHeaders() {
    val resolver =
        problemResolver<IllegalStateException> { headers.set(HttpHeaders.RETRY_AFTER, "120") }
    val headers = HttpHeaders()

    resolver.resolve(
        ProblemContext.create(),
        IllegalStateException("boom"),
        headers,
        HttpStatus.TOO_MANY_REQUESTS,
    )

    assertThat(headers.getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("120")
  }

  @Test
  fun givenDslResolverNotTouchingHeaders_whenResolving_thenPassedHeadersAreUnchanged() {
    val resolver = problemResolver<IllegalStateException> { status(409) }
    val headers = HttpHeaders()
    headers.set("X-Existing", "value")

    resolver.resolve(
        ProblemContext.create(),
        IllegalStateException("boom"),
        headers,
        HttpStatus.CONFLICT,
    )

    assertThat(headers.toSingleValueMap()).containsExactlyEntriesOf(mapOf("X-Existing" to "value"))
  }

  @Test
  fun givenProblemFormatSet_whenResolvingWithFormatDetail_thenDetailIsFormatted() {
    val resolver = problemResolver<IllegalStateException> { ex -> detail(formatDetail(ex.message)) }
    (resolver as ProblemFormatAware).setProblemFormat(
        DefaultProblemFormat(ProblemProperties.DetailFormat.UPPERCASE),
    )

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalStateException("boom"),
            HttpHeaders(),
            HttpStatus.CONFLICT,
        )

    assertThat(result.detail).isEqualTo("BOOM")
  }

  @Test
  fun givenProblemFormatSet_whenResolvingWithoutFormatDetail_thenDetailIsNotFormatted() {
    val resolver = problemResolver<IllegalStateException> { ex -> detail(ex.message) }
    (resolver as ProblemFormatAware).setProblemFormat(
        DefaultProblemFormat(ProblemProperties.DetailFormat.UPPERCASE),
    )

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalStateException("boom"),
            HttpHeaders(),
            HttpStatus.CONFLICT,
        )

    assertThat(result.detail).isEqualTo("boom")
  }

  @Test
  fun givenNoProblemFormatSet_whenResolvingWithFormatDetail_thenDetailIsUnchanged() {
    val resolver = problemResolver<IllegalStateException> { ex -> detail(formatDetail(ex.message)) }

    val result =
        resolver.resolve(
            ProblemContext.create(),
            IllegalStateException("Boom"),
            HttpHeaders(),
            HttpStatus.CONFLICT,
        )

    assertThat(result.detail).isEqualTo("Boom")
  }

  @Test
  fun givenExceptionOfOtherType_whenResolving_thenClassCastExceptionIsThrown() {
    val resolver = problemResolver<IllegalStateException> { status(409) }

    assertThatThrownBy {
          resolver.resolve(
              ProblemContext.create(),
              IllegalArgumentException("boom"),
              HttpHeaders(),
              HttpStatus.CONFLICT,
          )
        }
        .isInstanceOf(ClassCastException::class.java)
  }
}
