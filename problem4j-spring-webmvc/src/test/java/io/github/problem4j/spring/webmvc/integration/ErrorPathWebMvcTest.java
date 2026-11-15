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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

class ErrorPathWebMvcTest {

  private static void assertProblem(ResponseEntity<String> response, int status) {
    assertThat(response.getStatusCode().value()).isEqualTo(status);
    assertThat(response.getHeaders().getContentType()).hasToString("application/problem+json");
    assertThat(response.getBody()).contains("\"processed\":\"yes\"");
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=fallback",
        "problem4j.extension-overrides.processed=yes",
        "spring.web.error.path=/oops"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithCustomErrorPathInFallbackMode {

    @Autowired private TestRestTemplate restTemplate;

    @ParameterizedTest
    @CsvSource({"/filter-error/problem, 409", "/send-error/internal-server-error, 500"})
    void givenErrorDispatchedToCustomPath_whenRequesting_thenRenderedAsProblem(
        String path, int status) {
      assertProblem(restTemplate.getForEntity(path, String.class), status);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=advice",
        "problem4j.extension-overrides.processed=yes",
        "spring.web.error.path=/oops"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithCustomErrorPathInAdviceMode {

    @Autowired private TestRestTemplate restTemplate;

    @ParameterizedTest
    @CsvSource({"/filter-error/problem, 500", "/send-error/internal-server-error, 500"})
    void givenErrorDispatchedToCustomPath_whenRequesting_thenRenderedAsProblem(
        String path, int status) {
      assertProblem(restTemplate.getForEntity(path, String.class), status);
    }
  }
}
