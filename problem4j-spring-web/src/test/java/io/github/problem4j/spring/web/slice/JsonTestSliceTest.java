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

import com.google.gson.Gson;
import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemGsonAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.ApplicationContext;
import tools.jackson.databind.json.JsonMapper;

@JsonTest
class JsonTestSliceTest {

  private static final Problem PROBLEM =
      Problem.builder()
          .title("Conflict")
          .status(409)
          .detail("slice conflict")
          .extension("orderId", "42")
          .build();

  private static final String JSON =
      "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"slice conflict\",\"orderId\":\"42\"}";

  @Autowired private ApplicationContext context;
  @Autowired private JsonMapper jsonMapper;
  @Autowired private Gson gson;

  @Test
  void givenJsonTestSlice_whenContextStarts_thenProblemAutoConfigurationsAreApplied() {
    assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemMapper.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemGsonAutoConfiguration.class)).hasSize(1);
  }

  @Test
  void givenJsonMapper_whenSerializingProblem_thenProblemJsonIsWritten() {
    assertThat(jsonMapper.writeValueAsString(PROBLEM)).isEqualTo(JSON);
    assertThat(jsonMapper.readValue(JSON, Problem.class)).isEqualTo(PROBLEM);
  }

  @Test
  void givenGson_whenSerializingProblem_thenProblemJsonIsWritten() {
    assertThat(gson.toJson(PROBLEM, Problem.class)).isEqualTo(JSON);
    assertThat(gson.fromJson(JSON, Problem.class)).isEqualTo(PROBLEM);
  }

  @SpringBootConfiguration
  static class SliceConfiguration {}
}
