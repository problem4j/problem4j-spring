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

import static io.github.problem4j.spring.web.ProblemSupport.ERRORS_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.VALIDATION_FAILED_DETAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidateMethodArgumentFailingWebMvcTest {

  private static final String VIOLATION_ERROR = "size must be between 5 and " + Integer.MAX_VALUE;

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void givenTooShortPathVariable_shouldReturnValidationProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/validate-parameter/path-variable/v", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL)
                .extension(
                    ERRORS_EXTENSION, List.of(Map.of("field", "idVar", "error", VIOLATION_ERROR)))
                .build());
  }

  @Test
  void givenTooShortRequestParam_shouldReturnValidationProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/validate-parameter/request-param?query=v", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL)
                .extension(
                    ERRORS_EXTENSION,
                    List.of(Map.of("field", "queryParam", "error", VIOLATION_ERROR)))
                .build());
  }

  @Test
  void givenTooShortRequestHeader_shouldReturnValidationProblem() throws Exception {
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

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL)
                .extension(
                    ERRORS_EXTENSION,
                    List.of(Map.of("field", "xCustomHeader", "error", VIOLATION_ERROR)))
                .build());
  }

  @Test
  void givenTooShortCookieValue_shouldReturnValidationProblem() throws Exception {
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

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(VALIDATION_FAILED_DETAIL)
                .extension(
                    ERRORS_EXTENSION,
                    List.of(Map.of("field", "xSession", "error", VIOLATION_ERROR)))
                .build());
  }

  @Test
  void givenValueViolatingAllConstraints_shouldReturnAllErrors() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/validate-parameter/multi-constraint?input=v", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensionValue(ERRORS_EXTENSION)).asInstanceOf(LIST).hasSize(2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"vvvvv", "iiiii"})
  void givenValueViolatingSingleConstraint_shouldReturnCorrectError(String input) throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/multi-constraint?input=" + input, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensionValue(ERRORS_EXTENSION)).asInstanceOf(LIST).hasSize(1);
  }

  @Test
  void givenFirstParamTooShort_shouldReturnValidationError() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/two-arg?first=v&second=anything", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
        .asInstanceOf(LIST)
        .hasSize(1)
        .allSatisfy(e -> assertThat(((Map<?, ?>) e).get("field")).isEqualTo("firstParam"));
  }

  @Test
  void givenSecondParamTooShort_shouldReturnValidationError() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/three-arg?first=anything&second=v&third=anything", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
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

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
    assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getExtensionMembers()).containsKey(ERRORS_EXTENSION);
    assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
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

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
    assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getExtensionMembers()).containsKey(ERRORS_EXTENSION);
    assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
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

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE);
    assertThat(problem.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getExtensionMembers()).containsKey(ERRORS_EXTENSION);
    assertThat(problem.getExtensionValue(ERRORS_EXTENSION))
        .asInstanceOf(LIST)
        .containsExactlyInAnyOrder(
            Map.of("field", "text", "error", "size must be between 1 and 5"),
            Map.of("field", "num", "error", "must be greater than 0"));
  }
}
