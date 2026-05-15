package io.github.problem4j.spring.web.parameter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ViolationExtensionsTest {

  @Test
  fun givenViolation_whenDestructuring_thenComponentsMatchFieldAndError() {
    val violation = Violation("age", "must be positive")

    val (field, error) = violation

    assertThat(field).isEqualTo("age")
    assertThat(error).isEqualTo("must be positive")
  }
}
