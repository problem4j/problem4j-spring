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
