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
import org.junit.jupiter.api.Nested;
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

class ProblemContextArgumentWebMvcTest {

  private static final String TRACE_ID = "argument-trace";

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {"problem4j.tracing.source=simple", "problem4j.tracing-header-name=X-Trace-Id"})
  @AutoConfigureTestRestTemplate
  @Nested
  class WithProblemContextFilter {

    @Autowired private TestRestTemplate restTemplate;

    @ParameterizedTest
    @ValueSource(
        strings = {
          "/problem-context/argument",
          "/problem-context/request-attribute",
          "/coroutine/problem-context/argument",
          "/coroutine/problem-context/request-attribute"
        })
    void givenProblemContextParameter_whenRequesting_thenProblemContextOfRequestIsResolved(
        String path) {
      ResponseEntity<String> response = exchange(restTemplate, path);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(TRACE_ID);
    }

    @Test
    void givenAnnotatedAndNotAnnotatedParameters_whenRequesting_thenSameProblemContextIsResolved() {
      ResponseEntity<String> response = exchange(restTemplate, "/problem-context/both");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo("true");
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.tracing.source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.webmvc.problem-context-filter.enabled=false"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithoutProblemContextFilter {

    @Autowired private TestRestTemplate restTemplate;

    @ParameterizedTest
    @ValueSource(strings = {"/problem-context/argument", "/coroutine/problem-context/argument"})
    void givenNotAnnotatedProblemContextParameter_whenRequesting_thenNullIsResolved(String path) {
      ResponseEntity<String> response = exchange(restTemplate, path);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo("no-context");
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "/problem-context/request-attribute",
          "/coroutine/problem-context/request-attribute"
        })
    void givenAnnotatedProblemContextParameter_whenRequesting_thenBadRequestIsReturned(
        String path) {
      ResponseEntity<String> response = exchange(restTemplate, path);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
  }

  private static ResponseEntity<String> exchange(TestRestTemplate restTemplate, String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Trace-Id", TRACE_ID);
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }
}
