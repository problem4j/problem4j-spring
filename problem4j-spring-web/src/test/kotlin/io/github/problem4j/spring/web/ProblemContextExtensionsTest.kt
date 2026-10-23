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

import io.github.problem4j.core.ProblemContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProblemContextExtensionsTest {

  @Test
  fun givenVarargPairEntries_whenPuttingAllIntoContext_thenAllEntriesArePresent() {
    val context = ProblemContext.create().putAll("userId" to "12345", "traceId" to "abcde")

    assertThat(context.toMap()).containsEntry("userId", "12345").containsEntry("traceId", "abcde")
  }

  @Test
  fun givenKeyAndValue_whenSettingContextByIndex_thenValueIsStored() {
    val context = ProblemContext.create()

    context["userId"] = "12345"

    assertThat(context.toMap()).containsEntry("userId", "12345")
  }

  @Test
  fun givenNullValue_whenSettingContextByIndex_thenExistingValueIsRemoved() {
    val context = ProblemContext.create().put("userId", "12345")

    context["userId"] = null

    assertThat(context.toMap()).doesNotContainKey("userId")
  }

  @Test
  fun givenExistingKey_whenCheckingContextContains_thenTrueIsReturned() {
    val context = ProblemContext.create().put("userId", "12345")

    assertThat("userId" in context).isTrue()
  }

  @Test
  fun givenMissingKey_whenCheckingContextContains_thenFalseIsReturned() {
    val context = ProblemContext.create().put("userId", "12345")

    assertThat("traceId" in context).isFalse()
  }

  @Test
  fun givenVarargPairEntriesWithNullValue_whenPuttingAllIntoContext_thenExistingValueIsRemoved() {
    val context = ProblemContext.create().put("userId", "12345")

    context.putAll("userId" to null, "traceId" to "abcde")

    assertThat(context.toMap()).doesNotContainKey("userId").containsEntry("traceId", "abcde")
  }
}
