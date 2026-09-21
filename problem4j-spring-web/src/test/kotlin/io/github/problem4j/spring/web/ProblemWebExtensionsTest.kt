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
import io.github.problem4j.core.ProblemContext
import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ProblemWebExtensionsTest {

  @Test
  fun givenBlock_whenBuildingProblem_thenPropertiesAreApplied() {
    val result = buildProblem {
      status(400)
      detail("bad input")
    }

    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isEqualTo("bad input")
  }

  @Test
  fun givenEmptyBlock_whenBuildingProblem_thenNoPropertiesAreSet() {
    val result = buildProblem {}

    assertThat(result.status).isZero()
    assertThat(result.detail).isNull()
    assertThat(result.extensions).isEmpty()
  }

  @Test
  fun givenMultipleStatements_whenBuildingProblem_thenAllPropertiesAreApplied() {
    val result = buildProblem {
      title("Bad Request")
      status(400)
      detail("bad input")
    }

    assertThat(result.title).isEqualTo("Bad Request")
    assertThat(result.detail).isEqualTo("bad input")
  }

  @Test
  fun givenHttpStatusCodeInBlock_whenBuildingProblem_thenNumericStatusIsUsed() {
    val result = buildProblem {
      status(HttpStatus.BAD_REQUEST)
      detail("bad input")
    }

    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isEqualTo("bad input")
  }

  @Test
  fun givenVarargExtensions_whenAddingToBuilder_thenAllExtensionsArePresent() {
    val result = buildProblem {
      status(400)
      extensions("field" to "email", "reason" to "blank")
    }

    assertThat(result.extensions).containsEntry("field", "email").containsEntry("reason", "blank")
  }

  @Test
  fun givenStringTypeAndInstance_whenBuildingProblem_thenUrisAreApplied() {
    val result = buildProblem {
      type("https://example.org/invalid")
      status(400)
      instance("/users/1")
    }

    assertThat(result.type).isEqualTo(URI.create("https://example.org/invalid"))
    assertThat(result.instance).isEqualTo(URI.create("/users/1"))
  }

  @Test
  fun givenUriTypeAndInstance_whenBuildingProblem_thenUrisAreApplied() {
    val result = buildProblem {
      type(URI.create("https://example.org/invalid"))
      status(400)
      instance(URI.create("/users/1"))
    }

    assertThat(result.type).isEqualTo(URI.create("https://example.org/invalid"))
    assertThat(result.instance).isEqualTo(URI.create("/users/1"))
  }

  @Test
  fun givenNullTypeAndInstance_whenBuildingProblem_thenDefaultsAreKept() {
    val result = buildProblem {
      type(null as String?)
      status(400)
      instance(null as String?)
    }

    assertThat(result.type).isEqualTo(Problem.BLANK_TYPE)
    assertThat(result.instance).isNull()
  }

  @Test
  fun givenStatusSetTwiceInBlock_whenBuildingProblem_thenLastStatusWins() {
    val result = buildProblem {
      status(400)
      status(409)
    }

    assertThat(result.status).isEqualTo(409)
  }

  @Test
  fun givenAllExtensionVariants_whenBuildingProblem_thenAllExtensionsArePresent() {
    val result = buildProblem {
      status(400)
      extension("a", 1)
      extension(Problem.extension("b", 2))
      extensions(mapOf("c" to 3))
      extensions(Problem.extension("d", 4), Problem.extension("e", 5))
      extensions(listOf(Problem.extension("f", 6)))
    }

    assertThat(result.extensions)
        .containsEntry("a", 1)
        .containsEntry("b", 2)
        .containsEntry("c", 3)
        .containsEntry("d", 4)
        .containsEntry("e", 5)
        .containsEntry("f", 6)
  }

  @Test
  fun givenExistingExtension_whenIndexingProblem_thenValueIsReturned() {
    val result = buildProblem {
      status(400)
      extension("field", "email")
      extension("count", 3)
    }

    assertThat(result["field"]).isEqualTo("email")
    assertThat(result["count"]).isEqualTo(3)
  }

  @Test
  fun givenMissingExtension_whenIndexingProblem_thenNullIsReturned() {
    val result = buildProblem {
      status(400)
      extension("field", "email")
    }

    assertThat(result["reason"]).isNull()
  }

  @Test
  fun givenNoExtensions_whenIndexingProblem_thenNullIsReturned() {
    val result = buildProblem { status(400) }

    assertThat(result["field"]).isNull()
  }

  @Test
  fun givenMatchingType_whenReadingTypedExtension_thenValueIsReturned() {
    val result = buildProblem {
      status(400)
      extension("attempts", 3)
      extension("field", "email")
    }

    assertThat(result.extension<Int>("attempts")).isEqualTo(3)
    assertThat(result.extension<String>("field")).isEqualTo("email")
  }

  @Test
  fun givenMismatchedType_whenReadingTypedExtension_thenNullIsReturned() {
    val result = buildProblem {
      status(400)
      extension("attempts", 3)
    }

    assertThat(result.extension<String>("attempts")).isNull()
  }

  @Test
  fun givenMissingExtension_whenReadingTypedExtension_thenNullIsReturned() {
    val result = buildProblem { status(400) }

    assertThat(result.extension<String>("field")).isNull()
  }

  @Test
  fun givenExistingExtension_whenCheckingContains_thenTrueIsReturned() {
    val result = buildProblem {
      status(400)
      extension("field", "email")
    }

    assertThat("field" in result).isTrue()
  }

  @Test
  fun givenMissingExtension_whenCheckingContains_thenFalseIsReturned() {
    val result = buildProblem {
      status(400)
      extension("field", "email")
    }

    assertThat("reason" in result).isFalse()
  }

  @Test
  fun givenExtension_whenDestructuring_thenComponentsMatchNameAndValue() {
    val extension = Problem.extension("field", "email")

    val (name, value) = extension

    assertThat(name).isEqualTo("field")
    assertThat(value).isEqualTo("email")
  }

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

  @Test
  fun givenProblem_whenCopyingWithChanges_thenChangesAreAppliedOverOriginalFields() {
    val original = buildProblem {
      type("https://example.org/invalid")
      title("Invalid Input")
      status(400)
      detail("bad input")
      extension("field", "email")
    }

    val result = original.copy {
      detail("still bad input")
      extension("reason", "blank")
    }

    assertThat(result.type).isEqualTo(URI.create("https://example.org/invalid"))
    assertThat(result.title).isEqualTo("Invalid Input")
    assertThat(result.status).isEqualTo(400)
    assertThat(result.detail).isEqualTo("still bad input")
    assertThat(result.extensions).containsEntry("field", "email").containsEntry("reason", "blank")
  }

  @Test
  fun givenProblem_whenCopying_thenOriginalIsUnchanged() {
    val original = buildProblem {
      status(400)
      extension("field", "email")
    }

    original.copy {
      status(409)
      extension("field", null)
    }

    assertThat(original.status).isEqualTo(400)
    assertThat(original.extensions).containsEntry("field", "email")
  }

  @Test
  fun givenProblem_whenCopyingWithEmptyBlock_thenCopyEqualsOriginal() {
    val original = buildProblem {
      status(400)
      detail("bad input")
    }

    val result = original.copy {}

    assertThat(result).isEqualTo(original).isNotSameAs(original)
  }
}
