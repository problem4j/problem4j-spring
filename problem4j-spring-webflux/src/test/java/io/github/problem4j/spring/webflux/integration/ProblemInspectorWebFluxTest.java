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
import io.github.problem4j.spring.webflux.ProblemInspector;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.problem.ResolvableException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class, ProblemInspectorWebFluxTest.InspectorConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProblemInspectorWebFluxTest {

  @Autowired private WebTestClient webTestClient;
  @Autowired private List<Inspected> inspected;

  @BeforeEach
  void beforeEach() {
    inspected.clear();
  }

  @Test
  void givenProblemInspectorBean_whenExceptionRendered_thenInspectorIsInvoked() {
    webTestClient
        .get()
        .uri("/problem-advice/resolvable")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

    assertThat(inspected)
        .singleElement()
        .satisfies(it -> {
          assertThat(it.problem().getStatus()).isEqualTo(422);
          assertThat(it.ex()).isInstanceOf(ResolvableException.class);
          assertThat(it.status()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        });
  }

  record Inspected(Problem problem, Exception ex, HttpStatus status) {}

  @TestConfiguration(proxyBeanMethods = false)
  static class InspectorConfiguration {

    @Bean
    List<Inspected> inspected() {
      return new CopyOnWriteArrayList<>();
    }

    @Bean
    ProblemInspector recordingProblemInspector(List<Inspected> inspected) {
      return (context, problem, ex, headers, status, exchange) ->
          inspected.add(new Inspected(problem, ex, HttpStatus.valueOf(status.value())));
    }
  }
}
