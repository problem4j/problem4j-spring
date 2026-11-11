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

package io.github.problem4j.spring.webflux.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.tracing-source=micrometer",
      "problem4j.tracing-header-name=X-Trace-Id",
      "problem4j.instance-override=https://example.org/trace/{context.traceId}"
    })
@AutoConfigureWebTestClient
class TracingCoroutineWebFluxTest {

  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929b0e0e4736";
  private static final String TRACEPARENT = "00-" + TRACE_ID + "-00f067aa0ba902b7-01";

  @Autowired private WebTestClient webTestClient;

  @ParameterizedTest
  @CsvSource({
    "/coroutine/problem-exception, coroutine conflict",
    "/coroutine/resolver, Coroutine failure",
    "/coroutine/flow, Flow failure"
  })
  void givenCoroutineThrowingAfterSuspension_whenRequesting_thenTraceIdOfIncomingTraceIsUsed(
      String path, String detail) {
    webTestClient
        .get()
        .uri(path)
        .header("traceparent", TRACEPARENT)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT)
        .expectHeader()
        .valueEquals("X-Trace-Id", TRACE_ID)
        .expectBody(Problem.class)
        .value(problem ->
            assertThat(problem)
                .isEqualTo(
                    Problem.builder()
                        .type("https://example.org/coroutine")
                        .title(HttpStatus.CONFLICT.getReasonPhrase())
                        .status(HttpStatus.CONFLICT.value())
                        .detail(detail)
                        .instance("https://example.org/trace/" + TRACE_ID)
                        .build()));
  }

  @Test
  void
      givenCoroutineThrowingUnresolvableException_whenRequesting_thenTraceIdOfIncomingTraceIsUsed() {
    webTestClient
        .get()
        .uri("/coroutine/unresolvable")
        .header("traceparent", TRACEPARENT)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        .expectBody(Problem.class)
        .value(problem ->
            assertThat(problem)
                .isEqualTo(
                    Problem.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .instance("https://example.org/trace/" + TRACE_ID)
                        .build()));
  }
}
