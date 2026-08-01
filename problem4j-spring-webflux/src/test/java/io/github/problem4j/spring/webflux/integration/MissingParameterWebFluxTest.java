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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureWebTestClient
class MissingParameterWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void givenRequestWithoutPathVariable_shouldReturnProblem() {
    webTestClient
        .get()
        .uri("/missing-parameter/path-variable")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .consumeWith(
            res -> {
              Problem problem = res.getResponseBody();
              assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail("missing path variable")
                          .extension("name", "var")
                          .build());
            });
  }

  @Test
  void givenRequestWithPathVariable_shouldReturnOk() {
    webTestClient
        .get()
        .uri("/missing-parameter/path-variable/value")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutRequestParam_shouldReturnProblem() {
    webTestClient
        .get()
        .uri("/missing-parameter/request-param")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .consumeWith(
            res -> {
              Problem problem = res.getResponseBody();
              assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail("missing request param")
                          .extension("param", "param")
                          .extension("kind", "string")
                          .build());
            });
  }

  @Test
  void givenRequestWithRequestParam_shouldReturnOk() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/missing-parameter/request-param")
                    .queryParam("param", "value")
                    .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutRequestPart_shouldReturnProblem() {
    webTestClient
        .post()
        .uri("/missing-parameter/request-part")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .consumeWith(
            res -> {
              Problem problem = res.getResponseBody();
              assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail("missing request part")
                          .extension("param", "file")
                          .build());
            });
  }

  @Test
  void givenRequestWithRequestPart_shouldReturnOk() {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part(
        "file",
        new ByteArrayResource("test content".getBytes()) {
          @Override
          public String getFilename() {
            return "file.txt";
          }
        },
        MediaType.TEXT_PLAIN);

    webTestClient
        .post()
        .uri("/missing-parameter/request-part")
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutRequestHeader_shouldReturnProblem() {
    webTestClient
        .get()
        .uri("/missing-parameter/request-header")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .consumeWith(
            res -> {
              Problem problem = res.getResponseBody();
              assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail("missing header")
                          .extension("header", "X-Custom-Header")
                          .build());
            });
  }

  @Test
  void givenRequestWithRequestHeader_shouldReturnOk() {
    webTestClient
        .get()
        .uri("/missing-parameter/request-header")
        .header("X-Custom-Header", "value")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutCookieValue_shouldReturnProblem() {
    webTestClient
        .get()
        .uri("/missing-parameter/cookie-value")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .consumeWith(
            res -> {
              Problem problem = res.getResponseBody();
              assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail("missing cookie")
                          .extension("cookie", "x_session")
                          .build());
            });
  }

  @Test
  void givenRequestWithCookieValue_shouldReturnOk() {
    webTestClient
        .get()
        .uri("/missing-parameter/cookie-value")
        .cookie("x_session", "value")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutRequestAttribute_shouldReturnProblem() {
    webTestClient
        .get()
        .uri("/missing-parameter/request-attribute")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .consumeWith(
            res -> {
              Problem problem = res.getResponseBody();
              assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail("missing request attribute")
                          .extension("attribute", "attr")
                          .build());
            });
  }

  @Test
  void givenRequestWithoutSessionAttribute_shouldReturnProblem() {
    webTestClient
        .get()
        .uri("/missing-parameter/session-attribute")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> Assertions.assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("missing session attribute")
                .extension("attribute", "attr")
                .build());
  }
}
