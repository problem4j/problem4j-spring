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

import static io.github.problem4j.spring.web.ProblemSupport.ATTRIBUTE_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.COOKIE_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.HEADER_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_COOKIE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_HEADER_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_PATH_VARIABLE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_REQUEST_ATTRIBUTE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_REQUEST_PARAM_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_REQUEST_PART_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_SESSION_ATTRIBUTE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.NAME_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.PARAM_EXTENSION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MissingParameterWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void givenRequestWithoutPathVariable_shouldReturnProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/path-variable", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(MISSING_PATH_VARIABLE_DETAIL)
                .extension(NAME_EXTENSION, "var")
                .build());
  }

  @Test
  void givenRequestWithPathVariable_shouldReturnOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/path-variable/value", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutRequestParam_shouldReturnProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/request-param", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(MISSING_REQUEST_PARAM_DETAIL)
                .extension(PARAM_EXTENSION, "param")
                .extension(KIND_EXTENSION, "string")
                .build());
  }

  @Test
  void givenRequestWithRequestParam_shouldReturnOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/request-param?param=value", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutRequestPartParam_shouldReturnProblem() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> request =
        new HttpEntity<>(new LinkedMultiValueMap<>(), headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity("/missing-parameter/request-part", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(MISSING_REQUEST_PART_DETAIL)
                .extension(PARAM_EXTENSION, "file")
                .build());
  }

  @Test
  void givenRequestWithoutRequestPartHeader_shouldReturnProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/missing-parameter/request-part",
            new HttpEntity<>(null, new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem).isEqualTo(Problem.builder().status(ProblemStatus.BAD_REQUEST).build());
  }

  @Test
  void givenRequestWithRequestPart_shouldReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new ByteArrayResource("test content".getBytes()) {
          @Override
          public String getFilename() {
            return "test.txt";
          }
        });
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity("/missing-parameter/request-part", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutRequestHeader_shouldReturnProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/request-header", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(MISSING_HEADER_DETAIL)
                .extension(HEADER_EXTENSION, "X-Custom-Header")
                .build());
  }

  @Test
  void givenRequestWithRequestHeader_shouldReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Custom-Header", "value");
    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/missing-parameter/request-header", HttpMethod.GET, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutCookieValue_shouldReturnProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/cookie-value", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(MISSING_COOKIE_DETAIL)
                .extension(COOKIE_EXTENSION, "x_session")
                .build());
  }

  @Test
  void givenRequestWithCookieValue_shouldReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "x_session=value");
    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/missing-parameter/cookie-value", HttpMethod.GET, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenRequestWithoutRequestAttribute_shouldReturnProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/request-attribute", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(MISSING_REQUEST_ATTRIBUTE_DETAIL)
                .extension(ATTRIBUTE_EXTENSION, "attr")
                .build());
  }

  @Test
  void givenRequestWithoutSessionAttribute_shouldReturnProblem() throws Exception {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/session-attribute", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    Problem problem = objectMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.BAD_REQUEST)
                .detail(MISSING_SESSION_ATTRIBUTE_DETAIL)
                .extension(ATTRIBUTE_EXTENSION, "attr")
                .build());
  }
}
