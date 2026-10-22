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
import io.github.problem4j.spring.webflux.app.problem.AnnotatedException;
import io.github.problem4j.spring.webflux.app.problem.ResolvableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureWebTestClient
@Import(FilterExceptionWebFluxTest.ThrowingFilterConfiguration.class)
class FilterExceptionWebFluxTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void givenResolvableExceptionThrownFromFilter_whenRequesting_thenResolverIsUsed() {
    Problem problem =
        webTestClient
            .get()
            .uri("/filter-exception/resolvable")
            .exchange()
            .expectStatus()
            .isEqualTo(422)
            .expectHeader()
            .contentType(Problem.CONTENT_TYPE_JSON)
            .expectBody(Problem.class)
            .returnResult()
            .getResponseBody();

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .type("http://exception.example.org/resolvable")
                .title(ResolvableException.class.getSimpleName())
                .status(422)
                .extension("package", ResolvableException.class.getPackageName())
                .build());
  }

  @Test
  void givenProblemMappingExceptionThrownFromFilter_whenRequesting_thenMappingIsUsed() {
    Problem problem =
        webTestClient
            .get()
            .uri("/filter-exception/annotated")
            .exchange()
            .expectStatus()
            .isEqualTo(418)
            .expectBody(Problem.class)
            .returnResult()
            .getResponseBody();

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .type("https://example.org/annotated/abc")
                .title("Annotated Exception")
                .status(418)
                .detail("value2:123")
                .instance("https://example.org/annotated/instance/true")
                .build());
  }

  @Test
  void givenUnknownExceptionThrownFromFilter_whenRequesting_thenInternalServerErrorIsReturned() {
    Problem problem =
        webTestClient
            .get()
            .uri("/filter-exception/unknown")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(Problem.class)
            .returnResult()
            .getResponseBody();

    assertThat(problem).isEqualTo(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @TestConfiguration
  static class ThrowingFilterConfiguration {

    @Bean
    WebFilter throwingFilter() {
      return (exchange, chain) ->
          switch (exchange.getRequest().getPath().value()) {
            case "/filter-exception/resolvable" -> Mono.error(new ResolvableException());
            case "/filter-exception/annotated" ->
                Mono.error(new AnnotatedException("abc", 123L, true));
            case "/filter-exception/unknown" -> Mono.error(new IllegalStateException("unknown"));
            default -> chain.filter(exchange);
          };
    }
  }
}
