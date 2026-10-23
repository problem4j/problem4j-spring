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
import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ProblemDslTest {

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
}
