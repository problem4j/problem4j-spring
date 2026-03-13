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

package io.github.problem4j.spring.web.parameter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ViolationTest {

  @Test
  void givenSameInstance_whenEquals_thenTrue() {
    Violation violation = new Violation("field", "must not be null");

    assertThat(violation.equals(violation)).isTrue();
  }

  @Test
  void givenEqualFields_whenEquals_thenTrue() {
    Violation a = new Violation("field", "must not be null");
    Violation b = new Violation("field", "must not be null");

    assertThat(a).isEqualTo(b);
  }

  @Test
  void givenNull_whenEquals_thenFalse() {
    Violation violation = new Violation("field", "must not be null");

    assertThat(violation.equals(null)).isFalse();
  }

  @Test
  void givenDifferentType_whenEquals_thenFalse() {
    Violation violation = new Violation("field", "must not be null");

    assertThat(violation.equals("not a violation")).isFalse();
  }

  @Test
  void givenDifferentField_whenEquals_thenFalse() {
    Violation a = new Violation("field", "error");
    Violation b = new Violation("other", "error");

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void givenDifferentError_whenEquals_thenFalse() {
    Violation a = new Violation("field", "error");
    Violation b = new Violation("field", "different");

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void givenEqualFields_whenHashCode_thenConsistent() {
    Violation a = new Violation("field", "must not be null");
    Violation b = new Violation("field", "must not be null");

    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void givenNullFields_whenHashCode_thenDoesNotThrow() {
    Violation a = new Violation(null, null);
    Violation b = new Violation(null, null);

    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void givenViolation_whenToString_thenContainsFieldAndError() {
    Violation violation = new Violation("age", "must be positive");

    assertThat(violation.toString()).contains("age").contains("must be positive");
  }

  @Test
  void givenNullFields_whenToString_thenDoesNotThrow() {
    Violation violation = new Violation(null, null);

    assertThat(violation.toString()).isNotNull();
  }
}
