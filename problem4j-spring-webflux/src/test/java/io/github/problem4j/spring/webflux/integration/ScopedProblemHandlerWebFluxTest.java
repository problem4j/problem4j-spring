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

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ScopedProblemHandlerWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void givenExceptionFromControllerWithProblemHandler_whenRequesting_thenUsesControllerHandler() {
    webTestClient
        .get()
        .uri("/scoped-handler")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT)
        .expectBody(Problem.class)
        .isEqualTo(Problem.builder().title("Scoped").status(409).build());
  }

  @Test
  void givenExceptionFromOtherController_whenRequesting_thenIgnoresControllerHandler() {
    webTestClient
        .get()
        .uri("/unscoped-handler")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void
      givenFrameworkExceptionFromControllerWithProblemHandler_whenRequesting_thenUsesControllerHandler() {
    webTestClient
        .get()
        .uri("/scoped-handler/missing-param")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
        .expectBody(Problem.class)
        .isEqualTo(Problem.builder().title("Scoped missing parameter").status(422).build());
  }

  @Test
  void givenFrameworkExceptionFromOtherController_whenRequesting_thenUsesBuiltInResolver() {
    webTestClient
        .get()
        .uri("/unscoped-handler/missing-param")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
