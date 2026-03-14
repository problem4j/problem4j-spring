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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BindingKotlinWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/int     | { \"value\": null }",
        "/binding-kotlin/long    | { \"value\": null }",
        "/binding-kotlin/short   | { \"value\": null }",
        "/binding-kotlin/byte    | { \"value\": null }",
        "/binding-kotlin/float   | { \"value\": null }",
        "/binding-kotlin/double  | { \"value\": null }",
        "/binding-kotlin/boolean | { \"value\": null }"
      })
  void givenNullValue_whenPost_thenReturnTypeMismatch(String path, String json) {
    webTestClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(json)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(
            problem -> {
              String expectedKind;
              if (path.endsWith("/boolean")) {
                expectedKind = "boolean";
              } else if (path.endsWith("/float") || path.endsWith("/double")) {
                expectedKind = "number";
              } else {
                expectedKind = "integer";
              }

              assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail(TYPE_MISMATCH_DETAIL)
                          .extension(PROPERTY_EXTENSION, "value")
                          .extension(KIND_EXTENSION, expectedKind)
                          .build());
            });
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/nested/int     | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/short   | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/float   | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/boolean | { \"nested\": { \"value\": null } }"
      })
  void givenNullInNestedObjectValue_whenPost_thenReturnTypeMismatch(String path, String json) {
    webTestClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(json)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(
            problem -> {
              String expectedKind;
              if (path.endsWith("/boolean")) {
                expectedKind = "boolean";
              } else if (path.endsWith("/float")) {
                expectedKind = "number";
              } else {
                expectedKind = "integer";
              }

              assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail(TYPE_MISMATCH_DETAIL)
                          .extension(PROPERTY_EXTENSION, "nested.value")
                          .extension(KIND_EXTENSION, expectedKind)
                          .build());
            });
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/nested/int     | { \"nested\": null }",
        "/binding-kotlin/nested/int     | { }",
        "/binding-kotlin/nested/short   | { \"nested\": null }",
        "/binding-kotlin/nested/short   | { }",
        "/binding-kotlin/nested/float   | { \"nested\": null }",
        "/binding-kotlin/nested/float   | { }",
        "/binding-kotlin/nested/boolean | { \"nested\": null }",
        "/binding-kotlin/nested/boolean | { }"
      })
  void givenNullNestedObject_whenPost_thenReturnTypeMismatch(String path, String json) {
    webTestClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(json)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(
            problem ->
                assertThat(problem)
                    .isEqualTo(
                        Problem.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .detail(TYPE_MISMATCH_DETAIL)
                            .extension(PROPERTY_EXTENSION, "nested")
                            .build()));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "{ \"flag\": \"notBool\", \"timestamp\": \"notLong\", \"amount\": \"notFloat\", \"shortNested\": { \"value\": \"notShort\" } } | flag              | boolean",
        "{ \"timestamp\": \"notLong\", \"flag\": \"notBool\", \"amount\": \"notFloat\", \"shortNested\": { \"value\": \"notShort\" } } | timestamp         | integer",
        "{ \"amount\": \"notFloat\", \"flag\": \"notBool\", \"timestamp\": \"notLong\", \"shortNested\": { \"value\": \"notShort\" } } | amount            | number",
        "{ \"shortNested\": { \"value\": \"notShort\" }, \"flag\": \"notBool\", \"timestamp\": \"notLong\", \"amount\": \"notFloat\" } | shortNested.value | integer"
      })
  void givenMalformedComplexKotlinObject_whenPost_thenReturnProblemWithFirstInvalidFieldAsProperty(
      String json, String expectedProperty, String expectedKind) {
    webTestClient
        .post()
        .uri("/binding-kotlin/complex")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(json)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(
            problem ->
                assertThat(problem)
                    .isEqualTo(
                        Problem.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .detail(TYPE_MISMATCH_DETAIL)
                            .extension(PROPERTY_EXTENSION, expectedProperty)
                            .extension(KIND_EXTENSION, expectedKind)
                            .build()));
  }

  @Test
  void givenNullableProperty_whenPostNull_thenReturnOk() {
    webTestClient
        .post()
        .uri("/binding-kotlin/nullable")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{ \"value\": null }")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void givenDefaultParam_whenMissingProperty_thenUseDefault() {
    webTestClient
        .post()
        .uri("/binding-kotlin/default")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{}")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void givenListElementNullability_whenElementNull_thenBehaviorMatchesNullability() {
    webTestClient
        .post()
        .uri("/binding-kotlin/list/non-null-elements")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{ \"values\": [null] }")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(
            problem ->
                assertThat(problem)
                    .isEqualTo(
                        Problem.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .detail(TYPE_MISMATCH_DETAIL)
                            .extension(PROPERTY_EXTENSION, "values")
                            .extension(KIND_EXTENSION, "integer")
                            .build()));
  }

  @Test
  void givenListElementNullable_whenElementNull_thenReturnOk() {
    webTestClient
        .post()
        .uri("/binding-kotlin/list/nullable-elements")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{ \"values\": [null] }")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void givenMapValueNullability_whenValueNull_thenBehaviorMatchesNullability() {
    webTestClient
        .post()
        .uri("/binding-kotlin/map/non-null-values")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{ \"map\": { \"k\": null } }")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(
            problem ->
                assertThat(problem)
                    .isEqualTo(
                        Problem.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .detail(TYPE_MISMATCH_DETAIL)
                            .extension(PROPERTY_EXTENSION, "map.k")
                            .extension(KIND_EXTENSION, "integer")
                            .build()));
  }

  @Test
  void givenMapValueNullable_whenValueNull_thenReturnOk() {
    webTestClient
        .post()
        .uri("/binding-kotlin/map/nullable-values")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{ \"map\": { \"k\": null } }")
        .exchange()
        .expectStatus()
        .isOk();
  }
}
