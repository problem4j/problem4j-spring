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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureWebTestClient
class TypeMismatchWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void givenRequestWithInvalidPathVariable_shouldReturnProblem() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/type-mismatch/path-variable/abc").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("type mismatch")
                .extension("property", "id")
                .extension("kind", "integer")
                .build());
  }

  @Test
  void givenRequestWithValidPathVariable_shouldReturnOk() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/type-mismatch/path-variable/123").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody(String.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithInvalidParameterType_shouldReturnProblem() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path("/type-mismatch/request-param").queryParam("id", "abc").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("type mismatch")
                .extension("property", "id")
                .extension("kind", "integer")
                .build());
  }

  @Test
  void givenRequestWithValidParameter_shouldReturnOk() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path("/type-mismatch/request-param").queryParam("id", "123").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody(String.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithInvalidRequestHeader_shouldReturnProblem() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/type-mismatch/request-header").build())
        .header("X-Id", "abc")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("type mismatch")
                .extension("property", "X-Id")
                .extension("kind", "integer")
                .build());
  }

  @Test
  void givenRequestWithValidRequestHeader_shouldReturnOk() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/type-mismatch/request-header").build())
        .header("X-Id", "123")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody(String.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithInvalidCookieValue_shouldReturnProblem() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/type-mismatch/cookie-value").build())
        .cookie("id", "abc")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("type mismatch")
                .extension("property", "id")
                .extension("kind", "integer")
                .build());
  }

  @Test
  void givenRequestWithValidCookieValue_shouldReturnOk() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/type-mismatch/cookie-value").build())
        .cookie("id", "123")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody(String.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithInvalidEnumInRequestBody_shouldReturnProblem() {
    String json = "{\"name\": \"Test\", \"status\": \"INVALID\"}";

    webTestClient
        .post()
        .uri(uriBuilder -> uriBuilder.path("/type-mismatch/request-body").build())
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue(json)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("type mismatch")
                .extension("property", "status")
                .extension("kind", "enum")
                .build());
  }

  @Test
  void givenRequestWithValidEnumInRequestBody_shouldReturnOk() {
    String json = "{\"name\": \"Test\", \"status\": \"ACTIVE\"}";

    webTestClient
        .post()
        .uri(uriBuilder -> uriBuilder.path("/type-mismatch/request-body").build())
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue(json)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }
}
