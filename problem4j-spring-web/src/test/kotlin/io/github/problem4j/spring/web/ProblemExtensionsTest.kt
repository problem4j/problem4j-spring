package io.github.problem4j.spring.web

import io.github.problem4j.core.Problem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ProblemExtensionsTest {

  @Test
  fun givenHttpStatusCode_whenConvertingToProblem_thenStatusAndDetailAreSet() {
    val result = HttpStatus.BAD_REQUEST.toProblem("invalid input")

    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isEqualTo("invalid input")
  }

  @Test
  fun givenProblem_whenConvertingToException_thenExceptionWrapsProblem() {
    val problem = Problem.of("Not Found", 404)

    val result = problem.toException()

    assertThat(result.problem).isEqualTo(problem)
  }

  @Test
  fun givenProblemAndMessage_whenConvertingToException_thenExceptionUsesCustomMessage() {
    val problem = Problem.of("Not Found", 404)

    val result = problem.toException(message = "custom message")

    assertThat(result.message).isEqualTo("custom message")
    assertThat(result.problem).isEqualTo(problem)
  }

  @Test
  fun givenStatusAndBlock_whenBuildingProblem_thenPropertiesAreApplied() {
    val result = problem(400) { detail("bad input") }

    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isEqualTo("bad input")
  }

  @Test
  fun givenNoBlock_whenBuildingProblem_thenOnlyStatusIsSet() {
    val result = problem(400)

    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isNull()
  }

  @Test
  fun givenChainedBuilderCalls_whenBuildingProblem_thenLastReturnedBuilderIsUsed() {
    val result = problem(400) { title("Bad Request").detail("bad input") }

    assertThat(result.title).isEqualTo("Bad Request")
    assertThat(result.detail).isEqualTo("bad input")
  }

  @Test
  fun givenHttpStatusCode_whenBuildingProblem_thenPropertiesAreApplied() {
    val result = problem(HttpStatus.BAD_REQUEST) { detail("bad input") }

    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isEqualTo("bad input")
  }

  @Test
  fun givenHttpStatusCodeAndNoBlock_whenBuildingProblem_thenOnlyStatusIsSet() {
    val result = problem(HttpStatus.BAD_REQUEST)

    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isNull()
  }

  @Test
  fun givenHttpStatusCode_whenSettingBuilderStatus_thenNumericStatusIsUsed() {
    val result = problem(0) { status(HttpStatus.CONFLICT) }

    assertThat(result.status).isEqualTo(409)
  }

  @Test
  fun givenVarargExtensions_whenAddingToBuilder_thenAllExtensionsArePresent() {
    val result = problem(400) { extensions("field" to "email", "reason" to "blank") }

    assertThat(result.extensions).containsEntry("field", "email").containsEntry("reason", "blank")
  }
}
