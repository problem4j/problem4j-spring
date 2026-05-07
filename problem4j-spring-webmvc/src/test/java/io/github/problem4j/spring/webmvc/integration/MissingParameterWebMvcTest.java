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

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureTestRestTemplate
class MissingParameterWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @Test
  void givenRequestWithoutPathVariable_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/path-variable", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("missing path variable")
                .extension("name", "var")
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
  void givenRequestWithoutRequestParam_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/request-param", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("missing request param")
                .extension("param", "param")
                .extension("kind", "string")
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
  void givenRequestWithoutRequestPartParam_shouldReturnProblem() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> request =
        new HttpEntity<>(new LinkedMultiValueMap<>(), headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity("/missing-parameter/request-part", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("missing request part")
                .extension("param", "file")
                .build());
  }

  @Test
  void givenRequestWithoutRequestPartHeader_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/missing-parameter/request-part",
            new HttpEntity<>(null, new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem).isEqualTo(Problem.builder().status(HttpStatus.BAD_REQUEST.value()).build());
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
  void givenRequestWithoutRequestHeader_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/request-header", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("missing header")
                .extension("header", "X-Custom-Header")
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
  void givenRequestWithoutCookieValue_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/cookie-value", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("missing cookie")
                .extension("cookie", "x_session")
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
  void givenRequestWithoutRequestAttribute_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/request-attribute", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("missing request attribute")
                .extension("attribute", "attr")
                .build());
  }

  @Test
  void givenRequestWithoutSessionAttribute_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/missing-parameter/session-attribute", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("missing session attribute")
                .extension("attribute", "attr")
                .build());
  }
}
