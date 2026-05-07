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

package io.github.problem4j.spring.webmvc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureTestRestTemplate
class ValidateMethodArgumentFailingWebMvcTest {

  private static final String VIOLATION_ERROR = "size must be between 5 and " + Integer.MAX_VALUE;

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @Test
  void givenTooShortPathVariable_shouldReturnValidationProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/validate-parameter/path-variable/v", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("validation failed")
                .extension("errors", List.of(Map.of("field", "idVar", "error", VIOLATION_ERROR)))
                .build());
  }

  @Test
  void givenTooShortRequestParam_shouldReturnValidationProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/validate-parameter/request-param?query=v", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("validation failed")
                .extension(
                    "errors", List.of(Map.of("field", "queryParam", "error", VIOLATION_ERROR)))
                .build());
  }

  @Test
  void givenTooShortRequestHeader_shouldReturnValidationProblem() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Custom-Header", "v");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/validate-parameter/request-header",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("validation failed")
                .extension(
                    "errors", List.of(Map.of("field", "xCustomHeader", "error", VIOLATION_ERROR)))
                .build());
  }

  @Test
  void givenTooShortCookieValue_shouldReturnValidationProblem() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "x_session=v");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/validate-parameter/cookie-value",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("validation failed")
                .extension("errors", List.of(Map.of("field", "xSession", "error", VIOLATION_ERROR)))
                .build());
  }

  @Test
  void givenValueViolatingAllConstraints_shouldReturnAllErrors() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/validate-parameter/multi-constraint?input=v", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensions().get("errors")).asInstanceOf(LIST).hasSize(2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"vvvvv", "iiiii"})
  void givenValueViolatingSingleConstraint_shouldReturnCorrectError(String input) {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/multi-constraint?input=" + input, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensions().get("errors")).asInstanceOf(LIST).hasSize(1);
  }

  @Test
  void givenFirstParamTooShort_shouldReturnValidationError() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/two-arg?first=v&second=anything", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensions().get("errors"))
        .asInstanceOf(LIST)
        .hasSize(1)
        .allSatisfy(e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("firstParam"));
  }

  @Test
  void givenSecondParamTooShort_shouldReturnValidationError() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/three-arg?first=anything&second=v&third=anything", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensions().get("errors"))
        .asInstanceOf(LIST)
        .hasSize(1)
        .allSatisfy(e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("secondParam"));
  }

  @ParameterizedTest
  @CsvSource({
    "/validate-parameter/query-object/annotated,toolong1,-1",
    "/validate-parameter/query-object/unannotated,toolong1,-1",
    "/validate-parameter/query-record/annotated,toolong1,-1",
    "/validate-parameter/query-record/unannotated,toolong1,-1"
  })
  void givenQuerySimpleObjectsWithViolations_shouldReturnValidationProblem(
      String baseUrl, String text, String number) throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl + "?text=" + text + "&number=" + number, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
    assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getExtensions()).containsKey("errors");
    assertThat(problem.getExtensions().get("errors"))
        .asInstanceOf(LIST)
        .containsExactlyInAnyOrder(
            Map.of("field", "text", "error", "size must be between 1 and 5"),
            Map.of("field", "number", "error", "must be greater than 0"));
  }

  @ParameterizedTest
  @CsvSource({
    "/validate-parameter/query-bind-object/annotated,toolong1,-1",
    "/validate-parameter/query-bind-object/unannotated,toolong1,-1",
    "/validate-parameter/query-bind-record/annotated,toolong1,-1",
    "/validate-parameter/query-bind-record/unannotated,toolong1,-1"
  })
  void givenQueryBindObjectsWithViolations_shouldReturnValidationProblem(
      String baseUrl, String text, String num) throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl + "?text=" + text + "&num=" + num, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
    assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getExtensions()).containsKey("errors");
    assertThat(problem.getExtensions().get("errors"))
        .asInstanceOf(LIST)
        .containsExactlyInAnyOrder(
            Map.of("field", "text", "error", "size must be between 1 and 5"),
            Map.of("field", "num", "error", "must be greater than 0"));
  }

  // No methods for Object-based binding with multiple ctors as it's not supported by Spring. It
  // works only for records, and it will use record's canonical ctor.

  @ParameterizedTest
  @CsvSource({
    "/validate-parameter/query-bind-ctors-record/annotated,toolong1,-1",
    "/validate-parameter/query-bind-ctors-record/unannotated,toolong1,-1"
  })
  void givenQueryBindObjectsWithMultipleCtorsWithViolations_shouldReturnValidationProblem(
      String baseUrl, String text, String num) throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl + "?text=" + text + "&num=" + num, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
    assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getExtensions()).containsKey("errors");
    assertThat(problem.getExtensions().get("errors"))
        .asInstanceOf(LIST)
        .containsExactlyInAnyOrder(
            Map.of("field", "text", "error", "size must be between 1 and 5"),
            Map.of("field", "num", "error", "must be greater than 0"));
  }
}
