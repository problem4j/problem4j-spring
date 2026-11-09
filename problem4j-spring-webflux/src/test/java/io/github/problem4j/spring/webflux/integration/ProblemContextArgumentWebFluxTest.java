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

import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

class ProblemContextArgumentWebFluxTest {

  private static final String TRACE_ID = "argument-trace";

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {"problem4j.tracing.source=simple", "problem4j.tracing-header-name=X-Trace-Id"})
  @AutoConfigureWebTestClient
  @Nested
  class WithProblemContextFilter {

    @Autowired private WebTestClient webTestClient;

    @ParameterizedTest
    @ValueSource(
        strings = {
          "/problem-context/argument",
          "/problem-context/optional",
          "/problem-context/request-attribute",
          "/coroutine/problem-context/argument",
          "/coroutine/problem-context/request-attribute"
        })
    void givenProblemContextParameter_whenRequesting_thenProblemContextOfRequestIsResolved(
        String path) {
      EntityExchangeResult<String> result = exchange(webTestClient, path);

      assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);
      assertThat(result.getResponseBody()).isEqualTo(TRACE_ID);
    }

    @Test
    void givenAnnotatedAndNotAnnotatedParameters_whenRequesting_thenSameProblemContextIsResolved() {
      EntityExchangeResult<String> result = exchange(webTestClient, "/problem-context/both");

      assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);
      assertThat(result.getResponseBody()).isEqualTo("true");
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.tracing.source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.webflux.problem-context-filter.enabled=false"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithoutProblemContextFilter {

    @Autowired private WebTestClient webTestClient;

    @ParameterizedTest
    @ValueSource(
        strings = {
          "/problem-context/argument",
          "/problem-context/optional",
          "/coroutine/problem-context/argument"
        })
    void givenNotAnnotatedProblemContextParameter_whenRequesting_thenNoContextIsResolved(
        String path) {
      EntityExchangeResult<String> result = exchange(webTestClient, path);

      assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);
      assertThat(result.getResponseBody()).isEqualTo("no-context");
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "/problem-context/request-attribute",
          "/coroutine/problem-context/request-attribute"
        })
    void givenAnnotatedProblemContextParameter_whenRequesting_thenBadRequestIsReturned(
        String path) {
      EntityExchangeResult<String> result = exchange(webTestClient, path);

      assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
  }

  private static EntityExchangeResult<String> exchange(WebTestClient webTestClient, String path) {
    return webTestClient
        .get()
        .uri(path)
        .header("X-Trace-Id", TRACE_ID)
        .exchange()
        .expectBody(String.class)
        .returnResult();
  }
}
