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

class ProblemExtensionsTest {

  @Test
  fun givenExtension_whenDestructuring_thenComponentsMatchNameAndValue() {
    val extension = Problem.extension("field", "email")

    val (name, value) = extension

    assertThat(name).isEqualTo("field")
    assertThat(value).isEqualTo("email")
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
