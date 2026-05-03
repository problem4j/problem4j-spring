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

package io.github.problem4j.spring.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ResponseSupportTest {

  @Test
  void givenProblemWithValidStatus_whenResolveStatus_thenReturnsMatchingHttpStatus() {
    Problem problem = Problem.builder().status(400).build();

    HttpStatus status = ResponseSupport.resolveStatus(problem);

    assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void givenProblemWithServerErrorStatus_whenResolveStatus_thenReturnsMatchingHttpStatus() {
    Problem problem = Problem.builder().status(503).build();

    HttpStatus status = ResponseSupport.resolveStatus(problem);

    assertThat(status).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
  }

  @Test
  void givenProblemWithUnknownStatus_whenResolveStatus_thenFallsBackToInternalServerError() {
    Problem problem = Problem.builder().status(999).build();

    HttpStatus status = ResponseSupport.resolveStatus(problem);

    assertThat(status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void givenProblemWithZeroStatus_whenResolveStatus_thenFallsBackToInternalServerError() {
    Problem problem = Problem.builder().status(0).build();

    HttpStatus status = ResponseSupport.resolveStatus(problem);

    assertThat(status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
