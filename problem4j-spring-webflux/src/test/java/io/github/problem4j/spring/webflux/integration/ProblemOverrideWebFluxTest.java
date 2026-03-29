/*
 * Copyright (c) 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.webflux.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    properties = {
      "problem4j.type-override=https://example.org/type/{problem.type}",
      "problem4j.instance-override=https://example.org/trace/{context.traceId}",
      "problem4j.tracing-header-name=X-Trace-Id",
      "problem4j.detail-format=lowercase"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProblemOverrideWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void givenNonEmptyType_shouldNotRewriteType() {
    webTestClient
        .post()
        .uri("/problem-override/type-not-blank")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{}")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(notNullValue())
        .value(
            problem ->
                assertThat(problem.getType())
                    .isEqualTo(URI.create("https://example.org/type/not-blank")));
  }

  @Test
  void givenEmptyType_shouldNotRewriteType() {
    webTestClient
        .post()
        .uri("/problem-override/instance-override")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{}")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .value(notNullValue())
        .value(problem -> assertThat(problem.getType()).isEqualTo(Problem.BLANK_TYPE));
  }

  @Test
  void givenNonEmptyTraceId_shouldRewriteInstanceField() {
    String traceId = "12345-trace";
    webTestClient
        .post()
        .uri("/problem-override/instance-override")
        .header("X-Trace-Id", traceId)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{}")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectHeader()
        .value("X-Trace-Id", v -> assertThat(v).isEqualTo(traceId))
        .expectBody(Problem.class)
        .value(notNullValue())
        .value(
            problem ->
                assertThat(problem.getInstance())
                    .isEqualTo(URI.create("https://example.org/trace/" + traceId)));
  }
}
