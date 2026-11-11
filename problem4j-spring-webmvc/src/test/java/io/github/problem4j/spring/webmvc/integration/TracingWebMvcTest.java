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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.json.JsonMapper;

class TracingWebMvcTest {

  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929b0e0e4736";
  private static final String PARENT_SPAN_ID = "00f067aa0ba902b7";
  private static final String TRACEPARENT = "00-" + TRACE_ID + "-" + PARENT_SPAN_ID + "-01";
  private static final String UNSAMPLED_TRACEPARENT =
      "00-" + TRACE_ID + "-" + PARENT_SPAN_ID + "-00";
  private static final String HEADER_TRACE_ID = "header-trace";

  private static final Pattern INSTANCE_PATTERN =
      Pattern.compile("https://example\\.org/trace/([0-9a-f]{32})/span/([0-9a-f]{16})");

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.tracing-source=micrometer",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}/span/{context.spanId}"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithMicrometerSource {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @ParameterizedTest
    @CsvSource({
      "POST, /problem-override/instance-override, 400",
      "GET, /send-error/internal-server-error, 500",
      "GET, /not-existing, 404"
    })
    void givenTraceparentHeader_whenRequesting_thenTraceIdOfIncomingTraceIsUsed(
        HttpMethod method, String path, int status) {
      ResponseEntity<String> response = exchange(method, path, TRACEPARENT, null);

      assertThat(response.getStatusCode().value()).isEqualTo(status);
      assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);

      Matcher instance = instanceOf(response);
      assertThat(instance.group(1)).isEqualTo(TRACE_ID);
      assertThat(instance.group(2)).isNotEqualTo(PARENT_SPAN_ID);
    }

    @Test
    void givenUnsampledTraceparentHeader_whenRequesting_thenTraceIdOfIncomingTraceIsUsed() {
      ResponseEntity<String> response =
          exchange(
              HttpMethod.POST, "/problem-override/instance-override", UNSAMPLED_TRACEPARENT, null);

      Matcher instance = instanceOf(response);
      assertThat(instance.group(1)).isEqualTo(TRACE_ID);
      assertThat(instance.group(2)).isNotEqualTo(PARENT_SPAN_ID);
      assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);
    }

    @Test
    void givenNoTraceparentHeader_whenRequesting_thenTraceIdOfNewTraceIsUsed() {
      ResponseEntity<String> first =
          exchange(HttpMethod.POST, "/problem-override/instance-override", null, null);
      ResponseEntity<String> second =
          exchange(HttpMethod.POST, "/problem-override/instance-override", null, null);

      String firstTraceId = instanceOf(first).group(1);
      String secondTraceId = instanceOf(second).group(1);

      assertThat(firstTraceId).isNotEqualTo(secondTraceId);
      assertThat(first.getHeaders().getFirst("X-Trace-Id")).isEqualTo(firstTraceId);
      assertThat(second.getHeaders().getFirst("X-Trace-Id")).isEqualTo(secondTraceId);
    }

    @Test
    void givenTracingAndTraceparentHeaders_whenRequesting_thenTraceIdOfSpanTakesPrecedence() {
      ResponseEntity<String> response =
          exchange(
              HttpMethod.POST, "/problem-override/instance-override", TRACEPARENT, HEADER_TRACE_ID);

      assertThat(instanceOf(response).group(1)).isEqualTo(TRACE_ID);
      assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);
    }

    @Test
    void givenTracingHeaderOnly_whenRequesting_thenTraceIdOfNewTraceTakesPrecedence() {
      ResponseEntity<String> response =
          exchange(HttpMethod.POST, "/problem-override/instance-override", null, HEADER_TRACE_ID);

      String traceId = instanceOf(response).group(1);

      assertThat(traceId).isNotEqualTo(HEADER_TRACE_ID);
      assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo(traceId);
    }

    private ResponseEntity<String> exchange(
        HttpMethod method, String path, String traceparent, String tracingHeader) {
      return TracingWebMvcTest.exchange(restTemplate, method, path, traceparent, tracingHeader);
    }

    private Matcher instanceOf(ResponseEntity<String> response) {
      Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
      Matcher matcher = INSTANCE_PATTERN.matcher(String.valueOf(problem.getInstance()));
      assertThat(matcher.matches()).as("instance=%s", problem.getInstance()).isTrue();
      return matcher;
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithSimpleSource {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenTracingAndTraceparentHeaders_whenRequesting_thenTraceIdOfTracingHeaderIsUsed() {
      ResponseEntity<String> response =
          TracingWebMvcTest.exchange(
              restTemplate,
              HttpMethod.POST,
              "/problem-override/instance-override",
              TRACEPARENT,
              HEADER_TRACE_ID);

      Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(problem.getInstance()).hasToString("https://example.org/trace/" + HEADER_TRACE_ID);
      assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo(HEADER_TRACE_ID);
    }

    @Test
    void givenTraceparentHeaderOnly_whenRequesting_thenTraceIdOfIncomingTraceIsNotUsed() {
      ResponseEntity<String> response =
          TracingWebMvcTest.exchange(
              restTemplate,
              HttpMethod.POST,
              "/problem-override/instance-override",
              TRACEPARENT,
              null);

      Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
      String traceId = response.getHeaders().getFirst("X-Trace-Id");

      assertThat(traceId).isNotEqualTo(TRACE_ID).matches("[0-9a-f]{32}");
      assertThat(problem.getInstance()).hasToString("https://example.org/trace/" + traceId);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.instance-override=https://example.org/trace/{context.traceId}"
      })
  @AutoConfigureTestRestTemplate
  @Nested
  class WithDefaultSource {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenTraceparentHeader_whenRequesting_thenTraceIdOfIncomingTraceIsUsed() {
      ResponseEntity<String> response =
          TracingWebMvcTest.exchange(
              restTemplate,
              HttpMethod.POST,
              "/problem-override/instance-override",
              TRACEPARENT,
              HEADER_TRACE_ID);

      Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

      assertThat(problem.getInstance()).hasToString("https://example.org/trace/" + TRACE_ID);
      assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo(TRACE_ID);
    }
  }

  private static ResponseEntity<String> exchange(
      TestRestTemplate restTemplate,
      HttpMethod method,
      String path,
      String traceparent,
      String tracingHeader) {
    HttpHeaders headers = new HttpHeaders();
    if (traceparent != null) {
      headers.set("traceparent", traceparent);
    }
    if (tracingHeader != null) {
      headers.set("X-Trace-Id", tracingHeader);
    }
    return restTemplate.exchange(path, method, new HttpEntity<>(headers), String.class);
  }
}
