/*
 * Copyright 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.web.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.web.app.TestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TestApp.class})
class ProblemAutoConfigurationTest {

  @SpringBootTest(classes = {TestApp.class})
  @Nested
  class WithEnabled {

    @Autowired(required = false)
    private ProblemAutoConfiguration problemAutoConfiguration;

    @Autowired(required = false)
    private ProblemParameterConfiguration problemParameterConfiguration;

    @Autowired(required = false)
    private ProblemResolverConfiguration problemResolverConfiguration;

    @Autowired(required = false)
    private ProblemProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemAutoConfiguration).isNotNull();
      assertThat(problemParameterConfiguration).isNotNull();
      assertThat(problemResolverConfiguration).isNotNull();

      assertThat(properties).isNotNull();
      assertThat(properties.isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {TestApp.class},
      properties = {"problem4j.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired(required = false)
    private ProblemAutoConfiguration problemAutoConfiguration;

    @Autowired(required = false)
    private ProblemParameterConfiguration problemParameterConfiguration;

    @Autowired(required = false)
    private ProblemResolverConfiguration problemResolverConfiguration;

    @Autowired(required = false)
    private ProblemProperties properties;

    @Test
    void contextLoadsWithoutProblemConfiguration() {
      assertThat(problemAutoConfiguration).isNull();
      assertThat(problemParameterConfiguration).isNull();
      assertThat(problemResolverConfiguration).isNull();

      assertThat(properties).isNull();
    }
  }
}
