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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class ObservationErrorWebFluxInspectorTest {

  @ParameterizedTest
  @ValueSource(ints = {500, 503})
  void givenServerErrorStatus_whenInspecting_thenExceptionIsRecorded(int status) {
    MockServerWebExchange exchange = exchange();
    ServerRequestObservationContext observationContext = withObservationContext(exchange);
    Exception ex = new IllegalStateException("boom");

    inspect(new ObservationErrorWebFluxInspector(), ex, status, exchange);

    assertThat(observationContext.getError()).isSameAs(ex);
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 404, 422})
  void givenClientErrorStatus_whenInspectingWithDefaults_thenExceptionIsNotRecorded(int status) {
    MockServerWebExchange exchange = exchange();
    ServerRequestObservationContext observationContext = withObservationContext(exchange);

    inspect(new ObservationErrorWebFluxInspector(), new IllegalStateException(), status, exchange);

    assertThat(observationContext.getError()).isNull();
  }

  @Test
  void givenCustomStatusFilter_whenStatusMatches_thenExceptionIsRecorded() {
    MockServerWebExchange exchange = exchange();
    ServerRequestObservationContext observationContext = withObservationContext(exchange);
    Exception ex = new IllegalArgumentException();

    inspect(new ObservationErrorWebFluxInspector(status -> true), ex, 400, exchange);

    assertThat(observationContext.getError()).isSameAs(ex);
  }

  @Test
  void givenErrorAlreadyRecorded_whenInspecting_thenErrorIsNotReplaced() {
    MockServerWebExchange exchange = exchange();
    ServerRequestObservationContext observationContext = withObservationContext(exchange);
    Exception existing = new IllegalStateException("existing");
    observationContext.setError(existing);

    inspect(new ObservationErrorWebFluxInspector(), new IllegalStateException(), 500, exchange);

    assertThat(observationContext.getError()).isSameAs(existing);
  }

  @Test
  void givenNoObservationContext_whenInspecting_thenNothingHappens() {
    MockServerWebExchange exchange = exchange();

    assertThatNoException()
        .isThrownBy(() ->
            inspect(
                new ObservationErrorWebFluxInspector(),
                new IllegalStateException(),
                500,
                exchange));
  }

  private static MockServerWebExchange exchange() {
    return MockServerWebExchange.from(MockServerHttpRequest.get("/"));
  }

  private static ServerRequestObservationContext withObservationContext(
      MockServerWebExchange exchange) {
    ServerRequestObservationContext observationContext =
        new ServerRequestObservationContext(
            exchange.getRequest(), exchange.getResponse(), exchange.getAttributes());
    exchange
        .getAttributes()
        .put(
            ServerRequestObservationContext.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
            observationContext);
    return observationContext;
  }

  private static void inspect(
      ObservationErrorWebFluxInspector inspector,
      Exception ex,
      int status,
      MockServerWebExchange exchange) {
    inspector.inspect(
        ProblemContext.create(),
        Problem.builder().status(status).build(),
        ex,
        new HttpHeaders(),
        HttpStatusCode.valueOf(status),
        exchange);
  }
}
