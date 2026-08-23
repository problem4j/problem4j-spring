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
