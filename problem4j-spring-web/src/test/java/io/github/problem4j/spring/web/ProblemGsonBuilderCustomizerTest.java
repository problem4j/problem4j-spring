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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.web.app.TestApp;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TestApp.class})
class ProblemGsonBuilderCustomizerTest {

  @SpringBootTest(classes = {TestApp.class})
  @Nested
  class WithEnabled {

    @Autowired private Gson gson;

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

      JsonObject json =
          JsonParser.parseString(gson.toJson(problem, Problem.class)).getAsJsonObject();

      assertThat(json.get("type").getAsString()).isEqualTo("https://example.org/errors/not-found");
      assertThat(json.get("title").getAsString()).isEqualTo("Not Found");
      assertThat(json.get("status").getAsInt()).isEqualTo(404);
      assertThat(json.get("detail").getAsString()).isEqualTo("resource not found");
      assertThat(json.get("instance").getAsString()).isEqualTo("/orders/1");
      assertThat(json.get("orderId").getAsString()).isEqualTo("1");
    }

    @Test
    void givenProblemWithBlankType_whenSerializing_thenOmitsType() {
      Problem problem = Problem.builder().title("Bad Request").status(400).build();

      JsonObject json =
          JsonParser.parseString(gson.toJson(problem, Problem.class)).getAsJsonObject();

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

      Problem problem = gson.fromJson(json, Problem.class);

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

      Problem result = gson.fromJson(gson.toJson(problem, Problem.class), Problem.class);

      assertThat(result).isEqualTo(problem);
    }

    @Test
    void givenNonProblemType_whenSerializing_thenUsesDefaultAdapter() {
      String json = gson.toJson(Map.of("key", "value"));

      assertThat(json).isEqualTo("{\"key\":\"value\"}");
    }
  }

  @SpringBootTest(
      classes = {TestApp.class},
      properties = {"problem4j.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired private Gson gson;

    @Test
    void givenProblem4jDisabled_whenDeserializingProblem_thenThrows() {
      assertThatThrownBy(() -> gson.fromJson("{\"status\":400}", Problem.class))
          .isInstanceOf(RuntimeException.class);
    }
  }
}
