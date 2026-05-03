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

package io.github.problem4j.spring.webflux;

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
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class ProblemExceptionWebFluxAdviceTest {

  private ProblemExceptionWebFluxAdvice advice;

  private AtomicInteger hits;

  @BeforeEach
  void beforeEach() {
    hits = new AtomicInteger(0);
    advice =
        new ProblemExceptionWebFluxAdvice(
            ProblemPostProcessor.identity(),
            List.of((context, problem, ex, headers, status, exchange) -> hits.incrementAndGet()));
  }

  @Test
  void whileHandlingException_shouldHitInspector() {
    advice.handleProblemException(
        new ProblemException(Problem.of(HttpStatus.BAD_REQUEST.value())), exchange());

    assertThat(hits.get()).isEqualTo(1);
  }

  @Test
  void givenProblemException_whenHandleProblemException_thenReturnsProblemsStatus() {
    ResponseEntity<Problem> response =
        requireNonNull(
            advice
                .handleProblemException(
                    new ProblemException(Problem.of(HttpStatus.UNPROCESSABLE_ENTITY.value())),
                    exchange())
                .block());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(requireNonNull(response.getBody()).getStatus())
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
  }

  @Test
  void
      givenAnyProblemException_whenHandleProblemException_thenSetsApplicationProblemJsonContentType() {
    ResponseEntity<Problem> response =
        requireNonNull(
            advice
                .handleProblemException(
                    new ProblemException(Problem.of(HttpStatus.BAD_REQUEST.value())), exchange())
                .block());

    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenPostProcessor_whenHandleProblemException_thenPostProcessorIsApplied() {
    advice =
        new ProblemExceptionWebFluxAdvice(
            (ctx, problem) -> problem.toBuilder().detail("processed").build(), List.of());

    ResponseEntity<Problem> response =
        requireNonNull(
            advice
                .handleProblemException(
                    new ProblemException(Problem.of(HttpStatus.BAD_REQUEST.value())), exchange())
                .block());

    assertThat(requireNonNull(response.getBody()).getDetail()).isEqualTo("processed");
  }

  @Test
  void givenPostProcessorThrows_whenHandleProblemException_thenFallsBackToInternalServerError() {
    advice =
        new ProblemExceptionWebFluxAdvice(
            (ctx, problem) -> {
              throw new RuntimeException("post-processor failure");
            },
            List.of());

    ResponseEntity<Problem> response =
        requireNonNull(
            advice
                .handleProblemException(
                    new ProblemException(Problem.of(HttpStatus.BAD_REQUEST.value())), exchange())
                .block());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static MockServerWebExchange exchange() {
    return MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
  }
}
