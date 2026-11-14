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

package io.github.problem4j.spring.webflux.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import java.net.URI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import tools.jackson.databind.json.JsonMapper;

class PostProcessingWebFluxTest {

  private static final String TRACE_ID = "post-processing-trace";

  private static WebTestClient.ResponseSpec get(WebTestClient webTestClient, String path) {
    return webTestClient.get().uri(path).header("X-Trace-Id", TRACE_ID).exchange();
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}",
        "problem4j.extension-overrides.traceId={context.traceId}",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithResultHandlerEnabled {

    @Autowired private WebTestClient webTestClient;
    @Autowired private JsonMapper jsonMapper;

    @ParameterizedTest
    @CsvSource({
      "/post-processing/problem, 409, returned problem",
      "/post-processing/problem-detail, 409, returned problem detail",
      "/post-processing/problem-mono, 409, returned problem",
      "/post-processing/problem-detail-mono, 409, returned problem detail",
      "/post-processing/problem-suspend, 409, returned problem",
      "/post-processing/local-handler, 400, local handler",
      "/post-processing/advice-handler, 422, advice handler"
    })
    void givenReturnedProblemOrProblemDetail_whenRequesting_thenPostProcessed(
        String path, int status, String detail) {
      String body =
          get(webTestClient, path)
              .expectStatus()
              .isEqualTo(status)
              .expectHeader()
              .contentType(Problem.CONTENT_TYPE_JSON)
              .expectBody(String.class)
              .returnResult()
              .getResponseBody();

      Problem problem = jsonMapper.readValue(body, Problem.class);
      assertThat(problem.getStatus()).isEqualTo(status);
      assertThat(problem.getDetail()).isEqualTo(detail);
      assertThat(problem.getInstance())
          .isEqualTo(URI.create("https://example.org/trace/" + TRACE_ID));
      assertThat(problem.getExtensions()).containsEntry("traceId", TRACE_ID);
    }

    @Test
    void givenProblemDetailWithoutType_whenRequesting_thenTypeOmitted() {
      String body =
          get(webTestClient, "/post-processing/advice-handler")
              .expectBody(String.class)
              .returnResult()
              .getResponseBody();

      assertThat(jsonMapper.readTree(body).has("type")).isFalse();
    }

    @Test
    void givenProblemDetailFromAdvice_whenRequesting_thenOwnPropertiesRetained() {
      String body =
          get(webTestClient, "/post-processing/advice-handler")
              .expectBody(String.class)
              .returnResult()
              .getResponseBody();

      Problem problem = jsonMapper.readValue(body, Problem.class);
      assertThat(problem.getExtensions())
          .containsEntry("origin", "advice")
          .containsEntry("traceId", TRACE_ID);
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}",
        "problem4j.extension-overrides.traceId={context.traceId}",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.webflux.result-handler.enabled=false"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithResultHandlerDisabled {

    @Autowired private WebTestClient webTestClient;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenReturnedProblemDetail_whenRequesting_thenNotPostProcessed() {
      String body =
          get(webTestClient, "/post-processing/advice-handler")
              .expectBody(String.class)
              .returnResult()
              .getResponseBody();

      Problem problem = jsonMapper.readValue(body, Problem.class);
      assertThat(problem.getInstance())
          .isNotEqualTo(URI.create("https://example.org/trace/" + TRACE_ID));
      assertThat(problem.getExtensions()).doesNotContainKey("traceId");
    }
  }
}
