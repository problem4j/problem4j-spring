/*
 * Copyright (c) 2025-2026 The Problem4J Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.problem4j.spring.webflux.integration;

import static io.github.problem4j.spring.web.ProblemSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.PROPERTY_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.TYPE_MISMATCH_DETAIL;
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
                .detail(TYPE_MISMATCH_DETAIL.toLowerCase())
                .extension(PROPERTY_EXTENSION, "id")
                .extension(KIND_EXTENSION, "integer")
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
                .detail(TYPE_MISMATCH_DETAIL.toLowerCase())
                .extension(PROPERTY_EXTENSION, "id")
                .extension(KIND_EXTENSION, "integer")
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
                .detail(TYPE_MISMATCH_DETAIL.toLowerCase())
                .extension(PROPERTY_EXTENSION, "X-Id")
                .extension(KIND_EXTENSION, "integer")
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
                .detail(TYPE_MISMATCH_DETAIL.toLowerCase())
                .extension(PROPERTY_EXTENSION, "id")
                .extension(KIND_EXTENSION, "integer")
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
                .detail(TYPE_MISMATCH_DETAIL.toLowerCase())
                .extension(PROPERTY_EXTENSION, "status")
                .extension(KIND_EXTENSION, "enum")
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
