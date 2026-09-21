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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.dataformat.xml.XmlMapper;

@SpringBootTest(classes = {TestApp.class})
class ProblemXmlMapperBuilderCustomizerTest {

  @SpringBootTest(classes = {TestApp.class})
  @Nested
  class WithEnabled {

    @Autowired private XmlMapper xmlMapper;

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

      String xml = xmlMapper.writeValueAsString(problem);

      assertThat(xml)
          .contains("<type>https://example.org/errors/not-found</type>")
          .contains("<title>Not Found</title>")
          .contains("<status>404</status>")
          .contains("<detail>resource not found</detail>")
          .contains("<instance>/orders/1</instance>")
          .contains("<orderId>1</orderId>");
    }

    @Test
    void givenProblemWithBlankType_whenSerializing_thenOmitsType() {
      Problem problem = Problem.builder().title("Bad Request").status(400).build();

      String xml = xmlMapper.writeValueAsString(problem);

      assertThat(xml)
          .doesNotContain("<type>")
          .doesNotContain("<detail>")
          .doesNotContain("<instance>");
    }

    @Test
    void givenProblem_whenRoundTripping_thenProblemIsEqual() {
      Problem problem =
          Problem.builder()
              .type("https://example.org/errors/conflict")
              .title("Conflict")
              .status(409)
              .detail("already exists")
              .extension("reason", "duplicate")
              .build();

      Problem result = xmlMapper.readValue(xmlMapper.writeValueAsString(problem), Problem.class);

      assertThat(result).isEqualTo(problem);
    }
  }

  @SpringBootTest(
      classes = {TestApp.class},
      properties = {"spring.jackson.find-and-add-modules=false"})
  @Nested
  class WithModuleDiscoveryDisabled {

    @Autowired private XmlMapper xmlMapper;

    @Test
    void givenModuleDiscoveryDisabled_whenRoundTripping_thenCustomizerAloneHandlesProblem() {
      Problem problem =
          Problem.builder()
              .type("https://example.org/errors/conflict")
              .title("Conflict")
              .status(409)
              .extension("reason", "duplicate")
              .build();

      Problem result = xmlMapper.readValue(xmlMapper.writeValueAsString(problem), Problem.class);

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

    @Autowired private XmlMapper xmlMapper;

    @Test
    void givenProblem4jDisabled_whenDeserializingProblem_thenThrows() {
      assertThatThrownBy(
              () -> xmlMapper.readValue("<problem><status>400</status></problem>", Problem.class))
          .isInstanceOf(RuntimeException.class);
    }
  }
}
