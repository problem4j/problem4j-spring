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

package io.github.problem4j.spring.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.web.app.TestApp;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = {TestApp.class})
class ProblemJsonMapperBuilderCustomizerTest {

  @SpringBootTest(classes = {TestApp.class})
  @Nested
  class WithEnabled {

    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenProblem_whenSerializing_thenWritesProblemMembers() {
      Problem problem =
          Problem.builder()
              .type("https://example.org/errors/not-found")
              .title("Not Found")
              .status(404)
              .detail("resource not found")
              .instance("/orders/1")
              .extension("orderId", "1")
              .build();

      JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(problem));

      assertThat(json.get("type").asString()).isEqualTo("https://example.org/errors/not-found");
      assertThat(json.get("title").asString()).isEqualTo("Not Found");
      assertThat(json.get("status").asInt()).isEqualTo(404);
      assertThat(json.get("detail").asString()).isEqualTo("resource not found");
      assertThat(json.get("instance").asString()).isEqualTo("/orders/1");
      assertThat(json.get("orderId").asString()).isEqualTo("1");
    }

    @Test
    void givenProblemWithBlankType_whenSerializing_thenOmitsType() {
      Problem problem = Problem.builder().title("Bad Request").status(400).build();

      JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(problem));

      assertThat(json.has("type")).isFalse();
      assertThat(json.has("detail")).isFalse();
      assertThat(json.has("instance")).isFalse();
    }

    @Test
    void givenProblemJson_whenDeserializing_thenReadsProblem() {
      String json =
          """
          {
            "type": "https://example.org/errors/not-found",
            "title": "Not Found",
            "status": 404,
            "detail": "resource not found",
            "instance": "/orders/1",
            "orderId": "1"
          }
          """;

      Problem problem = jsonMapper.readValue(json, Problem.class);

      assertThat(problem)
          .isEqualTo(
              Problem.builder()
                  .type("https://example.org/errors/not-found")
                  .title("Not Found")
                  .status(404)
                  .detail("resource not found")
                  .instance("/orders/1")
                  .extension("orderId", "1")
                  .build());
    }

    @Test
    void givenProblem_whenRoundTripping_thenProblemIsEqual() {
      Problem problem =
          Problem.builder()
              .type("https://example.org/errors/conflict")
              .title("Conflict")
              .status(409)
              .extension("reason", "duplicate")
              .build();

      Problem result = jsonMapper.readValue(jsonMapper.writeValueAsString(problem), Problem.class);

      assertThat(result).isEqualTo(problem);
    }

    @Test
    void givenNonProblemType_whenSerializing_thenUsesDefaultSerializer() {
      String json = jsonMapper.writeValueAsString(Map.of("key", "value"));

      assertThat(json).isEqualTo("{\"key\":\"value\"}");
    }
  }

  @SpringBootTest(
      classes = {TestApp.class},
      properties = {"spring.jackson.find-and-add-modules=false"})
  @Nested
  class WithModuleDiscoveryDisabled {

    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenModuleDiscoveryDisabled_whenRoundTripping_thenCustomizerAloneHandlesProblem() {
      Problem problem =
          Problem.builder()
              .type("https://example.org/errors/conflict")
              .title("Conflict")
              .status(409)
              .extension("reason", "duplicate")
              .build();

      Problem result = jsonMapper.readValue(jsonMapper.writeValueAsString(problem), Problem.class);

      assertThat(result).isEqualTo(problem);
    }
  }

  // problem4j-jackson3 also registers its module via ServiceLoader, which Spring Boot picks up
  // unless module discovery is disabled
  @SpringBootTest(
      classes = {TestApp.class},
      properties = {"problem4j.enabled=false", "spring.jackson.find-and-add-modules=false"})
  @Nested
  class WithDisabled {

    @Autowired private JsonMapper jsonMapper;

    @Test
    void givenProblem4jDisabled_whenDeserializingProblem_thenThrows() {
      assertThatThrownBy(() -> jsonMapper.readValue("{\"status\":400}", Problem.class))
          .isInstanceOf(RuntimeException.class);
    }
  }
}
