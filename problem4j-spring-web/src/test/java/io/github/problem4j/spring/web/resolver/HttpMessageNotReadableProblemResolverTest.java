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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

class HttpMessageNotReadableProblemResolverTest {

  private HttpMessageNotReadableProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new HttpMessageNotReadableProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsHttpMessageNotReadableException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(HttpMessageNotReadableException.class);
  }

  @Test
  void givenExceptionWithoutJacksonCause_whenResolve_thenReturnsBadRequestProblem() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException("msg", new MockHttpInputMessage(new byte[0]));

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void givenExceptionWithoutJacksonCause_whenResolve_thenIgnoresPassedStatus() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException("msg", new MockHttpInputMessage(new byte[0]));

    Problem problem =
        resolver.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }
}
