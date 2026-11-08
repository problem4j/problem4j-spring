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

package io.github.problem4j.spring.webflux.slice;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemWebAutoConfiguration;
import io.github.problem4j.spring.webflux.autoconfigure.ProblemWebFluxAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@WebFluxTest(
    properties = {
      "problem4j.tracing-header-name=X-Trace-Id",
      "problem4j.instance-override=https://example.org/trace/{context.traceId}"
    })
class WebFluxTestSliceTest {

  @Autowired private WebTestClient webTestClient;
  @Autowired private ApplicationContext context;

  @Test
  void givenWebFluxTestSlice_whenContextStarts_thenProblemAutoConfigurationsAreApplied() {
    assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemWebAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemWebFluxAutoConfiguration.class)).hasSize(1);
  }

  @Test
  void givenProblemException_whenRequesting_thenProblemResponseIsReturned() {
    webTestClient
        .get()
        .uri("/slice/problem")
        .header("X-Trace-Id", "slice-trace")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE_JSON)
        .expectHeader()
        .valueEquals("X-Trace-Id", "slice-trace")
        .expectBody()
        .json(
            """
            {
              "title": "Conflict",
              "status": 409,
              "detail": "slice conflict",
              "instance": "https://example.org/trace/slice-trace"
            }
            """,
            JsonCompareMode.STRICT);
  }

  @Test
  void givenUnknownPath_whenRequesting_thenProblemResponseIsReturned() {
    webTestClient
        .get()
        .uri("/slice/unknown")
        .header("X-Trace-Id", "slice-trace")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE_JSON)
        .expectBody(Problem.class)
        .isEqualTo(
            Problem.builder()
                .title("Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .instance("https://example.org/trace/slice-trace")
                .build());
  }

  @SpringBootConfiguration
  @Import(SliceController.class)
  static class SliceConfiguration {}

  @RestController
  static class SliceController {

    @GetMapping("/slice/problem")
    Mono<String> problem() {
      return Mono.error(
          new ProblemException(
              Problem.builder().title("Conflict").status(409).detail("slice conflict").build()));
    }
  }
}
