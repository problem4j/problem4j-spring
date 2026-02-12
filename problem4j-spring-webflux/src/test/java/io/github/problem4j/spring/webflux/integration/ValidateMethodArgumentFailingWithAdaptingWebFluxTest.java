/*
 * Copyright (c) 2025-2026 Damian Malczewski
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
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.rest.ValidateMethodArgumentController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    properties = {"spring.validation.method.adapt-constraint-violations=true"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ValidateMethodArgumentFailingWithAdaptingWebFluxTest {

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
        .value(
            problem ->
                assertThat(problem)
                    .isEqualTo(
                        Problem.builder()
                            .status(ProblemStatus.BAD_REQUEST)
                            .detail(VALIDATION_FAILED_DETAIL)
                            .extension(
                                ERRORS_EXTENSION,
                                List.of(Map.of("field", "id", "error", VIOLATION_ERROR)))
                            .build()));
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
        .value(
            problem ->
                assertThat(problem)
                    .isEqualTo(
                        Problem.builder()
                            .status(ProblemStatus.BAD_REQUEST)
                            .detail(VALIDATION_FAILED_DETAIL)
                            .extension(
                                ERRORS_EXTENSION,
                                List.of(Map.of("field", "query", "error", VIOLATION_ERROR)))
                            .build()));
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
        .value(
            problem ->
                assertThat(problem)
                    .isEqualTo(
                        Problem.builder()
                            .status(ProblemStatus.BAD_REQUEST)
                            .detail(VALIDATION_FAILED_DETAIL)
                            .extension(
                                ERRORS_EXTENSION,
                                List.of(
                                    Map.of("field", "X-Custom-Header", "error", VIOLATION_ERROR)))
                            .build()));
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
        .value(
            problem ->
                assertThat(problem)
                    .isEqualTo(
                        Problem.builder()
                            .status(ProblemStatus.BAD_REQUEST)
                            .detail(VALIDATION_FAILED_DETAIL)
                            .extension(
                                ERRORS_EXTENSION,
                                List.of(Map.of("field", "x_session", "error", VIOLATION_ERROR)))
                            .build()));
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
        .value(
            problem ->
                assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                    .asInstanceOf(LIST)
                    .hasSize(1)
                    .allSatisfy(e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("first")));
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
        .value(
            problem ->
                assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
                    .asInstanceOf(LIST)
                    .hasSize(1)
                    .allSatisfy(e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("second")));
  }
}
