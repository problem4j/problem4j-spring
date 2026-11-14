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
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.DefaultProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice;
import io.github.problem4j.spring.webflux.ProblemInspector;
import io.github.problem4j.spring.webflux.ProblemResponseEntityExceptionHandler;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.problem.UnresolvableException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import tools.jackson.databind.json.JsonMapper;

class DeferredAdvicePostProcessingWebFluxTest {

  private static final String TRACE_ID = "deferred-trace";

  private static Problem get(WebTestClient webTestClient, JsonMapper jsonMapper, String path) {
    String body =
        webTestClient
            .get()
            .uri(path)
            .header("X-Trace-Id", TRACE_ID)
            .exchange()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();
    return jsonMapper.readValue(body, Problem.class);
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.title-override={problem.title} ({context.traceId})"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithDeferredMode {

    @Autowired private WebTestClient webTestClient;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenProblemFromWebFilter_whenRequesting_thenTitleIsOverriddenOnce() {
      Problem problem = get(webTestClient, jsonMapper, "/filter-error/problem");

      assertThat(problem.getTitle()).isEqualTo("Conflict (" + TRACE_ID + ")");
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class, InheritedExceptionAdvice.class, RecordingInspector.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.title-override={problem.title} ({context.traceId})"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithInheritedAdviceInDeferredMode {

    @Autowired private WebTestClient webTestClient;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private RecordingInspector recordingInspector;

    private List<String> inspected;

    @BeforeEach
    void beforeEach() {
      inspected = new CopyOnWriteArrayList<>();
      recordingInspector.recordTo(inspected);
    }

    @Test
    void givenProblemOfInheritedAdvice_whenRequesting_thenTitleIsOverriddenOnce() {
      Problem problem = get(webTestClient, jsonMapper, "/problem-advice/resolvable");

      assertThat(problem.getStatus()).isEqualTo(422);
      assertThat(problem.getTitle()).isEqualTo("ResolvableException (" + TRACE_ID + ")");
      assertThat(inspected)
          .containsExactly("422 ResolvableException (" + TRACE_ID + ") ResolvableException");
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class, InheritedExceptionAdvice.class, RecordingInspector.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.webflux.result-handler.enabled=false",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.title-override={problem.title} ({context.traceId})"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithoutPostProcessingOfReturnedProblems {

    @Autowired private WebTestClient webTestClient;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private RecordingInspector recordingInspector;

    private List<String> inspected;

    @BeforeEach
    void beforeEach() {
      inspected = new CopyOnWriteArrayList<>();
      recordingInspector.recordTo(inspected);
    }

    @Test
    void givenDisabledResponsePostProcessing_whenRequesting_thenInheritedAdvicePostProcessesOnce() {
      Problem problem = get(webTestClient, jsonMapper, "/problem-advice/resolvable");

      assertThat(problem.getStatus()).isEqualTo(422);
      assertThat(problem.getTitle()).isEqualTo("ResolvableException (" + TRACE_ID + ")");
      assertThat(inspected)
          .containsExactly("422 ResolvableException (" + TRACE_ID + ") ResolvableException");
    }
  }

  @SpringBootTest(
      classes = {
        WebFluxTestApp.class,
        InheritedResponseEntityExceptionHandler.class,
        RecordingInspector.class
      },
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {
        "problem4j.exception-handling.mode=deferred",
        "problem4j.tracing-source=simple",
        "problem4j.tracing-header-name=X-Trace-Id",
        "problem4j.title-override={problem.title} ({context.traceId})"
      })
  @AutoConfigureWebTestClient
  @Nested
  class WithResponseEntityExceptionHandlerInDeferredMode {

    @Autowired private WebTestClient webTestClient;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private RecordingInspector recordingInspector;

    private List<String> inspected;

    @BeforeEach
    void beforeEach() {
      inspected = new CopyOnWriteArrayList<>();
      recordingInspector.recordTo(inspected);
    }

    @Test
    void givenSpringException_whenRequesting_thenRenderedPostProcessedAndInspectedOnce() {
      Problem problem = get(webTestClient, jsonMapper, "/missing-parameter/request-param");

      assertThat(problem.getStatus()).isEqualTo(400);
      assertThat(problem.getTitle()).isEqualTo("Bad Request (" + TRACE_ID + ")");
      assertThat(inspected)
          .containsExactly("400 Bad Request (" + TRACE_ID + ") MissingRequestValueException");
    }

    @Test
    void givenOwnHandlerOfInheritedClass_whenRequesting_thenPostProcessedOnceAndNotInspected() {
      Problem problem = get(webTestClient, jsonMapper, "/problem-advice/unresolvable");

      assertThat(problem.getStatus()).isEqualTo(418);
      assertThat(problem.getTitle()).isEqualTo("Unresolvable (" + TRACE_ID + ")");
      assertThat(inspected).isEmpty();
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      properties = {"problem4j.exception-handling.mode=advice"})
  @Nested
  class WithAdviceMode {

    @Autowired private ProblemPostProcessor problemPostProcessor;

    @Test
    void givenAdviceMode_whenStarting_thenPostProcessorIsDefault() {
      assertThat(problemPostProcessor).isExactlyInstanceOf(DefaultProblemPostProcessor.class);
    }
  }

  /** Controller advice of the application, inheriting from Problem4J. */
  @RestControllerAdvice
  static class InheritedExceptionAdvice extends ExceptionWebFluxAdvice {

    InheritedExceptionAdvice(
        ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
      super(problemOperations, problemInspectors);
    }
  }

  /** Spring's exception handler of the application, inheriting from Problem4J. */
  @RestControllerAdvice
  static class InheritedResponseEntityExceptionHandler
      extends ProblemResponseEntityExceptionHandler {

    InheritedResponseEntityExceptionHandler(
        ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
      super(problemOperations, problemInspectors);
    }

    /** Own handler of the application, returning a problem not post-processed by Problem4J. */
    @ExceptionHandler
    Problem handleUnresolvable(UnresolvableException ex) {
      return Problem.of("Unresolvable", 418);
    }
  }

  /** Inspector recording inspected problems into the list of the current test. */
  static class RecordingInspector implements ProblemInspector {

    private List<String> inspected = new CopyOnWriteArrayList<>();

    void recordTo(List<String> inspected) {
      this.inspected = inspected;
    }

    @Override
    public void inspect(
        ProblemContext context,
        Problem problem,
        Exception ex,
        HttpHeaders headers,
        HttpStatusCode status,
        ServerWebExchange exchange) {
      inspected.add(
          status.value() + " " + problem.getTitle() + " " + ex.getClass().getSimpleName());
    }
  }
}
