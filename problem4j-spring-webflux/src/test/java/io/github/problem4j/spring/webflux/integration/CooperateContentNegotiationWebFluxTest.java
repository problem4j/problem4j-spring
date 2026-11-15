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

import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.exception-handling.mode=cooperate",
      "problem4j.extension-overrides.processed=yes"
    })
@AutoConfigureWebTestClient
class CooperateContentNegotiationWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  private EntityExchangeResult<String> get(String path, String accept) {
    WebTestClient.RequestHeadersSpec<?> request = webTestClient.get().uri(path);
    if (!accept.isEmpty()) {
      request = request.header(HttpHeaders.ACCEPT, accept);
    }
    return request.exchange().expectBody(String.class).returnResult();
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
    "/filter-error/problem, text/html, 409"
  })
  void givenExceptionRenderedByProblem4J_whenRequestingWithAnyAccept_thenProblemJson(
      String path, String accept, int status) {
    EntityExchangeResult<String> result = get(path, accept);

    assertThat(result.getStatus().value()).isEqualTo(status);
    assertThat(result.getResponseHeaders().getContentType())
        .hasToString("application/problem+json");
    assertThat(result.getResponseBody()).contains("\"processed\":\"yes\"");
  }

  @ParameterizedTest
  @CsvSource({
    "/post-processing/problem, ''",
    "/post-processing/problem, */*",
    "/post-processing/problem, application/json",
    "/post-processing/problem, application/problem+json",
    "/post-processing/problem, application/xml",
    "/post-processing/problem, text/html",
    "/post-processing/problem-mono, ''",
    "/post-processing/problem-mono, text/html",
    "/post-processing/problem-detail, ''",
    "/post-processing/problem-detail, application/xml",
    "/post-processing/problem-detail, text/html"
  })
  void givenReturnedProblemOrProblemDetail_whenRequestingWithAnyAccept_thenProblemJson(
      String path, String accept) {
    EntityExchangeResult<String> result = get(path, accept);

    assertThat(result.getStatus().value()).isEqualTo(409);
    assertThat(result.getResponseHeaders().getContentType())
        .hasToString("application/problem+json");
    assertThat(result.getResponseBody()).contains("\"processed\":\"yes\"");
  }
}
