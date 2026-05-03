/*
 * Copyright 2025-2026 The Problem4J Authors
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

import static io.github.problem4j.spring.web.parameter.ViolationSupport.ERRORS_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.VALIDATION_FAILED_DETAIL;
import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
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
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL.toLowerCase())
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
        .value(v -> assertThat(v).isNotNull())
        .value(
            problem -> {
              Map<String, String> error = new HashMap<>();
              error.put("error", "always invalid");
              Assertions.assertThat(problem)
                  .isEqualTo(
                      Problem.builder()
                          .status(HttpStatus.BAD_REQUEST.value())
                          .detail(VALIDATION_FAILED_DETAIL.toLowerCase())
                          .extension(ERRORS_EXTENSION, List.of(error))
                          .build());
            });
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"{ \"name\": \"Alice\"", "not a json", "123", "[1, 2, 3]", "\"just a string\""})
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
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(Problem.of(HttpStatus.BAD_REQUEST.value()));
  }
}
