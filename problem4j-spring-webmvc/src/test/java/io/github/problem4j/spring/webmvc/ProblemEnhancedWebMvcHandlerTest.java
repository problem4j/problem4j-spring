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

package io.github.problem4j.spring.webmvc;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;

class ProblemEnhancedWebMvcHandlerTest {

  private ProblemEnhancedWebMvcHandler advice;

  private AtomicInteger hits;

  @BeforeEach
  void beforeEach() {
    hits = new AtomicInteger(0);
    advice =
        new ProblemEnhancedWebMvcHandler(
            new DefaultProblemResolverStore(List.of()),
            ProblemPostProcessor.identity(),
            List.of((context, problem, ex, headers, status, exchange) -> hits.incrementAndGet()));
  }

  @Test
  void whileHandlingException_shouldHitInspector() throws Exception {
    advice.handleException(new ResponseStatusException(HttpStatus.BAD_REQUEST), request());

    assertThat(hits.get()).isEqualTo(1);
  }

  @Test
  void givenNoResolver_whenHandleExceptionInternal_thenReturnsFallbackProblemWithGivenStatus() {
    ResponseEntity<Object> response =
        requireNonNull(
            advice.handleExceptionInternal(
                new RuntimeException(),
                null,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(requireNonNull((Problem) response.getBody()).getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void givenResolver_whenHandleExceptionInternal_thenUsesResolverProblem() {
    ProblemResolver resolver =
        new ProblemResolver() {
          @Override
          public Class<? extends Exception> getExceptionClass() {
            return IllegalArgumentException.class;
          }

          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            return Problem.builder().status(422).detail("from-resolver").build();
          }
        };
    advice =
        new ProblemEnhancedWebMvcHandler(
            new DefaultProblemResolverStore(List.of(resolver)),
            ProblemPostProcessor.identity(),
            List.of());

    ResponseEntity<Object> response =
        requireNonNull(
            advice.handleExceptionInternal(
                new IllegalArgumentException(),
                null,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(requireNonNull((Problem) response.getBody()).getDetail()).isEqualTo("from-resolver");
  }

  @Test
  void givenAnyException_whenHandleExceptionInternal_thenSetsApplicationProblemJsonContentType() {
    ResponseEntity<Object> response =
        requireNonNull(
            advice.handleExceptionInternal(
                new RuntimeException(),
                null,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request()));

    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenPostProcessor_whenHandleExceptionInternal_thenPostProcessorIsApplied() {
    advice =
        new ProblemEnhancedWebMvcHandler(
            new DefaultProblemResolverStore(List.of()),
            (ctx, problem) -> problem.toBuilder().detail("processed").build(),
            List.of());

    ResponseEntity<Object> response =
        requireNonNull(
            advice.handleExceptionInternal(
                new RuntimeException(),
                null,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request()));

    assertThat(requireNonNull((Problem) response.getBody()).getDetail()).isEqualTo("processed");
  }

  @Test
  void givenPostProcessorThrows_whenHandleExceptionInternal_thenFallsBackToInternalServerError() {
    advice =
        new ProblemEnhancedWebMvcHandler(
            new DefaultProblemResolverStore(List.of()),
            (ctx, problem) -> {
              throw new RuntimeException("post-processor failure");
            },
            List.of());

    ResponseEntity<Object> response =
        requireNonNull(
            advice.handleExceptionInternal(
                new RuntimeException(),
                null,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static ServletWebRequest request() {
    return new ServletWebRequest(
        new MockHttpServletRequest("GET", "/test"), new MockHttpServletResponse());
  }
}
