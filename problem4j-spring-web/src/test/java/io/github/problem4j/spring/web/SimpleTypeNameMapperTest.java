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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
        Arguments.of(TestEnum.class, "string"),
        Arguments.of(String[].class, "array"),
        Arguments.of(Collection.class, "array"),
        Arguments.of(List.class, "array"),
        Arguments.of(ArrayList.class, "array"),
        Arguments.of(LinkedList.class, "array"),
        Arguments.of(Set.class, "array"),
        Arguments.of(HashSet.class, "array"),
        Arguments.of(TreeSet.class, "array"));
  }
}
