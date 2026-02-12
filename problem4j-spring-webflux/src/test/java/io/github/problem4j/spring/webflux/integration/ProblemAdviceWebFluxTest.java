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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.problem.ResolvableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProblemAdviceWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @ParameterizedTest
  @CsvSource({"string1, 1, true", "string2, 2, false"})
  void givenExtendedProblemException_shouldReturnProblem(
      String value1, Long value2, boolean value3) {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/problem-advice/exception")
                    .queryParam("value1", value1)
                    .queryParam("value2", value2)
                    .queryParam("value3", value3)
                    .build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.valueOf(418))
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .type("https://example.org/extended/" + value1)
                .title("Extended Exception")
                .status(418)
                .detail("value2:" + value2)
                .instance("https://example.org/extended/instance/" + value3)
                .build());
  }

  @ParameterizedTest
  @CsvSource({"string1, 1, true", "string2, 2, false"})
  void givenAnnotatedException_shouldReturnProblem(String value1, Long value2, boolean value3) {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/problem-advice/annotation")
                    .queryParam("value1", value1)
                    .queryParam("value2", value2)
                    .queryParam("value3", value3)
                    .build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.valueOf(418))
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .type("https://example.org/annotated/" + value1)
                .title("Annotated Exception")
                .status(418)
                .detail("value2:" + value2)
                .instance("https://example.org/annotated/instance/" + value3)
                .build());
  }

  @Test
  void givenAnnotationEmptyException_returnProblemWithUnknownStatus() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/problem-advice/annotation-empty").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(Problem.builder().status(0).build());
  }

  @Test
  void givenResolvableException_shouldReturnProblem() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/problem-advice/resolvable").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(
            Problem.builder()
                .type("http://exception.example.org/resolvable")
                .title(ResolvableException.class.getSimpleName())
                .status(422)
                .extension("package", ResolvableException.class.getPackageName())
                .build());
  }

  @Test
  void givenUnresolvableException_shouldReturnInternalServerErrorProblem() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/problem-advice/unresolvable").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(v -> assertThat(v).isNotNull())
        .isEqualTo(Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR).build());
  }
}
