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

package io.github.problem4j.spring.web.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

class TypeMismatchProblemResolverTest {

  private TypeMismatchProblemResolver typeMismatchMapping;

  @BeforeEach
  void beforeEach() {
    typeMismatchMapping = new TypeMismatchProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsTypeMismatchException() {
    assertThat(typeMismatchMapping.getExceptionClass()).isEqualTo(TypeMismatchException.class);
  }

  @Test
  void givenExceptionWithParameterNameAndType_shouldReturnProblemWithAll() {
    TypeMismatchException ex = new TypeMismatchException("42", Integer.class);
    ex.initPropertyName("age");

    Problem problem =
        typeMismatchMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Type mismatch")
                .extension("property", "age")
                .extension("kind", "integer")
                .build());
  }

  @Test
  void givenExceptionWithParameterType_shouldReturnProblemWithTypeOnly() {
    TypeMismatchException ex = new TypeMismatchException("42", Integer.class);

    Problem problem =
        typeMismatchMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Type mismatch")
                .extension("kind", "integer")
                .build());
  }

  @Test
  void givenExceptionWithParameterName_shouldReturnProblemWithNameOnly() {
    TypeMismatchException ex = new TypeMismatchException("value", null);
    ex.initPropertyName("field");

    Problem problem =
        typeMismatchMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Type mismatch")
                .extension("property", "field")
                .build());
  }
}
