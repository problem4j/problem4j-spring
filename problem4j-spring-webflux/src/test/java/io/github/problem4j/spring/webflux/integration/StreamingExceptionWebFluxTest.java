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

import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.problem.ResolvableException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(
    classes = {WebFluxTestApp.class, StreamingExceptionWebFluxTest.StreamingController.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.exception-handling.mode=fallback",
      "problem4j.extension-overrides.processed=yes"
    })
@AutoConfigureWebTestClient
class StreamingExceptionWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @ParameterizedTest
  @ValueSource(strings = {"/streaming-exception/delayed-error", "/streaming-exception/flux-error"})
  void givenErrorBeforeResponseIsCommitted_whenRequesting_thenRenderedAsPostProcessedProblem(
      String path) {
    EntityExchangeResult<String> result =
        webTestClient.get().uri(path).exchange().expectBody(String.class).returnResult();

    assertThat(result.getStatus().value()).isEqualTo(422);
    assertThat(result.getResponseHeaders().getContentType())
        .hasToString("application/problem+json");
    assertThat(result.getResponseBody()).contains("\"processed\":\"yes\"");
  }

  @Test
  void givenErrorAfterResponseIsCommitted_whenRequesting_thenStreamIsAbortedWithoutProblem() {
    FluxExchangeResult<String> result =
        webTestClient
            .get()
            .uri("/streaming-exception/event-stream")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class);

    StepVerifier.create(result.getResponseBody())
        .expectNext("partial")
        .expectError()
        .verify(Duration.ofSeconds(5));
  }

  @RestController
  static class StreamingController {

    @GetMapping("/streaming-exception/delayed-error")
    Mono<String> delayedError() {
      return Mono.delay(Duration.ofMillis(20)).then(Mono.error(new ResolvableException()));
    }

    @GetMapping("/streaming-exception/flux-error")
    Flux<String> fluxError() {
      return Flux.error(new ResolvableException());
    }

    @GetMapping(
        path = "/streaming-exception/event-stream",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> eventStream() {
      return Flux.concat(
          Flux.just("partial"),
          Mono.delay(Duration.ofMillis(20)).then(Mono.error(new ResolvableException())));
    }
  }
}
