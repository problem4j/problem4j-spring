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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemGsonAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@RestClientTest(RestClientTestSliceTest.SliceClient.class)
class RestClientTestSliceTest {

  @Autowired private ApplicationContext context;
  @Autowired private SliceClient client;
  @Autowired private MockRestServiceServer server;

  @Test
  void givenClientTestSlice_whenContextStarts_thenProblemAutoConfigurationsAreApplied() {
    assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemMapper.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemGsonAutoConfiguration.class)).hasSize(1);
  }

  @Test
  void givenProblemResponse_whenReadingBody_thenProblemIsDeserialized() {
    server
        .expect(requestTo("/orders/1"))
        .andRespond(
            withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.parseMediaType(Problem.CONTENT_TYPE_JSON))
                .body("{\"title\":\"Not Found\",\"status\":404,\"orderId\":\"1\"}"));

    assertThat(client.getOrder("1"))
        .isEqualTo(
            Problem.builder().title("Not Found").status(404).extension("orderId", "1").build());
  }

  @SpringBootConfiguration
  @Import(SliceClient.class)
  static class SliceConfiguration {}

  @Component
  static class SliceClient {

    private final RestClient restClient;

    SliceClient(RestClient.Builder builder) {
      this.restClient = builder.build();
    }

    Problem getOrder(String id) {
      return restClient
          .get()
          .uri("/orders/{id}", id)
          .exchange((request, response) -> response.bodyTo(Problem.class));
    }
  }
}
