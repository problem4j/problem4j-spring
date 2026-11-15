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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.exception-handling.mode=cooperate",
      "problem4j.extension-overrides.processed=yes"
    })
@AutoConfigureTestRestTemplate
class CooperateContentNegotiationWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;

  private ResponseEntity<String> get(String path, String accept) {
    HttpHeaders headers = new HttpHeaders();
    if (!accept.isEmpty()) {
      headers.set(HttpHeaders.ACCEPT, accept);
    }
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  @ParameterizedTest
  @CsvSource({
    "/problem-advice/resolvable, */*, 422",
    "/problem-advice/resolvable, application/json, 422",
    "/problem-advice/resolvable, application/problem+json, 422",
    "/problem-advice/resolvable, application/xml, 422",
    "/problem-advice/resolvable, text/html, 422",
    "/problem-advice/unresolvable, application/xml, 500",
    "/problem-advice/unresolvable, text/html, 500",
    "/filter-error/problem, '', 409",
    "/filter-error/problem, application/xml, 409",
    "/filter-error/problem, text/html, 409",
    "/send-error/internal-server-error, '', 500",
    "/send-error/internal-server-error, text/html, 500"
  })
  void givenExceptionRenderedByProblem4J_whenRequestingWithAnyAccept_thenProblemJson(
      String path, String accept, int status) {
    ResponseEntity<String> response = get(path, accept);

    assertThat(response.getStatusCode().value()).isEqualTo(status);
    assertThat(response.getHeaders().getContentType()).hasToString("application/problem+json");
    assertThat(response.getBody()).contains("\"processed\":\"yes\"");
  }

  @ParameterizedTest
  @CsvSource({
    "'', application/problem+json",
    "*/*, application/problem+json",
    "application/json, application/problem+json",
    "application/problem+json, application/problem+json",
    "application/xml, application/problem+xml",
    "application/problem+xml, application/problem+xml"
  })
  void givenReturnedProblem_whenRequesting_thenNegotiatedProblemContentType(
      String accept, String contentType) {
    ResponseEntity<String> response = get("/post-processing/problem", accept);

    assertThat(response.getStatusCode().value()).isEqualTo(409);
    assertThat(response.getHeaders().getContentType()).hasToString(contentType);
    assertThat(response.getBody()).contains("processed");
  }

  @ParameterizedTest
  @CsvSource({"text/html", "text/plain"})
  void givenReturnedProblem_whenRequestingNotAcceptableType_thenNotAcceptableProblem(
      String accept) {
    ResponseEntity<String> response = get("/post-processing/problem", accept);

    assertThat(response.getStatusCode().value()).isEqualTo(406);
    assertThat(response.getHeaders().getContentType()).hasToString("application/problem+json");
  }

  @ParameterizedTest
  @CsvSource({
    "'', application/problem+json",
    "*/*, application/problem+json",
    "application/json, application/problem+json",
    "application/problem+json, application/problem+json",
    "application/xml, application/problem+json",
    "application/problem+xml, application/problem+xml",
    "text/html, application/problem+json",
    "text/plain, application/problem+json"
  })
  void givenReturnedProblemDetail_whenRequesting_thenSpringFallsBackToProblemJson(
      String accept, String contentType) {
    ResponseEntity<String> response = get("/post-processing/problem-detail", accept);

    assertThat(response.getStatusCode().value()).isEqualTo(409);
    assertThat(response.getHeaders().getContentType()).hasToString(contentType);
    assertThat(response.getBody()).contains("processed");
  }
}
