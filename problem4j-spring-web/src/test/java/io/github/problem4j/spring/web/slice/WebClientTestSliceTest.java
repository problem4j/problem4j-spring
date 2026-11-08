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

package io.github.problem4j.spring.web.slice;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.web.autoconfigure.ProblemGsonAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webclient.test.autoconfigure.WebClientTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebClientTest
class WebClientTestSliceTest {

  @Autowired private ApplicationContext context;
  @Autowired private WebClient.Builder builder;

  @Test
  void givenClientTestSlice_whenContextStarts_thenProblemAutoConfigurationsAreApplied() {
    assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemGsonAutoConfiguration.class)).hasSize(1);
  }

  @Test
  void givenProblemResponse_whenReadingBody_thenProblemIsDeserialized() {
    ClientHttpConnector connector =
        (method, uri, requestCallback) ->
            requestCallback
                .apply(new MockClientHttpRequest(method, uri))
                .then(Mono.fromSupplier(WebClientTestSliceTest::problemResponse));

    Problem problem =
        builder
            .clientConnector(connector)
            .build()
            .get()
            .uri("http://localhost/orders/1")
            .exchangeToMono(response -> response.bodyToMono(Problem.class))
            .block();

    assertThat(problem)
        .isEqualTo(
            Problem.builder().title("Not Found").status(404).extension("orderId", "1").build());
  }

  private static MockClientHttpResponse problemResponse() {
    MockClientHttpResponse response = new MockClientHttpResponse(HttpStatus.NOT_FOUND);
    response.getHeaders().set(HttpHeaders.CONTENT_TYPE, Problem.CONTENT_TYPE_JSON);
    response.setBody(
        Flux.just(
            DefaultDataBufferFactory.sharedInstance.wrap(
                "{\"title\":\"Not Found\",\"status\":404,\"orderId\":\"1\"}"
                    .getBytes(StandardCharsets.UTF_8))));
    return response;
  }

  @SpringBootConfiguration
  static class SliceConfiguration {}
}
