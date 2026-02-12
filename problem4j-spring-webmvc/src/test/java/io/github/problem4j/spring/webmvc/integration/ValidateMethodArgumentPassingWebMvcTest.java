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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
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

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ValidateMethodArgumentPassingWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void givenValidPathVariable_shouldReturnOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/validate-parameter/path-variable/value", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenValidRequestParam_shouldReturnOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/validate-parameter/request-param?query=value", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenValidRequestHeader_shouldReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Custom-Header", "value");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/validate-parameter/request-header",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenValidCookieValue_shouldReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "x_session=value");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/validate-parameter/cookie-value",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenBothParamsValid_shouldReturnOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/two-arg?first=validVal&second=anything", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @Test
  void givenThreeParamsValid_shouldReturnOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/validate-parameter/three-arg?first=anything&second=validVal&third=anything",
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/validate-parameter/query-object/annotated",
        "/validate-parameter/query-object/unannotated",
        "/validate-parameter/query-record/annotated",
        "/validate-parameter/query-record/unannotated",
      })
  void givenQuerySimpleObjectsWithViolations_shouldReturnValidationProblem(String baseUrl) {
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl + "?text=fine&number=1", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/validate-parameter/query-bind-object/annotated",
        "/validate-parameter/query-bind-object/unannotated",
        "/validate-parameter/query-bind-record/annotated",
        "/validate-parameter/query-bind-record/unannotated",
      })
  void givenQueryBindObjectsWithViolations_shouldReturnValidationProblem(String baseUrl) {
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl + "?text=fine&num=1", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  // No methods for Object-based binding with multiple ctors as it's not supported by Spring. It
  // works only for records, and it will use record's canonical ctor.

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/validate-parameter/query-bind-ctors-record/annotated",
        "/validate-parameter/query-bind-ctors-record/unannotated",
      })
  void givenQueryBindObjectsWithMultipleCtorsWithViolations_shouldReturnValidationProblem(
      String baseUrl) {
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl + "?text=fine&num=1", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }
}
