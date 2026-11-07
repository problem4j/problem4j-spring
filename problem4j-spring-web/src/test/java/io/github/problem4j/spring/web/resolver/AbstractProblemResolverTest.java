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

package io.github.problem4j.spring.web.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

class AbstractProblemResolverTest {

  public static Stream<Arguments> exceptions() {
    return Stream.of(Arguments.of(IllegalArgumentException.class, IllegalStateException.class));
  }

  @ParameterizedTest
  @MethodSource("exceptions")
  void givenAnyMapping_shouldReturnExceptionClass(Class<? extends Exception> clazz) {
    AbstractProblemResolver resolver =
        new AbstractProblemResolver(clazz) {
          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            return Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
          }
        };

    Class<? extends Exception> exceptionClass = resolver.getExceptionClass();

    assertThat(exceptionClass).isEqualTo(clazz);
  }

  @Test
  void givenSetProblemFormat_whenResolve_thenUsesNewFormat() {
    AbstractProblemResolver resolver =
        new AbstractProblemResolver(RuntimeException.class) {
          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            return Problem.builder().status(500).detail(formatDetail("detail")).build();
          }
        };

    resolver.setProblemFormat(detail -> detail == null ? null : detail.toUpperCase());

    Problem problem =
        resolver.resolve(
            ProblemContext.create(),
            new RuntimeException("boom"),
            new HttpHeaders(),
            HttpStatusCode.valueOf(500));

    assertThat(problem.getDetail()).isEqualTo("DETAIL");
  }

  @Test
  void givenNoResolveProblemOverride_whenResolve_thenReturnsInternalServerError() {
    AbstractProblemResolver resolver =
        new AbstractProblemResolver(RuntimeException.class) {
          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            return Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
          }
        };

    Problem problem =
        resolver.resolve(
            ProblemContext.create(),
            new RuntimeException("boom"),
            new HttpHeaders(),
            HttpStatusCode.valueOf(500));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }
}
