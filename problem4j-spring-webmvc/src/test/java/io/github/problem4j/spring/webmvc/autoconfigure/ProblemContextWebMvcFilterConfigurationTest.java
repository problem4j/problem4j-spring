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

import static io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.UNAVAILABLE_SOURCE_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.webmvc.MicrometerProblemContextWebMvcFilter;
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter;
import io.micrometer.tracing.handler.TracingObservationHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class ProblemContextWebMvcFilterConfigurationTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  ProblemAutoConfiguration.class,
                  ProblemWebMvcAutoConfiguration.ProblemContextMvcFilterConfiguration.class));

  @Test
  void givenNoTracingSource_whenContextStarts_thenMicrometerFilterIsRegistered() {
    contextRunner.run(context ->
        assertThat(context)
            .getBean(ProblemContextWebMvcFilter.class)
            .isExactlyInstanceOf(MicrometerProblemContextWebMvcFilter.class));
  }

  @Test
  void givenSimpleTracingSource_whenContextStarts_thenDefaultFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=simple")
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebMvcFilter.class)
                .isExactlyInstanceOf(ProblemContextWebMvcFilter.class));
  }

  @Test
  void givenMicrometerTracingSource_whenContextStarts_thenMicrometerFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=micrometer")
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebMvcFilter.class)
                .isExactlyInstanceOf(MicrometerProblemContextWebMvcFilter.class));
  }

  @Test
  void givenMicrometerTracingSourceAndCustomFilter_whenContextStarts_thenCustomFilterIsKept() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=micrometer")
        .withUserConfiguration(CustomFilterConfiguration.class)
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebMvcFilter.class)
                .isSameAs(context.getBean(CustomFilterConfiguration.class).filter));
  }

  @Test
  void givenMicrometerTracingSourceWithoutMicrometerTracing_whenContextStarts_thenStartupFails() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=micrometer")
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .run(context ->
            assertThat(context)
                .hasFailed()
                .getFailure()
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(UNAVAILABLE_SOURCE_MESSAGE));
  }

  @Test
  void
      givenNoTracingSourceWithoutMicrometerTracing_whenContextStarts_thenDefaultFilterIsRegistered() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebMvcFilter.class)
                .isExactlyInstanceOf(ProblemContextWebMvcFilter.class));
  }

  @Test
  void
      givenFilterDisabledWithMicrometerTracingSourceWithoutMicrometerTracing_whenContextStarts_thenNoFilterIsRegistered() {
    contextRunner
        .withPropertyValues(
            "problem4j.tracing.source=micrometer",
            "problem4j.webmvc.problem-context-filter.enabled=false")
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .run(context -> assertThat(context).doesNotHaveBean(ProblemContextWebMvcFilter.class));
  }

  @Test
  void givenAutoTracingSource_whenContextStarts_thenMicrometerFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=auto")
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebMvcFilter.class)
                .isExactlyInstanceOf(MicrometerProblemContextWebMvcFilter.class));
  }

  @Test
  void
      givenAutoTracingSourceWithoutMicrometerTracing_whenContextStarts_thenDefaultFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=auto")
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebMvcFilter.class)
                .isExactlyInstanceOf(ProblemContextWebMvcFilter.class));
  }

  @Test
  void
      givenSimpleTracingSourceWithoutMicrometerTracing_whenContextStarts_thenDefaultFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=simple")
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebMvcFilter.class)
                .isExactlyInstanceOf(ProblemContextWebMvcFilter.class));
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomFilterConfiguration {

    final ProblemContextWebMvcFilter filter = new ProblemContextWebMvcFilter(() -> null);

    @Bean
    ProblemContextWebMvcFilter customProblemContextWebMvcFilter() {
      return filter;
    }
  }
}
