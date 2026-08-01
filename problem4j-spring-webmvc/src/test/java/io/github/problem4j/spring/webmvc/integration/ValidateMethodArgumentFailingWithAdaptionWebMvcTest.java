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

package io.github.problem4j.spring.webmvc.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
    properties = {
      "spring.validation.method.adapt-constraint-violations=true",
      "problem4j.detail-format=lowercase"
    })
@AutoConfigureTestRestTemplate
class ValidateMethodArgumentFailingWithAdaptionWebMvcTest {

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
                .extension("errors", List.of(Map.of("field", "id", "error", VIOLATION_ERROR)))
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
                .extension("errors", List.of(Map.of("field", "query", "error", VIOLATION_ERROR)))
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
                    "errors", List.of(Map.of("field", "X-Custom-Header", "error", VIOLATION_ERROR)))
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
                .extension(
                    "errors", List.of(Map.of("field", "x_session", "error", VIOLATION_ERROR)))
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
        .allSatisfy(e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("first"));
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
        .allSatisfy(e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("second"));
  }
}
