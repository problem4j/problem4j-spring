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

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.web.operations.ProblemTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.ErrorResponseException;
import reactor.test.StepVerifier;

class ProblemWebExceptionHandlerTest {

  private List<HttpMessageWriter<?>> writers;
  private MockServerWebExchange exchange;
  private List<ProblemContext> inspectedContexts;

  @BeforeEach
  void beforeEach() {
    writers = ServerCodecConfigurer.create().getWriters();
    exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));
    inspectedContexts = new ArrayList<>();
  }

  private ProblemWebExceptionHandler handler(
      ProblemOperations operations, List<HttpMessageWriter<?>> writers) {
    return new ProblemWebExceptionHandler(
        operations,
        writers,
        List.of(
            (context, problem, ex, headers, status, exchange) -> inspectedContexts.add(context)));
  }

  private ProblemWebExceptionHandler handler() {
    return handler(ProblemTemplate.builder().build(), writers);
  }

  @Test
  void givenDefaults_whenChangingOrderAndUnknownExceptions_thenValuesAreChanged() {
    ProblemWebExceptionHandler handler = handler();

    assertThat(handler.getOrder()).isEqualTo(-3);
    assertThat(handler.isUnknownExceptions()).isFalse();
    handler.setOrder(7);
    handler.setUnknownExceptions(true);
    assertThat(handler.getOrder()).isEqualTo(7);
    assertThat(handler.isUnknownExceptions()).isTrue();
  }

  @Test
  void givenProblemException_whenHandling_thenProblemIsWrittenAndInspected() {
    ProblemContext context = ProblemContext.create().put("traceId", "abc");
    exchange.getAttributes().put(PROBLEM_CONTEXT_ATTRIBUTE, context);

    StepVerifier.create(
            handler().handle(exchange, new ProblemException(Problem.of(409, "conflict"))))
        .verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(exchange.getResponse().getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(exchange.getResponse().getBodyAsString().block())
        .contains("\"status\":409")
        .contains("conflict");
    assertThat(inspectedContexts).containsExactly(context);
  }

  @Test
  void givenErrorResponseWithHeaders_whenHandling_thenHeadersAreWritten() {
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.METHOD_NOT_ALLOWED);
    ex.getHeaders().add("Allow", "GET");

    StepVerifier.create(handler().handle(exchange, ex)).verifyComplete();

    assertThat(exchange.getResponse().getHeaders().get("Allow")).containsExactly("GET");
    assertThat(inspectedContexts).hasSize(1);
  }

  @Test
  void givenProblemWithUnknownStatus_whenHandling_thenInternalServerErrorStatusIsUsed() {
    StepVerifier.create(handler().handle(exchange, new ProblemException(Problem.of(599))))
        .verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void givenCommittedResponse_whenHandling_thenErrorIsPropagated() {
    exchange.getResponse().setComplete().block();
    ProblemException ex = new ProblemException(Problem.of(409));

    StepVerifier.create(handler().handle(exchange, ex)).verifyErrorMatches(e -> e == ex);
  }

  @Test
  void givenError_whenHandling_thenErrorIsPropagated() {
    AssertionError error = new AssertionError("not an exception");

    StepVerifier.create(handler().handle(exchange, error)).verifyErrorMatches(e -> e == error);
  }

  @Test
  void givenDisconnectedClient_whenHandling_thenErrorIsPropagated() {
    IOException ex = new IOException("Connection reset by peer");

    StepVerifier.create(handler().handle(exchange, ex)).verifyErrorMatches(e -> e == ex);
  }

  @Test
  void givenUnknownExceptionAndUnknownExceptionsDisabled_whenHandling_thenErrorIsPropagated() {
    IllegalStateException ex = new IllegalStateException("unknown");

    StepVerifier.create(handler().handle(exchange, ex)).verifyErrorMatches(e -> e == ex);
    assertThat(inspectedContexts).isEmpty();
  }

  @Test
  void givenUnknownExceptionAndUnknownExceptionsEnabled_whenHandling_thenInternalServerError() {
    ProblemWebExceptionHandler handler = handler();
    handler.setUnknownExceptions(true);

    StepVerifier.create(handler.handle(exchange, new IllegalStateException("unknown")))
        .verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void givenFailingTranslation_whenHandling_thenInternalServerErrorProblemIsWritten() {
    StepVerifier.create(
            handler(new FailingOperations(), writers).handle(exchange, new IllegalStateException()))
        .verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void givenNoWriters_whenHandling_thenErrorIsPropagated() {
    ProblemException ex = new ProblemException(Problem.of(409));

    StepVerifier.create(handler(ProblemTemplate.builder().build(), List.of()).handle(exchange, ex))
        .verifyErrorMatches(e -> e == ex);
  }

  static class FailingOperations implements ProblemOperations {

    @Override
    public boolean supports(Exception ex) {
      return true;
    }

    @Override
    public Problem resolve(ProblemContext context, Exception ex, HttpHeaders headers) {
      throw new IllegalStateException("broken");
    }

    @Override
    public Problem resolve(
        ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
      throw new IllegalStateException("broken");
    }

    @Override
    public Problem postProcess(@Nullable ProblemContext context, Problem problem) {
      return problem;
    }

    @Override
    public ProblemDetail postProcess(@Nullable ProblemContext context, ProblemDetail detail) {
      return detail;
    }
  }
}
