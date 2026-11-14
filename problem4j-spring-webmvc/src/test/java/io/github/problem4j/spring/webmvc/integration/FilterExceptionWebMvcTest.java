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

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Nested;
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
import tools.jackson.databind.json.JsonMapper;

class FilterExceptionWebMvcTest {

  private static final String TRACE_ID = "filter-trace";

  private static ResponseEntity<String> get(TestRestTemplate restTemplate, String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Trace-Id", TRACE_ID);
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  private static void assertProblem(
      ResponseEntity<String> response, JsonMapper jsonMapper, int status) {
    assertThat(response.getStatusCode().value()).isEqualTo(status);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE_JSON);
    assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem.getStatus()).isEqualTo(status);
    assertThat(problem.getExtensions()).containsEntry("traceId", TRACE_ID);
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=advice",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.extension-overrides.traceId={context.traceId}"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithAdviceMode {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @ParameterizedTest
    @CsvSource({"/filter-error/resolvable", "/filter-error/problem", "/filter-error/unknown"})
    void givenExceptionFromFilter_whenRequesting_thenRenderedAsStatusOnlyProblemWithTraceId(
        String path) {
      assertProblem(get(restTemplate, path), jsonMapper, 500);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.extension-overrides.traceId={context.traceId}"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithDeferredMode {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @ParameterizedTest
    @CsvSource({
      "/filter-error/resolvable, 422",
      "/filter-error/problem, 409",
      "/filter-error/unknown, 500"
    })
    void givenExceptionFromFilter_whenRequesting_thenTranslatedWithTraceId(
        String path, int status) {
      assertProblem(get(restTemplate, path), jsonMapper, status);
    }
  }
}
