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

package io.github.problem4j.spring.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TypeNameEntryTest {

  @Test
  void givenTypeAndName_whenCreating_thenGettersReturnThem() {
    TypeNameEntry entry = new TypeNameEntry(UUID.class, "uuid");

    assertThat(entry.getType()).isEqualTo(UUID.class);
    assertThat(entry.getName()).isEqualTo("uuid");
  }

  @Test
  void givenEntriesWithSameTypeAndName_whenComparing_thenTheyAreEqual() {
    TypeNameEntry entry = new TypeNameEntry(UUID.class, "uuid");
    TypeNameEntry other = new TypeNameEntry(UUID.class, "uuid");

    assertThat(entry).isEqualTo(other).hasSameHashCodeAs(other);
  }

  @Test
  void givenEntriesWithDifferentTypeOrName_whenComparing_thenTheyAreNotEqual() {
    TypeNameEntry entry = new TypeNameEntry(UUID.class, "uuid");

    assertThat(entry)
        .isNotEqualTo(new TypeNameEntry(String.class, "uuid"))
        .isNotEqualTo(new TypeNameEntry(UUID.class, "identifier"))
        .isNotEqualTo("uuid")
        .isNotEqualTo(null);
  }

  @Test
  void givenEntry_whenConvertingToString_thenTypeNameAndNameAreIncluded() {
    TypeNameEntry entry = new TypeNameEntry(UUID.class, "uuid");

    assertThat(entry).hasToString("TypeNameEntry[type=java.util.UUID, name=uuid]");
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullType_whenCreating_thenThrowsNullPointerException() {
    assertThatThrownBy(() -> new TypeNameEntry(null, "uuid"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("type must not be null");
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullName_whenCreating_thenThrowsNullPointerException() {
    assertThatThrownBy(() -> new TypeNameEntry(UUID.class, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name must not be null");
  }
}
