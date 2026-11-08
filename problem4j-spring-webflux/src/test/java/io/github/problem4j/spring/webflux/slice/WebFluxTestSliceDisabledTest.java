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

import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemWebAutoConfiguration;
import io.github.problem4j.spring.webflux.autoconfigure.ProblemWebFluxAutoConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = WebFluxTestSliceDisabledTest.SliceConfiguration.class)
class WebFluxTestSliceDisabledTest {

  private static void assertUnknownPathIsNotProblemResponse(WebTestClient webTestClient) {
    webTestClient
        .get()
        .uri("/slice/unknown")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON);
  }

  @Nested
  @TestPropertySource(properties = {"problem4j.enabled=false"})
  class Problem4jDisabled {

    @Autowired private WebTestClient webTestClient;
    @Autowired private ApplicationContext context;

    @Test
    void givenProblem4jDisabled_whenContextStarts_thenProblemAutoConfigurationsAreNotApplied() {
      assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).isEmpty();
      assertThat(context.getBeansOfType(ProblemWebAutoConfiguration.class)).isEmpty();
      assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).isEmpty();
      assertThat(context.getBeansOfType(ProblemWebFluxAutoConfiguration.class)).isEmpty();
    }

    @Test
    void givenProblem4jDisabled_whenRequestingUnknownPath_thenProblemResponseIsNotReturned() {
      assertUnknownPathIsNotProblemResponse(webTestClient);
    }
  }

  @Nested
  @TestPropertySource(properties = {"problem4j.webflux.enabled=false"})
  class WebFluxDisabled {

    @Autowired private WebTestClient webTestClient;
    @Autowired private ApplicationContext context;

    @Test
    void givenWebFluxDisabled_whenContextStarts_thenOnlyWebFluxAutoConfigurationIsNotApplied() {
      assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).hasSize(1);
      assertThat(context.getBeansOfType(ProblemWebAutoConfiguration.class)).hasSize(1);
      assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).hasSize(1);
      assertThat(context.getBeansOfType(ProblemWebFluxAutoConfiguration.class)).isEmpty();
    }

    @Test
    void givenWebFluxDisabled_whenRequestingUnknownPath_thenProblemResponseIsNotReturned() {
      assertUnknownPathIsNotProblemResponse(webTestClient);
    }
  }

  @SpringBootConfiguration
  static class SliceConfiguration {}
}
