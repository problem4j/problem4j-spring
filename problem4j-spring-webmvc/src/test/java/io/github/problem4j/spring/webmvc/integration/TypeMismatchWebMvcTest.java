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
package io.github.problem4j.spring.webmvc.integration;

import static io.github.problem4j.spring.web.ProblemSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.PROPERTY_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.TYPE_MISMATCH_DETAIL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Test;
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
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class TypeMismatchWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @Test
  void givenRequestWithInvalidPathVariable_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/type-mismatch/path-variable/abc", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "id")
                .extension(KIND_EXTENSION, "integer")
                .build());
  }

  @Test
  void givenRequestWithValidPathVariable_shouldReturnOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/type-mismatch/path-variable/123", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenRequestWithInvalidParameterType_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/type-mismatch/request-param?id=abc", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "id")
                .extension(KIND_EXTENSION, "integer")
                .build());
  }

  @Test
  void givenRequestWithValidParameter_shouldReturnOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/type-mismatch/request-param?id=123", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenRequestWithInvalidRequestHeader_shouldReturnProblem() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Id", "abc");

    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/type-mismatch/request-header", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "X-Id")
                .extension(KIND_EXTENSION, "integer")
                .build());
  }

  @Test
  void givenRequestWithValidRequestHeader_shouldReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Id", "123");

    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/type-mismatch/request-header", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenRequestWithInvalidCookieValue_shouldReturnProblem() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "id=abc");

    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<String> response =
        restTemplate.exchange("/type-mismatch/cookie-value", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "id")
                .extension(KIND_EXTENSION, "integer")
                .build());
  }

  @Test
  void givenRequestWithValidCookieValue_shouldReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "id=123");

    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<String> response =
        restTemplate.exchange("/type-mismatch/cookie-value", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }
}
