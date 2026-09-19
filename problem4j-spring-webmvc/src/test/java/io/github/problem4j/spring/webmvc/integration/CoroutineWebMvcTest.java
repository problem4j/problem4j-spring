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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.instance-override=https://example.org/trace/{context.traceId}",
      "problem4j.tracing-header-name=X-Trace-Id"
    })
@AutoConfigureTestRestTemplate
class CoroutineWebMvcTest {

  private static final String TRACE_ID = "coroutine-trace";

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @ParameterizedTest
  @CsvSource({
    "/coroutine/problem-exception, coroutine conflict",
    "/coroutine/resolver, coroutine failure",
    "/coroutine/flow, flow failure"
  })
  void givenCoroutineThrowingAfterSuspension_whenRequesting_thenProblemIsReturned(
      String path, String detail) {
    ResponseEntity<String> response = exchange(path);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
    assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .type("https://example.org/coroutine")
                .title(HttpStatus.CONFLICT.getReasonPhrase())
                .status(HttpStatus.CONFLICT.value())
                .detail(detail)
                .instance("https://example.org/trace/" + TRACE_ID)
                .build());
  }

  @Test
  void
      givenCoroutineThrowingUnresolvableException_whenRequesting_thenInternalServerErrorProblemIsReturned() {
    ResponseEntity<String> response = exchange("/coroutine/unresolvable");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .instance("https://example.org/trace/" + TRACE_ID)
                .build());
  }

  private ResponseEntity<String> exchange(String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Trace-Id", TRACE_ID);
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }
}
