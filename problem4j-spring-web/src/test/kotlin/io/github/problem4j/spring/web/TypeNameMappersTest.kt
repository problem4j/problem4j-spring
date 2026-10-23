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

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.Temporal
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TypeNameMappersTest {

  @Test
  fun givenPairs_whenMappingRegisteredTypes_thenCustomNamesAreReturned() {
    val mapper = simpleTypeNameMapper(UUID::class to "uuid", LocalDate::class to "date")

    assertThat(mapper.map(UUID::class.java)).hasValue("uuid")
    assertThat(mapper.map(LocalDate::class.java)).hasValue("date")
  }

  @Test
  fun givenPairs_whenMappingOtherType_thenDefaultNameIsReturned() {
    val mapper = simpleTypeNameMapper(UUID::class to "uuid")

    assertThat(mapper.map(String::class.java)).hasValue("string")
  }

  @Test
  fun givenPairForSupertype_whenMappingSubtype_thenCustomNameIsReturned() {
    val mapper = simpleTypeNameMapper(Temporal::class to "date-time")

    assertThat(mapper.map(LocalDate::class.java)).hasValue("date-time")
  }

  @Test
  fun givenSpecificTypeBeforeSupertype_whenMapping_thenEachTypeMatchesItsOwnPair() {
    val mapper = simpleTypeNameMapper(LocalDate::class to "date", Temporal::class to "date-time")

    assertThat(mapper.map(LocalDate::class.java)).hasValue("date")
    assertThat(mapper.map(LocalDateTime::class.java)).hasValue("date-time")
  }

  @Test
  fun givenSupertypeBeforeSpecificType_whenMapping_thenSupertypePairWins() {
    val mapper = simpleTypeNameMapper(Temporal::class to "date-time", LocalDate::class to "date")

    assertThat(mapper.map(LocalDate::class.java)).hasValue("date-time")
  }

  @Test
  fun givenPairForTypeWithPrimitiveCounterpart_whenMapping_thenBothPrimitiveAndBoxedAreMatched() {
    val mapper = simpleTypeNameMapper(Int::class to "int32")

    assertThat(mapper.map(Int::class.javaPrimitiveType!!)).hasValue("int32")
    assertThat(mapper.map(Int::class.javaObjectType)).hasValue("int32")
  }

  @Test
  fun givenSameTypeInMultiplePairs_whenMapping_thenLastPairWins() {
    val mapper = simpleTypeNameMapper(UUID::class to "uuid", UUID::class to "identifier")

    assertThat(mapper.map(UUID::class.java)).hasValue("identifier")
  }

  @Test
  fun givenNoPairs_whenMapping_thenDefaultsAreUsed() {
    val mapper = simpleTypeNameMapper()

    assertThat(mapper.map(Int::class.javaObjectType)).hasValue("integer")
    assertThat(mapper.map(UUID::class.java)).isEmpty()
  }
}
