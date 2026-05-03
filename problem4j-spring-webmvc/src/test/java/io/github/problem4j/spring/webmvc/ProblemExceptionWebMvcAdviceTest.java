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

package io.github.problem4j.spring.webmvc;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;

class ProblemExceptionWebMvcAdviceTest {

  private ProblemExceptionWebMvcAdvice advice;

  private AtomicInteger hits;

  @BeforeEach
  void beforeEach() {
    hits = new AtomicInteger(0);
    advice =
        new ProblemExceptionWebMvcAdvice(
            ProblemPostProcessor.identity(),
            List.of((context, problem, ex, headers, status, exchange) -> hits.incrementAndGet()));
  }

  @Test
  void whileHandlingException_shouldHitInspector() {
    advice.handleProblemException(
        new ProblemException(Problem.of(HttpStatus.BAD_REQUEST.value())), request());

    assertThat(hits.get()).isEqualTo(1);
  }

  @Test
  void givenProblemException_whenHandleProblemException_thenReturnsProblemsStatus() {
    ResponseEntity<Problem> response =
        advice.handleProblemException(
            new ProblemException(Problem.of(HttpStatus.UNPROCESSABLE_ENTITY.value())), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(requireNonNull(response.getBody()).getStatus())
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
  }

  @Test
  void
      givenAnyProblemException_whenHandleProblemException_thenSetsApplicationProblemJsonContentType() {
    ResponseEntity<Problem> response =
        advice.handleProblemException(
            new ProblemException(Problem.of(HttpStatus.BAD_REQUEST.value())), request());

    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenPostProcessor_whenHandleProblemException_thenPostProcessorIsApplied() {
    advice =
        new ProblemExceptionWebMvcAdvice(
            (ctx, problem) -> problem.toBuilder().detail("processed").build(), List.of());

    ResponseEntity<Problem> response =
        advice.handleProblemException(
            new ProblemException(Problem.of(HttpStatus.BAD_REQUEST.value())), request());

    assertThat(requireNonNull(response.getBody()).getDetail()).isEqualTo("processed");
  }

  @Test
  void givenPostProcessorThrows_whenHandleProblemException_thenFallsBackToInternalServerError() {
    advice =
        new ProblemExceptionWebMvcAdvice(
            (ctx, problem) -> {
              throw new RuntimeException("post-processor failure");
            },
            List.of());

    ResponseEntity<Problem> response =
        advice.handleProblemException(
            new ProblemException(Problem.of(HttpStatus.BAD_REQUEST.value())), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static ServletWebRequest request() {
    return new ServletWebRequest(
        new MockHttpServletRequest("GET", "/test"), new MockHttpServletResponse());
  }
}
