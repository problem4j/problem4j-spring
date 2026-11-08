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

package io.github.problem4j.spring.webmvc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class ProblemContextArgumentResolverConfigurationTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  ProblemAutoConfiguration.class,
                  ProblemWebMvcAutoConfiguration.ProblemContextArgumentResolverConfiguration
                      .class));

  @Test
  void givenDefaultConfiguration_whenContextStarts_thenResolverIsRegistered() {
    contextRunner.run(
        context -> assertThat(context).hasSingleBean(ProblemContextWebMvcArgumentResolver.class));
  }

  @Test
  void givenDefaultConfiguration_whenContextStarts_thenResolverIsAddedToWebMvc() {
    contextRunner.run(context -> assertThat(context).hasSingleBean(WebMvcConfigurer.class));
  }

  @Test
  void givenCustomResolver_whenContextStarts_thenCustomResolverIsKept() {
    contextRunner
        .withUserConfiguration(CustomResolverConfiguration.class)
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebMvcArgumentResolver.class)
                .isSameAs(context.getBean(CustomResolverConfiguration.class).resolver));
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomResolverConfiguration {

    final ProblemContextWebMvcArgumentResolver resolver =
        new ProblemContextWebMvcArgumentResolver();

    @Bean
    ProblemContextWebMvcArgumentResolver customProblemContextWebMvcArgumentResolver() {
      return resolver;
    }
  }
}
