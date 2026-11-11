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

package io.github.problem4j.spring.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ServerHttpObservationFilter;

class ObservationErrorWebMvcInspectorTest {

  @ParameterizedTest
  @ValueSource(ints = {500, 503})
  void givenServerErrorStatus_whenInspecting_thenExceptionIsRecorded(int status) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    ServerRequestObservationContext observationContext = withObservationContext(request);
    Exception ex = new IllegalStateException("boom");

    inspect(new ObservationErrorWebMvcInspector(), ex, status, request);

    assertThat(observationContext.getError()).isSameAs(ex);
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 404, 422})
  void givenClientErrorStatus_whenInspectingWithDefaults_thenExceptionIsNotRecorded(int status) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    ServerRequestObservationContext observationContext = withObservationContext(request);

    inspect(new ObservationErrorWebMvcInspector(), new IllegalStateException(), status, request);

    assertThat(observationContext.getError()).isNull();
  }

  @Test
  void givenCustomStatusFilter_whenStatusMatches_thenExceptionIsRecorded() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    ServerRequestObservationContext observationContext = withObservationContext(request);
    Exception ex = new IllegalArgumentException();

    inspect(new ObservationErrorWebMvcInspector(status -> true), ex, 400, request);

    assertThat(observationContext.getError()).isSameAs(ex);
  }

  @Test
  void givenErrorAlreadyRecorded_whenInspecting_thenErrorIsNotReplaced() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    ServerRequestObservationContext observationContext = withObservationContext(request);
    Exception existing = new IllegalStateException("existing");
    observationContext.setError(existing);

    inspect(new ObservationErrorWebMvcInspector(), new IllegalStateException(), 500, request);

    assertThat(observationContext.getError()).isSameAs(existing);
  }

  @Test
  void givenNoObservationContext_whenInspecting_thenNothingHappens() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    assertThatNoException()
        .isThrownBy(() ->
            inspect(
                new ObservationErrorWebMvcInspector(), new IllegalStateException(), 500, request));
  }

  private static ServerRequestObservationContext withObservationContext(
      MockHttpServletRequest request) {
    ServerRequestObservationContext observationContext =
        new ServerRequestObservationContext(request, new MockHttpServletResponse());
    request.setAttribute(
        ServerHttpObservationFilter.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE, observationContext);
    return observationContext;
  }

  private static void inspect(
      ObservationErrorWebMvcInspector inspector,
      Exception ex,
      int status,
      MockHttpServletRequest request) {
    inspector.inspect(
        ProblemContext.create(),
        Problem.builder().status(status).build(),
        ex,
        new HttpHeaders(),
        HttpStatusCode.valueOf(status),
        new ServletWebRequest(request));
  }
}
