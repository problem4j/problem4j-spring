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

import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import io.github.problem4j.spring.webmvc.app.problem.ResolvableException;
import io.github.problem4j.spring.webmvc.app.problem.UnresolvableException;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.observation.ServerRequestObservationContext;

class ObservationErrorWebMvcTest {

  private static final String SERVER_ERROR_PATH = "/problem-advice/unresolvable";
  private static final String CLIENT_ERROR_PATH = "/problem-advice/resolvable";

  @SpringBootTest(
      classes = {WebMvcTestApp.class, ObservationCaptureConfiguration.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  @AutoConfigureTestRestTemplate
  @Nested
  class WithDefaultRecording {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObservationCapture capture;

    @BeforeEach
    void beforeEach() {
      capture.clear();
    }

    @Test
    void givenServerError_whenRequesting_thenExceptionIsRecordedInObservation() {
      ResponseEntity<String> response = restTemplate.getForEntity(SERVER_ERROR_PATH, String.class);

      assertThat(response.getStatusCode().value()).isEqualTo(500);

      ServerRequestObservationContext context = capture.await(SERVER_ERROR_PATH);
      assertThat(context.getError()).isInstanceOf(UnresolvableException.class);
      assertThat(context.getLowCardinalityKeyValue("exception").getValue())
          .isEqualTo(UnresolvableException.class.getSimpleName());
    }

    @Test
    void givenClientError_whenRequesting_thenExceptionIsNotRecordedInObservation() {
      ResponseEntity<String> response = restTemplate.getForEntity(CLIENT_ERROR_PATH, String.class);

      assertThat(response.getStatusCode().value()).isEqualTo(422);

      ServerRequestObservationContext context = capture.await(CLIENT_ERROR_PATH);
      assertThat(context.getError()).isNull();
      assertThat(context.getLowCardinalityKeyValue("exception").getValue()).isEqualTo("none");
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class, ObservationCaptureConfiguration.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {"problem4j.observation-errors=all"})
  @AutoConfigureTestRestTemplate
  @Nested
  class WithAllRecording {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObservationCapture capture;

    @BeforeEach
    void beforeEach() {
      capture.clear();
    }

    @Test
    void givenClientError_whenRequesting_thenExceptionIsRecordedInObservation() {
      ResponseEntity<String> response = restTemplate.getForEntity(CLIENT_ERROR_PATH, String.class);

      assertThat(response.getStatusCode().value()).isEqualTo(422);

      ServerRequestObservationContext context = capture.await(CLIENT_ERROR_PATH);
      assertThat(context.getError()).isInstanceOf(ResolvableException.class);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class, ObservationCaptureConfiguration.class},
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = {"problem4j.observation-errors=none"})
  @AutoConfigureTestRestTemplate
  @Nested
  class WithNoRecording {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObservationCapture capture;

    @BeforeEach
    void beforeEach() {
      capture.clear();
    }

    @Test
    void givenServerError_whenRequesting_thenExceptionIsNotRecordedInObservation() {
      ResponseEntity<String> response = restTemplate.getForEntity(SERVER_ERROR_PATH, String.class);

      assertThat(response.getStatusCode().value()).isEqualTo(500);

      ServerRequestObservationContext context = capture.await(SERVER_ERROR_PATH);
      assertThat(context.getError()).isNull();
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class ObservationCaptureConfiguration {

    @Bean
    ObservationCapture observationCapture() {
      return new ObservationCapture();
    }
  }

  static class ObservationCapture implements ObservationHandler<ServerRequestObservationContext> {

    private final BlockingQueue<StoppedObservation> stopped = new LinkedBlockingQueue<>();

    // request URI must be read on stop, as the servlet request is recycled once response completes
    @Override
    public void onStop(ServerRequestObservationContext context) {
      stopped.add(new StoppedObservation(context.getCarrier().getRequestURI(), context));
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
      return context instanceof ServerRequestObservationContext;
    }

    void clear() {
      stopped.clear();
    }

    ServerRequestObservationContext await(String path) {
      try {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
          StoppedObservation observation = stopped.poll(100, TimeUnit.MILLISECONDS);
          if (observation != null && path.equals(observation.path())) {
            return observation.context();
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      throw new AssertionError("No observation stopped for " + path);
    }
  }

  record StoppedObservation(String path, ServerRequestObservationContext context) {}
}
