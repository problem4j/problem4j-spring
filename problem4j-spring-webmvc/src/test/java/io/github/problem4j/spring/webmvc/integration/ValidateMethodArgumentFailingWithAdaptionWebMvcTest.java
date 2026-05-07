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

    assertThat(problem.getExtensionValue("errors")).asInstanceOf(LIST).hasSize(2);
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

    assertThat(problem.getExtensionValue("errors")).asInstanceOf(LIST).hasSize(1);
  }

  @Test
  void givenFirstParamTooShort_shouldReturnValidationError() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/two-arg?first=v&second=anything", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensionValue("errors"))
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

    assertThat(problem.getExtensionValue("errors"))
        .asInstanceOf(LIST)
        .hasSize(1)
        .allSatisfy(e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("second"));
  }
}
