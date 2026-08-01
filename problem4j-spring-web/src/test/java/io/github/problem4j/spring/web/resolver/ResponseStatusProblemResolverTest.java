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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ResponseStatusProblemResolverTest {

  private ResponseStatusProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new ResponseStatusProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsResponseStatusException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(ResponseStatusException.class);
  }

  @Test
  void givenResponseStatusException_whenResolve_thenCopiesStatusFromException() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND);

    Problem problem =
        resolver.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void givenResponseStatusExceptionWithCustomStatus_whenResolve_thenUsesExceptionStatus() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.OK);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.PAYMENT_REQUIRED.value());
  }
}
