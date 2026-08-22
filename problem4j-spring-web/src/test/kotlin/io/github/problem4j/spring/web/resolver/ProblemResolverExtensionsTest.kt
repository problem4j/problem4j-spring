package io.github.problem4j.spring.web.resolver

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

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

  @Test
  fun givenKClassSubtype_whenSubclassingKotlinProblemResolver_thenExceptionClassIsResolved() {
    val resolver =
        object : KotlinProblemResolver(IllegalStateException::class) {
          override fun resolve(
              context: ProblemContext,
              ex: Exception,
              headers: HttpHeaders,
              status: HttpStatusCode,
          ) = Problem.of(500, ex.message)
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
}
