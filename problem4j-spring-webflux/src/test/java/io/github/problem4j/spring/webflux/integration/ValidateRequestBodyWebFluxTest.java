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
import static org.hamcrest.Matchers.notNullValue;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.model.TestRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ValidateRequestBodyWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void givenInvalidRequestBody_shouldReturnProblem() {
    TestRequest invalidRequest = new TestRequest("", null);

    webTestClient
        .post()
        .uri("/validate-request-body")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(invalidRequest)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(notNullValue())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL)
                .extension(
                    ERRORS_EXTENSION,
                    List.of(Map.of("field", "name", "error", "must not be blank")))
                .build());
  }

  @Test
  void givenGlobalValidationViolation_shouldReturnProblemWithoutFieldName() {
    webTestClient
        .post()
        .uri("/validate-global-object")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"field\":\"value\"}")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(notNullValue())
        .value(
            problem -> {
              Map<String, String> error = new HashMap<>();
              error.put("error", "always invalid");
              Assertions.assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail(VALIDATION_FAILED_DETAIL)
                          .extension(ERRORS_EXTENSION, List.of(error))
                          .build());
            });
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"{ \"name\": \"Alice\"", "{ \"name\": \"Alice\", \"age\": \"too young\"}", ""})
  @NullSource
  void givenMalformedRequestBody_shouldReturnProblem(String json) {
    WebTestClient.RequestBodySpec spec =
        webTestClient.post().uri("/validate-request-body").contentType(MediaType.APPLICATION_JSON);

    if (json != null) {
      spec.bodyValue(json);
    }

    spec.exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(notNullValue())
        .isEqualTo(Problem.of(HttpStatus.BAD_REQUEST.value()));
  }
}
