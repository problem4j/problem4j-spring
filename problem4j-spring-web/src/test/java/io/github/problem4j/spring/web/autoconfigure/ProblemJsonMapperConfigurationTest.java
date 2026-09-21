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

package io.github.problem4j.spring.web.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.web.ProblemJsonMapperBuilderCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

class ProblemJsonMapperConfigurationTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  JacksonAutoConfiguration.class, ProblemJacksonAutoConfiguration.class));

  @Test
  void givenJsonMapperOnClasspath_whenContextStarts_thenRegistersCustomizer() {
    contextRunner.run(
        context -> assertThat(context).hasSingleBean(ProblemJsonMapperBuilderCustomizer.class));
  }

  @Test
  void givenJsonMapperOnClasspath_whenContextStarts_thenJsonMapperSerializesProblem() {
    contextRunner.run(
        context -> {
          JsonMapper jsonMapper = context.getBean(JsonMapper.class);
          Problem problem = Problem.builder().title("Bad Request").status(400).build();

          Problem result =
              jsonMapper.readValue(jsonMapper.writeValueAsString(problem), Problem.class);

          assertThat(result).isEqualTo(problem);
        });
  }

  @Test
  void givenUserDefinedCustomizer_whenContextStarts_thenBacksOff() {
    contextRunner
        .withUserConfiguration(CustomCustomizerConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(ProblemJsonMapperBuilderCustomizer.class);
              assertThat(context.getBean(ProblemJsonMapperBuilderCustomizer.class))
                  .isSameAs(context.getBean("customProblemJsonMapperBuilderCustomizer"));
            });
  }

  @Test
  void givenJsonMapperNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(JsonMapper.class))
        .run(
            context ->
                assertThat(context).doesNotHaveBean(ProblemJsonMapperBuilderCustomizer.class));
  }

  @Test
  void givenProblem4jDisabled_whenContextStarts_thenDoesNotRegisterCustomizer() {
    contextRunner
        .withPropertyValues("problem4j.enabled=false")
        .run(
            context ->
                assertThat(context).doesNotHaveBean(ProblemJsonMapperBuilderCustomizer.class));
  }

  @Test
  void givenNonWebApplication_whenContextStarts_thenRegistersCustomizer() {
    new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                JacksonAutoConfiguration.class, ProblemJacksonAutoConfiguration.class))
        .run(
            context -> assertThat(context).hasSingleBean(ProblemJsonMapperBuilderCustomizer.class));
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomCustomizerConfiguration {

    @Bean
    ProblemJsonMapperBuilderCustomizer customProblemJsonMapperBuilderCustomizer() {
      return new ProblemJsonMapperBuilderCustomizer();
    }
  }
}
