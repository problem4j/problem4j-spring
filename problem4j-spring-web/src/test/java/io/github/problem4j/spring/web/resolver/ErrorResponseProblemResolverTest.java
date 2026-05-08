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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

class ErrorResponseProblemResolverTest {

  private ErrorResponseProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new ErrorResponseProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsErrorResponseException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(ErrorResponseException.class);
  }

  @Test
  void givenErrorResponseException_whenResolve_thenCopiesStatusFromException() {
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.CONFLICT);

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.OK);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
  }

  @Test
  void givenErrorResponseExceptionWithDetail_whenResolve_thenCopiesDetailFromBody() {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "invalid input");
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.BAD_REQUEST, body, null);

    Problem problem =
        resolver.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("invalid input");
  }

  @Test
  void givenErrorResponseException_whenResolve_thenIgnoresPassedStatus() {
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.FORBIDDEN);

    Problem problem =
        resolver.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }
}
