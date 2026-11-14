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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class ExceptionHookWebFluxTest {

  private static void assertDefaultErrorResponse(WebTestClient webTestClient, String path) {
    webTestClient
        .get()
        .uri(path)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectHeader()
        .value(
            HttpHeaders.CONTENT_TYPE,
            type -> assertThat(type).doesNotStartWith(Problem.CONTENT_TYPE_JSON));
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {"problem4j.exception-handling.mode=deferred"})
  @AutoConfigureWebTestClient
  @Nested
  class WithWebExceptionHandlerEnabled {

    @Autowired private WebTestClient webTestClient;

    @Test
    void givenExceptionWithResolver_whenRequesting_thenResolvedByWebExceptionHandler() {
      webTestClient
          .get()
          .uri("/exception-hook/resolvable")
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
          .expectHeader()
          .contentType(Problem.CONTENT_TYPE_JSON)
          .expectBody()
          .jsonPath("$.status")
          .isEqualTo(422);
    }

    @Test
    void givenExceptionWithResolverHandledByAppAdvice_whenRequesting_thenAppAdviceWins() {
      webTestClient
          .get()
          .uri("/exception-hook/advice-overridden")
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.I_AM_A_TEAPOT)
          .expectBody(Problem.class)
          .isEqualTo(
              Problem.builder().title("I'm a teapot").status(418).detail("app advice").build());
    }

    @Test
    void givenUnknownException_whenRequesting_thenHandledAsInternalServerErrorProblem() {
      webTestClient
          .get()
          .uri("/exception-hook/unknown")
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
          .expectHeader()
          .contentType(Problem.CONTENT_TYPE_JSON)
          .expectBody(Problem.class)
          .isEqualTo(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.exception-handling.unknown-exceptions=false"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithFallbackDisabled {

    @Autowired private WebTestClient webTestClient;

    @Test
    void givenUnknownException_whenRequesting_thenLeftToDefaultErrorHandling() {
      assertDefaultErrorResponse(webTestClient, "/exception-hook/unknown");
    }

    @Test
    void givenExceptionWithResolver_whenRequesting_thenStillHandledByWebExceptionHandler() {
      webTestClient
          .get()
          .uri("/exception-hook/resolvable")
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
          .expectHeader()
          .contentType(Problem.CONTENT_TYPE_JSON);
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.webflux.web-exception-handler.enabled=false"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithWebExceptionHandlerDisabled {

    @Autowired private WebTestClient webTestClient;

    @Test
    void givenExceptionWithResolver_whenRequesting_thenLeftToDefaultErrorHandling() {
      assertDefaultErrorResponse(webTestClient, "/exception-hook/resolvable");
    }
  }
}
