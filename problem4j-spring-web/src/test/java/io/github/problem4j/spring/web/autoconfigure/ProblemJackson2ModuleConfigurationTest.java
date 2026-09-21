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

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.problem4j.core.Problem;
import io.github.problem4j.jackson2.ProblemModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class ProblemJackson2ModuleConfigurationTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ProblemJacksonAutoConfiguration.class));

  @Test
  void givenJackson2OnClasspath_whenContextStarts_thenRegistersModule() {
    contextRunner.run(context -> assertThat(context).hasSingleBean(ProblemModule.class));
  }

  @Test
  void givenJackson2OnClasspath_whenModuleRegistered_thenObjectMapperSerializesProblem() {
    contextRunner.run(
        context -> {
          ObjectMapper objectMapper =
              new ObjectMapper().registerModule(context.getBean(ProblemModule.class));
          Problem problem =
              Problem.builder()
                  .type("https://example.org/errors/conflict")
                  .title("Conflict")
                  .status(409)
                  .extension("reason", "duplicate")
                  .build();

          Problem result =
              objectMapper.readValue(objectMapper.writeValueAsString(problem), Problem.class);

          assertThat(result).isEqualTo(problem);
        });
  }

  @Test
  void givenUserDefinedModule_whenContextStarts_thenBacksOff() {
    contextRunner
        .withUserConfiguration(CustomModuleConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(ProblemModule.class);
              assertThat(context.getBean(ProblemModule.class))
                  .isSameAs(context.getBean("customProblemModule"));
            });
  }

  @Test
  void givenJackson2NotOnClasspath_whenContextStarts_thenDoesNotRegisterModule() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(Module.class))
        .run(context -> assertThat(context).doesNotHaveBean(ProblemModule.class));
  }

  @Test
  void givenProblem4jJackson2NotOnClasspath_whenContextStarts_thenDoesNotRegisterModule() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(ProblemModule.class))
        .run(context -> assertThat(context).doesNotHaveBean(ProblemModule.class));
  }

  @Test
  void givenProblem4jDisabled_whenContextStarts_thenDoesNotRegisterModule() {
    contextRunner
        .withPropertyValues("problem4j.enabled=false")
        .run(context -> assertThat(context).doesNotHaveBean(ProblemModule.class));
  }

  @Test
  void givenNonWebApplication_whenContextStarts_thenRegistersModule() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ProblemJacksonAutoConfiguration.class))
        .run(context -> assertThat(context).hasSingleBean(ProblemModule.class));
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomModuleConfiguration {

    @Bean
    ProblemModule customProblemModule() {
      return new ProblemModule();
    }
  }
}
