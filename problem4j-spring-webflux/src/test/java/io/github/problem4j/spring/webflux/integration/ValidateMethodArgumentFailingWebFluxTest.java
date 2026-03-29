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

import static io.github.problem4j.spring.web.ProblemSupport.ERRORS_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.VALIDATION_FAILED_DETAIL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.rest.ValidateMethodArgumentController;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
class ValidateMethodArgumentFailingWebFluxTest {

  private static final String VIOLATION_ERROR = "size must be between 5 and " + Integer.MAX_VALUE;

  @Autowired private WebTestClient webTestClient;

  /**
   * @see ValidateMethodArgumentController#validatePathVariable(String)
   */
  @Test
  void givenTooShortPathVariable_shouldReturnValidationProblem() {
    webTestClient
        .get()
        .uri("/validate-parameter/path-variable/v")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL.toLowerCase())
                .extension(
                    ERRORS_EXTENSION, List.of(Map.of("field", "idVar", "error", VIOLATION_ERROR)))
                .build());
  }

  /**
   * @see ValidateMethodArgumentController#validateRequestParam(String)
   */
  @Test
  void givenTooShortRequestParam_shouldReturnValidationProblem() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/validate-parameter/request-param")
                    .queryParam("query", "v")
                    .build())
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
                .detail(VALIDATION_FAILED_DETAIL.toLowerCase())
                .extension(
                    ERRORS_EXTENSION,
                    List.of(Map.of("field", "queryParam", "error", VIOLATION_ERROR)))
                .build());
  }

  /**
   * @see ValidateMethodArgumentController#validateRequestHeader(String)
   */
  @Test
  void givenTooShortRequestHeader_shouldReturnValidationProblem() {
    webTestClient
        .get()
        .uri("/validate-parameter/request-header")
        .header("X-Custom-Header", "v")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL.toLowerCase())
                .extension(
                    ERRORS_EXTENSION,
                    List.of(Map.of("field", "xCustomHeader", "error", VIOLATION_ERROR)))
                .build());
  }

  /**
   * @see ValidateMethodArgumentController#validateCookieValue(String)
   */
  @Test
  void givenTooShortCookieValue_shouldReturnValidationProblem() {
    webTestClient
        .get()
        .uri("/validate-parameter/cookie-value")
        .cookie("x_session", "v")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL.toLowerCase())
                .extension(
                    ERRORS_EXTENSION,
                    List.of(Map.of("field", "xSession", "error", VIOLATION_ERROR)))
                .build());
  }

  /**
   * @see ValidateMethodArgumentController#validateMultiConstraint(String)
   */
  @Test
  void givenValueViolatingAllConstraints_shouldReturnAllErrors() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/validate-parameter/multi-constraint")
                    .queryParam("input", "v")
                    .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .value(
            problem ->
                assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                    .asInstanceOf(LIST)
                    .hasSize(2));
  }

  /**
   * @see ValidateMethodArgumentController#validateMultiConstraint(String)
   */
  @ParameterizedTest
  @ValueSource(strings = {"vvvvv", "iiiii"})
  void givenValueViolatingSingleConstraint_shouldReturnCorrectError(String input) {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/validate-parameter/multi-constraint")
                    .queryParam("input", input)
                    .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .value(
            problem ->
                assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                    .asInstanceOf(LIST)
                    .hasSize(1));
  }

  /**
   * @see ValidateMethodArgumentController#validateTwoArguments(String, String)
   */
  @Test
  void givenFirstParamTooShort_shouldReturnValidationError() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/validate-parameter/two-arg")
                    .queryParam("first", "v")
                    .queryParam("second", "anything")
                    .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .value(
            problem ->
                assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                    .asInstanceOf(LIST)
                    .hasSize(1)
                    .allSatisfy(
                        e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("firstParam")));
  }

  /**
   * @see ValidateMethodArgumentController#validateThreeArguments(String, String, String)
   */
  @Test
  void givenSecondParamTooShort_shouldReturnValidationError() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/validate-parameter/three-arg")
                    .queryParam("first", "anything")
                    .queryParam("second", "v")
                    .queryParam("third", "anything")
                    .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .value(
            problem ->
                assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                    .asInstanceOf(LIST)
                    .hasSize(1)
                    .allSatisfy(
                        e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("secondParam")));
  }

  @ParameterizedTest
  @CsvSource({
    "/validate-parameter/query-object/annotated,toolong1,-1",
    "/validate-parameter/query-object/unannotated,toolong1,-1",
    "/validate-parameter/query-record/annotated,toolong1,-1",
    "/validate-parameter/query-record/unannotated,toolong1,-1"
  })
  void givenQuerySimpleObjectsWithViolations_shouldReturnValidationProblem(
      String baseUrl, String text, String number) {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(baseUrl)
                    .queryParam("text", text)
                    .queryParam("number", number)
                    .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .value(
            problem -> {
              assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
              assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
              assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

              assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                  .asInstanceOf(LIST)
                  .containsExactlyInAnyOrder(
                      Map.of("field", "text", "error", "size must be between 1 and 5"),
                      Map.of("field", "number", "error", "must be greater than 0"));
            });
  }

  @ParameterizedTest
  @CsvSource({
    "/validate-parameter/query-bind-object/annotated,toolong1,-1",
    "/validate-parameter/query-bind-object/unannotated,toolong1,-1",
    "/validate-parameter/query-bind-record/annotated,toolong1,-1",
    "/validate-parameter/query-bind-record/unannotated,toolong1,-1"
  })
  void givenQueryBindObjectsWithViolations_shouldReturnValidationProblem(
      String baseUrl, String text, String num) {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(baseUrl).queryParam("text", text).queryParam("num", num).build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .value(
            problem -> {
              assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
              assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
              assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

              assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                  .asInstanceOf(LIST)
                  .containsExactlyInAnyOrder(
                      Map.of("field", "text", "error", "size must be between 1 and 5"),
                      Map.of("field", "num", "error", "must be greater than 0"));
            });
  }

  // No methods for Object-based binding with multiple ctors as it's not supported by Spring. It
  // works only for records, and it will use record's canonical ctor.

  @ParameterizedTest
  @CsvSource({
    "/validate-parameter/query-bind-ctors-record/annotated,toolong1,-1",
    "/validate-parameter/query-bind-ctors-record/unannotated,toolong1,-1"
  })
  void givenQueryBindObjectsWithMultipleCtorsWithViolations_shouldReturnValidationProblem(
      String baseUrl, String text, String num) {

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(baseUrl).queryParam("text", text).queryParam("num", num).build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .value(
            problem -> {
              assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
              assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
              assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

              assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                  .asInstanceOf(LIST)
                  .containsExactlyInAnyOrder(
                      Map.of("field", "text", "error", "size must be between 1 and 5"),
                      Map.of("field", "num", "error", "must be greater than 0"));
            });
  }
}
