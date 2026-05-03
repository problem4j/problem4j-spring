/*
 * Copyright 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.problem4j.spring.web.autoconfigure.ProblemProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DefaultDetailFormatTest {

  @Test
  void givenLowercaseFormat_whenFormatting_thenReturnsLowercase() {
    DefaultProblemFormat format =
        new DefaultProblemFormat(ProblemProperties.DetailFormat.LOWERCASE);

    String result = format.formatDetail("TeSt StrIng");

    assertThat(result).isEqualTo("test string");
  }

  @Test
  void givenUppercaseFormat_whenFormatting_thenReturnsUppercase() {
    DefaultProblemFormat formatting =
        new DefaultProblemFormat(ProblemProperties.DetailFormat.UPPERCASE);

    String result = formatting.formatDetail("TeSt StrIng");

    assertThat(result).isEqualTo("TEST STRING");
  }

  @Test
  void givenCapitalizedFormat_whenFormatting_thenReturnsUppercase() {
    DefaultProblemFormat formatting =
        new DefaultProblemFormat(ProblemProperties.DetailFormat.CAPITALIZED);

    String result = formatting.formatDetail("test string");

    assertThat(result).isEqualTo("Test string");
  }

  @Test
  void givenUnknownFormat_whenFormatting_thenReturnsUnchanged() {
    DefaultProblemFormat formatting = new DefaultProblemFormat("something-else");

    String result = formatting.formatDetail("TeSt StrIng");

    assertThat(result).isEqualTo("TeSt StrIng");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        ProblemProperties.DetailFormat.LOWERCASE,
        ProblemProperties.DetailFormat.UPPERCASE,
        ProblemProperties.DetailFormat.CAPITALIZED,
        ""
      })
  @NullSource
  void givenEmptyString_whenFormatting_thenReturnsEmpty(String detailFormat) {
    DefaultProblemFormat formatting = new DefaultProblemFormat(detailFormat);

    String result = formatting.formatDetail("");

    assertThat(result).isEqualTo("");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        ProblemProperties.DetailFormat.LOWERCASE,
        ProblemProperties.DetailFormat.UPPERCASE,
        ProblemProperties.DetailFormat.CAPITALIZED,
        ""
      })
  @NullSource
  void givenNullInput_whenFormatting_thenReturnsNull(String detailFormat) {
    DefaultProblemFormat formatting = new DefaultProblemFormat(detailFormat);

    assertNull(formatting.formatDetail(null));
  }
}
