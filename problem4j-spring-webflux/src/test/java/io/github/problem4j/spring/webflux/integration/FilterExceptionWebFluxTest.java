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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.exception-handling.mode=cooperate",
      "problem4j.tracing-source=simple",
      "problem4j.tracing-header-name=X-Trace-Id",
      "problem4j.extension-overrides.traceId={context.traceId}"
    })
@AutoConfigureWebTestClient
class FilterExceptionWebFluxTest {

  private static final String TRACE_ID = "filter-trace";

  @Autowired private WebTestClient webTestClient;
  @Autowired private JsonMapper jsonMapper;

  @ParameterizedTest
  @CsvSource({
    "/filter-error/resolvable, 422",
    "/filter-error/problem, 409",
    "/filter-error/unknown, 500"
  })
  void givenExceptionFromWebFilter_whenRequesting_thenTranslatedWithTraceId(
      String path, int status) {
    String body =
        webTestClient
            .get()
            .uri(path)
            .header("X-Trace-Id", TRACE_ID)
            .exchange()
            .expectStatus()
            .isEqualTo(status)
            .expectHeader()
            .contentType(Problem.CONTENT_TYPE_JSON)
            .expectHeader()
            .valueEquals("X-Trace-Id", TRACE_ID)
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

    Problem problem = jsonMapper.readValue(body, Problem.class);
    assertThat(problem.getStatus()).isEqualTo(status);
    assertThat(problem.getExtensions()).containsEntry("traceId", TRACE_ID);
  }
}
