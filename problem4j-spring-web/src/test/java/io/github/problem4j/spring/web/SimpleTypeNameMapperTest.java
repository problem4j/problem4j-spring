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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SimpleTypeNameMapperTest {

  private final TypeNameMapper mapper = new SimpleTypeNameMapper();

  private enum TestEnum {
    A,
    B
  }

  @ParameterizedTest
  @MethodSource("supportedTypeMappings")
  void givenSupportedType_whenMap_thenReturnExpectedName(Class<?> type, String expected) {
    assertThat(mapper.map(type)).contains(expected);
  }

  @Test
  void givenUnsupportedType_whenMap_thenReturnEmpty() {
    assertThat(mapper.map(Thread.class)).isEmpty();
  }

  @Test
  void givenNull_whenMap_thenReturnEmpty() {
    assertThat(mapper.map(null)).isEmpty();
  }

  static Stream<Arguments> supportedTypeMappings() {
    return Stream.of(
        Arguments.of(int.class, "integer"),
        Arguments.of(Integer.class, "integer"),
        Arguments.of(long.class, "integer"),
        Arguments.of(Long.class, "integer"),
        Arguments.of(BigInteger.class, "integer"),
        Arguments.of(AtomicLong.class, "integer"),
        Arguments.of(AtomicInteger.class, "integer"),
        Arguments.of(short.class, "integer"),
        Arguments.of(Short.class, "integer"),
        Arguments.of(byte.class, "integer"),
        Arguments.of(Byte.class, "integer"),
        Arguments.of(double.class, "number"),
        Arguments.of(Double.class, "number"),
        Arguments.of(BigDecimal.class, "number"),
        Arguments.of(float.class, "number"),
        Arguments.of(Float.class, "number"),
        Arguments.of(boolean.class, "boolean"),
        Arguments.of(Boolean.class, "boolean"),
        Arguments.of(String.class, "string"),
        Arguments.of(TestEnum.class, "enum"),
        Arguments.of(String[].class, "array"),
        Arguments.of(Collection.class, "array"),
        Arguments.of(List.class, "array"),
        Arguments.of(ArrayList.class, "array"),
        Arguments.of(LinkedList.class, "array"),
        Arguments.of(Set.class, "array"),
        Arguments.of(HashSet.class, "array"),
        Arguments.of(TreeSet.class, "array"));
  }

  @Test
  void givenCustomMapping_whenMappingRegisteredType_thenCustomNameIsReturned() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(UUID.class, "uuid"));

    assertThat(mapper.map(UUID.class)).hasValue("uuid");
  }

  @Test
  void givenCustomMapping_whenMappingOtherType_thenDefaultNameIsReturned() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(UUID.class, "uuid"));

    assertThat(mapper.map(Integer.class)).hasValue("integer");
    assertThat(mapper.map(Object.class)).isEmpty();
  }

  @Test
  void givenCustomMappingForDefaultType_whenMapping_thenCustomMappingTakesPrecedence() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(String.class, "text"));

    assertThat(mapper.map(String.class)).hasValue("text");
  }

  @Test
  void givenCustomMappingForInterface_whenMappingImplementation_thenCustomNameIsReturned() {
    TypeNameMapper mapper =
        new SimpleTypeNameMapper(new TypeNameEntry(Temporal.class, "date-time"));

    assertThat(mapper.map(LocalDate.class)).hasValue("date-time");
  }

  @Test
  void givenCustomMappingForSuperclass_whenMappingSubclass_thenCustomMappingTakesPrecedence() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(Number.class, "numeric"));

    assertThat(mapper.map(BigDecimal.class)).hasValue("numeric");
  }

  @Test
  void givenSpecificTypeBeforeSupertype_whenMapping_thenEachTypeMatchesItsOwnMapping() {
    TypeNameMapper mapper =
        new SimpleTypeNameMapper(
            new TypeNameEntry(LocalDate.class, "date"),
            new TypeNameEntry(Temporal.class, "date-time"));

    assertThat(mapper.map(LocalDate.class)).hasValue("date");
    assertThat(mapper.map(LocalDateTime.class)).hasValue("date-time");
  }

  @Test
  void givenSupertypeBeforeSpecificType_whenMapping_thenSupertypeMappingWins() {
    TypeNameMapper mapper =
        new SimpleTypeNameMapper(
            new TypeNameEntry(Temporal.class, "date-time"),
            new TypeNameEntry(LocalDate.class, "date"));

    assertThat(mapper.map(LocalDate.class)).hasValue("date-time");
  }

  @Test
  void givenMultipleMatchingInterfaces_whenMapping_thenFirstGivenMappingWins() {
    TypeNameMapper mapper =
        new SimpleTypeNameMapper(
            new TypeNameEntry(SecondInterface.class, "second"),
            new TypeNameEntry(FirstInterface.class, "first"));

    assertThat(mapper.map(BothInterfaces.class)).hasValue("second");
  }

  @Test
  void givenSameTypeGivenTwice_whenMapping_thenLastNameWinsAtFirstPosition() {
    TypeNameMapper mapper =
        new SimpleTypeNameMapper(
            new TypeNameEntry(UUID.class, "uuid"),
            new TypeNameEntry(Object.class, "object"),
            new TypeNameEntry(UUID.class, "identifier"));

    assertThat(mapper.map(UUID.class)).hasValue("identifier");
  }

  @Test
  void givenCustomMappingForWrapper_whenMappingPrimitive_thenCustomNameIsReturned() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(Integer.class, "int32"));

    assertThat(mapper.map(int.class)).hasValue("int32");
    assertThat(mapper.map(Integer.class)).hasValue("int32");
  }

  @Test
  void givenCustomMappingForPrimitive_whenMappingWrapper_thenDefaultNameIsReturned() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(int.class, "int32"));

    assertThat(mapper.map(int.class)).hasValue("int32");
    assertThat(mapper.map(Integer.class)).hasValue("integer");
  }

  @Test
  void givenCustomMappingForWrapperSupertype_whenMappingPrimitive_thenCustomNameIsReturned() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(Number.class, "numeric"));

    assertThat(mapper.map(long.class)).hasValue("numeric");
  }

  @Test
  void givenNoCustomMappings_whenCreatingWithEmptyVarargs_thenDefaultsAreUsed() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry[0]);

    assertThat(mapper.map(Integer.class)).hasValue("integer");
  }

  @Test
  void givenCustomMappings_whenMappingNullType_thenEmptyIsReturned() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(Object.class, "object"));

    assertThat(mapper.map(null)).isEmpty();
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullEntriesArray_whenCreating_thenThrowsNullPointerException() {
    assertThatThrownBy(() -> new SimpleTypeNameMapper((TypeNameEntry[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("entries must not be null");
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullEntry_whenCreating_thenThrowsNullPointerException() {
    assertThatThrownBy(() -> new SimpleTypeNameMapper(new TypeNameEntry(UUID.class, "uuid"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("entries must not contain null elements");
  }

  @Test
  void givenListOfEntries_whenMapping_thenEntriesAreComparedInListOrder() {
    TypeNameMapper mapper =
        new SimpleTypeNameMapper(
            List.of(
                new TypeNameEntry(LocalDate.class, "date"),
                new TypeNameEntry(Temporal.class, "date-time")));

    assertThat(mapper.map(LocalDate.class)).hasValue("date");
    assertThat(mapper.map(LocalDateTime.class)).hasValue("date-time");
    assertThat(mapper.map(Integer.class)).hasValue("integer");
  }

  @Test
  void givenEmptyList_whenMapping_thenDefaultsAreUsed() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(List.of());

    assertThat(mapper.map(Integer.class)).hasValue("integer");
    assertThat(mapper.map(UUID.class)).isEmpty();
  }

  @Test
  void givenSourceListModifiedAfterCreation_whenMapping_thenOriginalEntriesAreUsed() {
    List<TypeNameEntry> entries = new ArrayList<>();
    entries.add(new TypeNameEntry(UUID.class, "uuid"));
    TypeNameMapper mapper = new SimpleTypeNameMapper(entries);

    entries.set(0, new TypeNameEntry(UUID.class, "changed"));
    entries.add(new TypeNameEntry(String.class, "text"));

    assertThat(mapper.map(UUID.class)).hasValue("uuid");
    assertThat(mapper.map(String.class)).hasValue("string");
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullList_whenCreating_thenThrowsNullPointerException() {
    assertThatThrownBy(() -> new SimpleTypeNameMapper((List<TypeNameEntry>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("entries must not be null");
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenListWithNullElement_whenCreating_thenThrowsNullPointerException() {
    List<TypeNameEntry> entries = new ArrayList<>();
    entries.add(null);

    assertThatThrownBy(() -> new SimpleTypeNameMapper(entries))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("entries must not contain null elements");
  }

  @Test
  void givenSingleEntry_whenMapping_thenOnlyThatTypeIsCustomized() {
    TypeNameMapper mapper = new SimpleTypeNameMapper(new TypeNameEntry(UUID.class, "uuid"));

    assertThat(mapper.map(UUID.class)).hasValue("uuid");
    assertThat(mapper.map(String.class)).hasValue("string");
  }

  private interface FirstInterface {}

  private interface SecondInterface {}

  private static final class BothInterfaces implements FirstInterface, SecondInterface {}
}
