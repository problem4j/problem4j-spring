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
import java.net.URI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

class PostProcessingWebMvcTest {

  private static final String TRACE_ID = "post-processing-trace";

  private static ResponseEntity<String> get(TestRestTemplate restTemplate, String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Trace-Id", TRACE_ID);
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=cooperate",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}",
        "problem4j.extension-overrides.traceId={context.traceId}",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithResponseBodyAdviceEnabled {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @ParameterizedTest
    @CsvSource({
      "/post-processing/problem, 409, returned problem",
      "/post-processing/problem-detail, 409, returned problem detail",
      "/post-processing/local-handler, 400, local handler",
      "/post-processing/advice-handler, 422, advice handler"
    })
    void givenReturnedProblemOrProblemDetail_whenRequesting_thenPostProcessed(
        String path, int status, String detail) {
      ResponseEntity<String> response = get(restTemplate, path);

      assertThat(response.getStatusCode().value()).isEqualTo(status);
      assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE_JSON);

      Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
      assertThat(problem.getStatus()).isEqualTo(status);
      assertThat(problem.getDetail()).isEqualTo(detail);
      assertThat(problem.getInstance())
          .isEqualTo(URI.create("https://example.org/trace/" + TRACE_ID));
      assertThat(problem.getExtensions()).containsEntry("traceId", TRACE_ID);
    }

    @Test
    void givenProblemDetailWithoutType_whenRequesting_thenTypeOmitted() {
      ResponseEntity<String> response = get(restTemplate, "/post-processing/advice-handler");

      assertThat(jsonMapper.readTree(response.getBody()).has("type")).isFalse();
    }

    @Test
    void givenProblemDetailFromAdvice_whenRequesting_thenOwnPropertiesRetained() {
      ResponseEntity<String> response = get(restTemplate, "/post-processing/advice-handler");

      Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
      assertThat(problem.getExtensions())
          .containsEntry("origin", "advice")
          .containsEntry("traceId", TRACE_ID);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=cooperate",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}",
        "problem4j.extension-overrides.traceId={context.traceId}",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.webmvc.response-body-advice.enabled=false"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithResponseBodyAdviceDisabled {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenReturnedProblemDetail_whenRequesting_thenNotPostProcessed() {
      ResponseEntity<String> response = get(restTemplate, "/post-processing/advice-handler");

      Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
      assertThat(problem.getInstance())
          .isNotEqualTo(URI.create("https://example.org/trace/" + TRACE_ID));
      assertThat(problem.getExtensions()).doesNotContainKey("traceId");
    }
  }
}
