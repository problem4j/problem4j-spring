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

import static org.hamcrest.Matchers.notNullValue;

import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.rest.ValidateMethodArgumentController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureWebTestClient
class ValidateMethodArgumentPassingWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  /**
   * @see ValidateMethodArgumentController#validatePathVariable(String)
   */
  @Test
  void givenValidPathVariable_shouldReturnOk() {
    webTestClient
        .get()
        .uri("/validate-parameter/path-variable/value")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(notNullValue())
        .isEqualTo("OK");
  }

  /**
   * @see ValidateMethodArgumentController#validateRequestParam(String)
   */
  @Test
  void givenValidRequestParam_shouldReturnOk() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/validate-parameter/request-param")
                    .queryParam("query", "value")
                    .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(notNullValue())
        .isEqualTo("OK");
  }

  /**
   * @see ValidateMethodArgumentController#validateRequestHeader(String)
   */
  @Test
  void givenValidRequestHeader_shouldReturnOk() {
    webTestClient
        .get()
        .uri("/validate-parameter/request-header")
        .header("X-Custom-Header", "value")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(notNullValue())
        .isEqualTo("OK");
  }

  /**
   * @see ValidateMethodArgumentController#validateCookieValue(String)
   */
  @Test
  void givenValidCookieValue_shouldReturnOk() {
    webTestClient
        .get()
        .uri("/validate-parameter/cookie-value")
        .cookie("x_session", "value")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(notNullValue())
        .isEqualTo("OK");
  }

  /**
   * @see ValidateMethodArgumentController#validateMultiConstraint(String)
   */
  @Test
  void givenBothParamsValid_shouldReturnOk() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/validate-parameter/two-arg")
                    .queryParam("first", "validVal")
                    .queryParam("second", "anything")
                    .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(notNullValue())
        .isEqualTo("OK");
  }

  /**
   * @see ValidateMethodArgumentController#validateThreeArguments(String, String, String)
   */
  @Test
  void givenThreeParamsValid_shouldReturnOk() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/validate-parameter/three-arg")
                    .queryParam("first", "anything")
                    .queryParam("second", "validVal")
                    .queryParam("third", "anything")
                    .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(notNullValue())
        .isEqualTo("OK");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/validate-parameter/query-object/annotated",
        "/validate-parameter/query-object/unannotated",
        "/validate-parameter/query-record/annotated",
        "/validate-parameter/query-record/unannotated",
      })
  void givenQuerySimpleObjectsWithoutViolations_shouldReturnOk(String baseUrl) {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(baseUrl)
                    .queryParam("text", "fine")
                    .queryParam("number", "1")
                    .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/validate-parameter/query-bind-object/annotated",
        "/validate-parameter/query-bind-object/unannotated",
        "/validate-parameter/query-bind-record/annotated",
        "/validate-parameter/query-bind-record/unannotated",
      })
  void givenQueryBindObjectsWithoutViolations_shouldReturnOk(String baseUrl) {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(baseUrl).queryParam("text", "fine").queryParam("num", "1").build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }

  // No methods for Object-based binding with multiple ctors as it's not supported by Spring.
  // It works only for records, and it will use record's canonical ctor.

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/validate-parameter/query-bind-ctors-record/annotated",
        "/validate-parameter/query-bind-ctors-record/unannotated",
      })
  void givenQueryBindObjectsWithMultipleCtorsWithoutViolations_shouldReturnOk(String baseUrl) {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(baseUrl).queryParam("text", "fine").queryParam("num", "1").build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("OK");
  }
}
