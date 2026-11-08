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

package io.github.problem4j.spring.webflux.autoconfigure;

import static io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.UNAVAILABLE_SOURCE_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.webflux.MicrometerProblemContextWebFluxFilter;
import io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter;
import io.micrometer.tracing.handler.TracingObservationHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class ProblemContextWebFluxFilterConfigurationTest {

  private final ReactiveWebApplicationContextRunner contextRunner =
      new ReactiveWebApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  ProblemAutoConfiguration.class,
                  ProblemWebFluxAutoConfiguration.ProblemContextWebFluxFilterConfiguration.class));

  @Test
  void givenNoTracingSource_whenContextStarts_thenMicrometerFilterIsRegistered() {
    contextRunner.run(context ->
        assertThat(context)
            .getBean(ProblemContextWebFluxFilter.class)
            .isExactlyInstanceOf(MicrometerProblemContextWebFluxFilter.class));
  }

  @Test
  void givenSimpleTracingSource_whenContextStarts_thenDefaultFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=simple")
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebFluxFilter.class)
                .isExactlyInstanceOf(ProblemContextWebFluxFilter.class));
  }

  @Test
  void givenMicrometerTracingSource_whenContextStarts_thenMicrometerFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=micrometer")
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebFluxFilter.class)
                .isExactlyInstanceOf(MicrometerProblemContextWebFluxFilter.class));
  }

  @Test
  void givenMicrometerTracingSourceAndCustomFilter_whenContextStarts_thenCustomFilterIsKept() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=micrometer")
        .withUserConfiguration(CustomFilterConfiguration.class)
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebFluxFilter.class)
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
                .getBean(ProblemContextWebFluxFilter.class)
                .isExactlyInstanceOf(ProblemContextWebFluxFilter.class));
  }

  @Test
  void
      givenFilterDisabledWithMicrometerTracingSourceWithoutMicrometerTracing_whenContextStarts_thenNoFilterIsRegistered() {
    contextRunner
        .withPropertyValues(
            "problem4j.tracing.source=micrometer",
            "problem4j.webflux.problem-context-filter.enabled=false")
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .run(context -> assertThat(context).doesNotHaveBean(ProblemContextWebFluxFilter.class));
  }

  @Test
  void givenAutoTracingSource_whenContextStarts_thenMicrometerFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=auto")
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebFluxFilter.class)
                .isExactlyInstanceOf(MicrometerProblemContextWebFluxFilter.class));
  }

  @Test
  void
      givenAutoTracingSourceWithoutMicrometerTracing_whenContextStarts_thenDefaultFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=auto")
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebFluxFilter.class)
                .isExactlyInstanceOf(ProblemContextWebFluxFilter.class));
  }

  @Test
  void
      givenSimpleTracingSourceWithoutMicrometerTracing_whenContextStarts_thenDefaultFilterIsRegistered() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=simple")
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .run(context ->
            assertThat(context)
                .getBean(ProblemContextWebFluxFilter.class)
                .isExactlyInstanceOf(ProblemContextWebFluxFilter.class));
  }

  @Test
  void
      givenMicrometerTracingSourceWithoutMicrometerTracingAndCustomFilter_whenContextStarts_thenCustomFilterIsKept() {
    contextRunner
        .withPropertyValues("problem4j.tracing.source=micrometer")
        .withClassLoader(new FilteredClassLoader(TracingObservationHandler.class))
        .withUserConfiguration(CustomFilterConfiguration.class)
        .run(context ->
            assertThat(context)
                .hasNotFailed()
                .getBean(ProblemContextWebFluxFilter.class)
                .isSameAs(context.getBean(CustomFilterConfiguration.class).filter));
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomFilterConfiguration {

    final ProblemContextWebFluxFilter filter = new ProblemContextWebFluxFilter(() -> null);

    @Bean
    ProblemContextWebFluxFilter customProblemContextWebFluxFilter() {
      return filter;
    }
  }
}
