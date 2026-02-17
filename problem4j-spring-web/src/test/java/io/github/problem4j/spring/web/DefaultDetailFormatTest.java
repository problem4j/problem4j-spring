/*
 * Copyright (c) 2025-2026 The Problem4J Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
