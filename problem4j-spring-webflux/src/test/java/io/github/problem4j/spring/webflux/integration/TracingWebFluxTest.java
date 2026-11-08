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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

class TracingWebFluxTest {

  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929b0e0e4736";
  private static final String PARENT_SPAN_ID = "00f067aa0ba902b7";
  private static final String TRACEPARENT = "00-" + TRACE_ID + "-" + PARENT_SPAN_ID + "-01";
  private static final String UNSAMPLED_TRACEPARENT =
      "00-" + TRACE_ID + "-" + PARENT_SPAN_ID + "-00";
  private static final String HEADER_TRACE_ID = "header-trace";

  private static final Pattern INSTANCE_PATTERN =
      Pattern.compile("https://example\\.org/trace/([0-9a-f]{32})/span/([0-9a-f]{16})");

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.tracing.source=micrometer",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}/span/{context.spanId}"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithMicrometerSource {

    @Autowired private WebTestClient webTestClient;

    @ParameterizedTest
    @CsvSource({"POST, /problem-override/instance-override, 400", "GET, /not-existing, 404"})
    void givenTraceparentHeader_whenRequesting_thenTraceIdOfIncomingTraceIsUsed(
        HttpMethod method, String path, int status) {
      EntityExchangeResult<Problem> result =
          exchange(webTestClient, method, path, TRACEPARENT, null);

      assertThat(result.getStatus().value()).isEqualTo(status);
      assertThat(result.getResponseHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);

      Matcher instance = instanceOf(result);
      assertThat(instance.group(1)).isEqualTo(TRACE_ID);
      assertThat(instance.group(2)).isNotEqualTo(PARENT_SPAN_ID);
    }

    @Test
    void givenUnsampledTraceparentHeader_whenRequesting_thenTraceIdOfIncomingTraceIsUsed() {
      EntityExchangeResult<Problem> result =
          exchange(
              webTestClient,
              HttpMethod.POST,
              "/problem-override/instance-override",
              UNSAMPLED_TRACEPARENT,
              null);

      Matcher instance = instanceOf(result);
      assertThat(instance.group(1)).isEqualTo(TRACE_ID);
      assertThat(instance.group(2)).isNotEqualTo(PARENT_SPAN_ID);
      assertThat(result.getResponseHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);
    }

    @Test
    void givenNoTraceparentHeader_whenRequesting_thenTraceIdOfNewTraceIsUsed() {
      EntityExchangeResult<Problem> first =
          exchange(
              webTestClient, HttpMethod.POST, "/problem-override/instance-override", null, null);
      EntityExchangeResult<Problem> second =
          exchange(
              webTestClient, HttpMethod.POST, "/problem-override/instance-override", null, null);

      String firstTraceId = instanceOf(first).group(1);
      String secondTraceId = instanceOf(second).group(1);

      assertThat(firstTraceId).isNotEqualTo(secondTraceId);
      assertThat(first.getResponseHeaders().getFirst("X-Trace-Id")).isEqualTo(firstTraceId);
      assertThat(second.getResponseHeaders().getFirst("X-Trace-Id")).isEqualTo(secondTraceId);
    }

    @Test
    void givenTracingAndTraceparentHeaders_whenRequesting_thenTraceIdOfSpanTakesPrecedence() {
      EntityExchangeResult<Problem> result =
          exchange(
              webTestClient,
              HttpMethod.POST,
              "/problem-override/instance-override",
              TRACEPARENT,
              HEADER_TRACE_ID);

      assertThat(instanceOf(result).group(1)).isEqualTo(TRACE_ID);
      assertThat(result.getResponseHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);
    }

    @Test
    void givenTracingHeaderOnly_whenRequesting_thenTraceIdOfNewTraceTakesPrecedence() {
      EntityExchangeResult<Problem> result =
          exchange(
              webTestClient,
              HttpMethod.POST,
              "/problem-override/instance-override",
              null,
              HEADER_TRACE_ID);

      String traceId = instanceOf(result).group(1);

      assertThat(traceId).isNotEqualTo(HEADER_TRACE_ID);
      assertThat(result.getResponseHeaders().getFirst("X-Trace-Id")).isEqualTo(traceId);
    }

    private Matcher instanceOf(EntityExchangeResult<Problem> result) {
      Problem problem = Objects.requireNonNull(result.getResponseBody());
      Matcher matcher = INSTANCE_PATTERN.matcher(String.valueOf(problem.getInstance()));
      assertThat(matcher.matches()).as("instance=%s", problem.getInstance()).isTrue();
      return matcher;
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.tracing.source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithSimpleSource {

    @Autowired private WebTestClient webTestClient;

    @Test
    void givenTracingAndTraceparentHeaders_whenRequesting_thenTraceIdOfTracingHeaderIsUsed() {
      EntityExchangeResult<Problem> result =
          exchange(
              webTestClient,
              HttpMethod.POST,
              "/problem-override/instance-override",
              TRACEPARENT,
              HEADER_TRACE_ID);

      Problem problem = Objects.requireNonNull(result.getResponseBody());

      assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(problem.getInstance()).hasToString("https://example.org/trace/" + HEADER_TRACE_ID);
      assertThat(result.getResponseHeaders().getFirst("X-Trace-Id")).isEqualTo(HEADER_TRACE_ID);
    }

    @Test
    void givenTraceparentHeaderOnly_whenRequesting_thenTraceIdOfIncomingTraceIsNotUsed() {
      EntityExchangeResult<Problem> result =
          exchange(
              webTestClient,
              HttpMethod.POST,
              "/problem-override/instance-override",
              TRACEPARENT,
              null);

      Problem problem = Objects.requireNonNull(result.getResponseBody());
      String traceId = result.getResponseHeaders().getFirst("X-Trace-Id");

      assertThat(traceId).isNotEqualTo(TRACE_ID).matches("[0-9a-f]{32}");
      assertThat(problem.getInstance()).hasToString("https://example.org/trace/" + traceId);
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithDefaultSource {

    @Autowired private WebTestClient webTestClient;

    @Test
    void givenTraceparentHeader_whenRequesting_thenTraceIdOfIncomingTraceIsUsed() {
      EntityExchangeResult<Problem> result =
          exchange(
              webTestClient,
              HttpMethod.POST,
              "/problem-override/instance-override",
              TRACEPARENT,
              HEADER_TRACE_ID);

      Problem problem = Objects.requireNonNull(result.getResponseBody());

      assertThat(problem.getInstance()).hasToString("https://example.org/trace/" + TRACE_ID);
      assertThat(result.getResponseHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);
    }
  }

  private static EntityExchangeResult<Problem> exchange(
      WebTestClient webTestClient,
      HttpMethod method,
      String path,
      String traceparent,
      String tracingHeader) {
    return webTestClient
        .method(method)
        .uri(path)
        .headers(headers -> {
          if (traceparent != null) {
            headers.set("traceparent", traceparent);
          }
          if (tracingHeader != null) {
            headers.set("X-Trace-Id", tracingHeader);
          }
        })
        .exchange()
        .expectBody(Problem.class)
        .returnResult();
  }
}
