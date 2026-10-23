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

package io.github.problem4j.spring.web

import io.github.problem4j.core.Problem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ProblemBuilderExtensionsTest {

  @Test
  fun givenHttpStatusCode_whenSettingStatusOnBuilder_thenNumericStatusIsUsed() {
    val result = Problem.builder().status(HttpStatus.CONFLICT).build()

    assertThat(result.status).isEqualTo(409)
  }

  @Test
  fun givenVarargPairs_whenAddingExtensionsToBuilder_thenAllExtensionsArePresent() {
    val result =
        Problem.builder().status(400).extensions("field" to "email", "reason" to "blank").build()

    assertThat(result.extensions).containsEntry("field", "email").containsEntry("reason", "blank")
  }

  @Test
  fun givenVarargPairWithNullValue_whenAddingExtensionsToBuilder_thenExistingExtensionIsRemoved() {
    val result =
        Problem.builder()
            .status(400)
            .extension("field", "email")
            .extensions("field" to null, "reason" to "blank")
            .build()

    assertThat(result.extensions).doesNotContainKey("field").containsEntry("reason", "blank")
  }
}
