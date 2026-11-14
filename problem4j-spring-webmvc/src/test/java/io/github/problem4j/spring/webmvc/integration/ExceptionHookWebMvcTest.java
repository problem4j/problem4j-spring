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

package io.github.problem4j.spring.webmvc.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.json.JsonMapper;

class ExceptionHookWebMvcTest {

  private static void assertDefaultErrorResponse(ResponseEntity<String> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getHeaders().getContentType())
        .isNotNull()
        .satisfies(type -> assertThat(type.isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue())
        .satisfies(type ->
            assertThat(type.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)).isFalse());
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {"problem4j.exception-handling.mode=cooperate"})
  @AutoConfigureTestRestTemplate
  @Nested
  class WithExceptionResolverEnabled {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenExceptionWithResolver_whenRequesting_thenResolvedByExceptionResolver() {
      ResponseEntity<String> response =
          restTemplate.getForEntity("/exception-hook/resolvable", String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
      assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE_JSON);
      assertThat(jsonMapper.readValue(response.getBody(), Problem.class).getStatus())
          .isEqualTo(422);
    }

    @Test
    void givenExceptionWithResolverHandledByAppAdvice_whenRequesting_thenAppAdviceWins() {
      ResponseEntity<String> response =
          restTemplate.getForEntity("/exception-hook/advice-overridden", String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
      assertThat(jsonMapper.readValue(response.getBody(), Problem.class))
          .isEqualTo(
              Problem.builder().title("I'm a teapot").status(418).detail("app advice").build());
    }

    @Test
    void givenUnknownException_whenRequesting_thenResolvedAsInternalServerErrorProblem() {
      ResponseEntity<String> response =
          restTemplate.getForEntity("/exception-hook/unknown", String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE_JSON);
      assertThat(jsonMapper.readValue(response.getBody(), Problem.class))
          .isEqualTo(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @Test
    void givenSendError_whenRequesting_thenRenderedByProblemErrorController() {
      ResponseEntity<String> response =
          restTemplate.getForEntity("/send-error/internal-server-error", String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE_JSON);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=cooperate",
        "problem4j.exception-handling.unknown-exceptions=false",
        "problem4j.webmvc.error-controller.enabled=false"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithFallbackDisabled {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenUnknownException_whenRequesting_thenLeftToDefaultErrorHandling() {
      assertDefaultErrorResponse(
          restTemplate.getForEntity("/exception-hook/unknown", String.class));
    }

    @Test
    void givenExceptionWithResolver_whenRequesting_thenStillResolvedByExceptionResolver() {
      ResponseEntity<String> response =
          restTemplate.getForEntity("/exception-hook/resolvable", String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
      assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE_JSON);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=cooperate",
        "problem4j.exception-handling.unknown-exceptions=false"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithFallbackDisabledAndErrorController {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void givenUnknownException_whenRequesting_thenRenderedByProblemErrorController() {
      ResponseEntity<String> response =
          restTemplate.getForEntity("/exception-hook/unknown", String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE_JSON);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=cooperate",
        "problem4j.webmvc.handler-exception-resolver.enabled=false",
        "problem4j.webmvc.error-controller.enabled=false"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithExceptionResolverDisabled {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void givenExceptionWithResolver_whenRequesting_thenLeftToDefaultErrorHandling() {
      assertDefaultErrorResponse(
          restTemplate.getForEntity("/exception-hook/resolvable", String.class));
    }
  }
}
