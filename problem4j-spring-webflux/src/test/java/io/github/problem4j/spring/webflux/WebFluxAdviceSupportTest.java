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

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.webflux.WebFluxAdviceSupport.resolveContentType;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

class WebFluxAdviceSupportTest {

  @Test
  void givenAcceptXml_whenResolveContentTypeFromExchange_thenReturnsProblemXml() {
    ServerWebExchange exchange = exchange(MediaType.APPLICATION_XML);

    MediaType resolved = resolveContentType(exchange);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenNoAcceptHeader_whenResolveContentTypeFromExchange_thenReturnsProblemJson() {
    ServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

    MediaType resolved = resolveContentType(exchange);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptXml_whenResolveContentTypeFromServerRequest_thenReturnsProblemXml() {
    ServerRequest request = serverRequest(exchange(MediaType.APPLICATION_XML));

    MediaType resolved = resolveContentType(request);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenNoAcceptHeader_whenResolveContentTypeFromServerRequest_thenReturnsProblemJson() {
    ServerRequest request =
        serverRequest(MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()));

    MediaType resolved = resolveContentType(request);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  private static MockServerWebExchange exchange(MediaType accept) {
    return MockServerWebExchange.from(MockServerHttpRequest.get("/test").accept(accept).build());
  }

  private static ServerRequest serverRequest(ServerWebExchange exchange) {
    return ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
  }
}
