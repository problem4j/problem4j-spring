package io.github.problem4j.spring.web.resolver

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

class ProblemResolverExtensionsTest {

  @Test
  fun givenExceptionSubtype_whenResolvingWithFunctionalResolver_thenTypedExceptionIsPassed() {
    val resolver =
        problemResolver<IllegalStateException> { _, ex, _, _ -> Problem.of(500, ex.message) }

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
}
