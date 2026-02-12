/*
 * Copyright (c) 2025-2026 Damian Malczewski
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
package io.github.problem4j.spring.web.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.ProblemSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

class TypeMismatchProblemResolverTest {

  private TypeMismatchProblemResolver typeMismatchMapping;

  @BeforeEach
  void beforeEach() {
    typeMismatchMapping = new TypeMismatchProblemResolver();
  }

  @Test
  void givenExceptionWithParameterNameAndType_shouldReturnProblemWithAll() {
    TypeMismatchException ex = new TypeMismatchException("42", Integer.class);
    ex.initPropertyName("age");

    Problem problem =
        typeMismatchMapping.resolveProblem(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(ProblemSupport.TYPE_MISMATCH_DETAIL)
                .extension(ProblemSupport.PROPERTY_EXTENSION, "age")
                .extension(ProblemSupport.KIND_EXTENSION, "integer")
                .build());
  }

  @Test
  void givenExceptionWithParameterType_shouldReturnProblemWithTypeOnly() {
    TypeMismatchException ex = new TypeMismatchException("42", Integer.class);

    Problem problem =
        typeMismatchMapping.resolveProblem(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(ProblemSupport.TYPE_MISMATCH_DETAIL)
                .extension(ProblemSupport.KIND_EXTENSION, "integer")
                .build());
  }

  @Test
  void givenExceptionWithParameterName_shouldReturnProblemWithNameOnly() {
    TypeMismatchException ex = new TypeMismatchException("value", null);
    ex.initPropertyName("field");

    Problem problem =
        typeMismatchMapping.resolveProblem(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(ProblemSupport.TYPE_MISMATCH_DETAIL)
                .extension(ProblemSupport.PROPERTY_EXTENSION, "field")
                .build());
  }
}
