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

import io.github.problem4j.core.DefaultProblemMapper;
import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
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
import org.springframework.web.bind.annotation.ResponseStatus;

class ExceptionWebFluxAdviceTest {

  private ExceptionWebFluxAdvice advice;

  private AtomicInteger hits;

  @BeforeEach
  void beforeEach() {
    hits = new AtomicInteger(0);
    advice =
        new ExceptionWebFluxAdvice(
            new DefaultProblemMapper(),
            new DefaultProblemResolverStore(List.of()),
            ProblemPostProcessor.identity(),
            List.of((context, problem, ex, headers, status, exchange) -> hits.incrementAndGet()));
  }

  @Test
  void whileHandlingException_shouldHitInspector() {
    advice.handleException(new RuntimeException(), exchange());

    assertThat(hits.get()).isEqualTo(1);
  }

  @Test
  void givenGenericException_whenHandleException_thenReturnsInternalServerError() {
    ResponseEntity<Problem> response =
        requireNonNull(advice.handleException(new RuntimeException(), exchange()).block());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(requireNonNull(response.getBody()).getStatus())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }

  @Test
  void givenExceptionWithResponseStatus_whenHandleException_thenReturnsStatusFromAnnotation() {
    ResponseEntity<Problem> response =
        requireNonNull(advice.handleException(new ForbiddenExceptionStub(), exchange()).block());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(requireNonNull(response.getBody()).getStatus())
        .isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void givenExceptionWithResponseStatusAndReason_whenHandleException_thenUsesReasonAsDetail() {
    ResponseEntity<Problem> response =
        requireNonNull(advice.handleException(new NotFoundWithReasonStub(), exchange()).block());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(requireNonNull(response.getBody()).getDetail()).isEqualTo("Resource not found");
  }

  @Test
  void givenAnyException_whenHandleException_thenSetsApplicationProblemJsonContentType() {
    ResponseEntity<Problem> response =
        requireNonNull(advice.handleException(new RuntimeException(), exchange()).block());

    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenPostProcessor_whenHandleException_thenPostProcessorIsApplied() {
    advice =
        new ExceptionWebFluxAdvice(
            new DefaultProblemMapper(),
            new DefaultProblemResolverStore(List.of()),
            (ctx, problem) -> problem.toBuilder().detail("processed").build(),
            List.of());

    ResponseEntity<Problem> response =
        requireNonNull(advice.handleException(new RuntimeException(), exchange()).block());

    assertThat(requireNonNull(response.getBody()).getDetail()).isEqualTo("processed");
  }

  @Test
  void givenPostProcessorThrows_whenHandleException_thenFallsBackToInternalServerError() {
    advice =
        new ExceptionWebFluxAdvice(
            new DefaultProblemMapper(),
            new DefaultProblemResolverStore(List.of()),
            (ctx, problem) -> {
              throw new RuntimeException("post-processor failure");
            },
            List.of());

    ResponseEntity<Problem> response =
        requireNonNull(advice.handleException(new RuntimeException(), exchange()).block());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static MockServerWebExchange exchange() {
    return MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
  }

  @ResponseStatus(code = HttpStatus.FORBIDDEN)
  static class ForbiddenExceptionStub extends RuntimeException {}

  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Resource not found")
  static class NotFoundWithReasonStub extends RuntimeException {}
}
