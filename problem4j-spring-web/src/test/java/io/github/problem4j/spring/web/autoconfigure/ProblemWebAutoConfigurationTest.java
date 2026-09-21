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
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.CachingProblemResolverStore;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

class ProblemWebAutoConfigurationTest {

  private final AutoConfigurations autoConfigurations =
      AutoConfigurations.of(ProblemAutoConfiguration.class, ProblemWebAutoConfiguration.class);

  @Test
  void givenWebApplication_whenContextStarts_thenRegistersGenericAndWebBeans() {
    new WebApplicationContextRunner()
        .withConfiguration(autoConfigurations)
        .run(
            context -> {
              assertThat(context).hasSingleBean(ProblemAutoConfiguration.class);
              assertThat(context).hasSingleBean(ProblemWebAutoConfiguration.class);
              assertThat(context).hasSingleBean(ProblemFormat.class);
              assertThat(context).hasSingleBean(ProblemResolverStore.class);
              assertThat(context).hasSingleBean(MethodParameterSupport.class);
              assertThat(context).hasSingleBean(BindingResultSupport.class);
              assertThat(context).hasSingleBean(MethodValidationResultSupport.class);
              assertThat(context.getBeansOfType(ProblemResolver.class)).isNotEmpty();
            });
  }

  @Test
  void givenNonWebApplication_whenContextStarts_thenRegistersOnlyGenericBeans() {
    new ApplicationContextRunner()
        .withConfiguration(autoConfigurations)
        .run(
            context -> {
              assertThat(context).hasSingleBean(ProblemAutoConfiguration.class);
              assertThat(context).hasSingleBean(ProblemProperties.class);
              assertThat(context).hasSingleBean(ProblemMapper.class);
              assertThat(context).hasSingleBean(ProblemFormat.class);
              assertThat(context).hasSingleBean(ProblemPostProcessor.class);
              assertThat(context).hasSingleBean(TypeNameMapper.class);
              assertThat(context).hasSingleBean(ProblemBeanPostProcessor.class);

              assertThat(context).doesNotHaveBean(ProblemWebAutoConfiguration.class);
              assertThat(context).doesNotHaveBean(ProblemResolverStore.class);
              assertThat(context).doesNotHaveBean(ProblemResolver.class);
              assertThat(context).doesNotHaveBean(MethodParameterSupport.class);
              assertThat(context).doesNotHaveBean(BindingResultSupport.class);
              assertThat(context).doesNotHaveBean(MethodValidationResultSupport.class);
            });
  }

  @Test
  void givenProblem4jDisabled_whenContextStarts_thenRegistersNoBeans() {
    new WebApplicationContextRunner()
        .withConfiguration(autoConfigurations)
        .withPropertyValues("problem4j.enabled=false")
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(ProblemAutoConfiguration.class);
              assertThat(context).doesNotHaveBean(ProblemWebAutoConfiguration.class);
              assertThat(context).doesNotHaveBean(ProblemFormat.class);
              assertThat(context).doesNotHaveBean(ProblemResolverStore.class);
            });
  }

  @Test
  void givenFirstPolicyAndResolversWithEqualOrder_whenContextStarts_thenFirstRegisteredWins() {
    new WebApplicationContextRunner()
        .withConfiguration(autoConfigurations)
        .withUserConfiguration(EqualOrderResolversConfiguration.class)
        .withPropertyValues("problem4j.duplicate-resolver-policy=first")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(
                      context
                          .getBean(ProblemResolverStore.class)
                          .findResolver(DuplicatedException.class))
                  .containsSame(context.getBean("firstDuplicatedResolver", ProblemResolver.class));
            });
  }

  @Test
  void givenDefaultPolicyAndResolversWithEqualOrder_whenContextStarts_thenFails() {
    new WebApplicationContextRunner()
        .withConfiguration(autoConfigurations)
        .withUserConfiguration(EqualOrderResolversConfiguration.class)
        .run(
            context ->
                assertThat(context)
                    .getFailure()
                    .rootCause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(DuplicatedException.class.getName()));
  }

  @Test
  void givenFailPolicyAndResolversWithEqualOrder_whenContextStarts_thenFails() {
    new WebApplicationContextRunner()
        .withConfiguration(autoConfigurations)
        .withUserConfiguration(EqualOrderResolversConfiguration.class)
        .withPropertyValues("problem4j.duplicate-resolver-policy=fail")
        .run(
            context ->
                assertThat(context)
                    .getFailure()
                    .rootCause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(DuplicatedException.class.getName())
                    .hasMessageContaining("'firstDuplicatedResolver'")
                    .hasMessageContaining("'secondDuplicatedResolver'"));
  }

  @Test
  void givenFailPolicyAndResolversWithDifferentOrder_whenContextStarts_thenLowerOrderWins() {
    new WebApplicationContextRunner()
        .withConfiguration(autoConfigurations)
        .withUserConfiguration(DifferentOrderResolversConfiguration.class)
        .withPropertyValues("problem4j.duplicate-resolver-policy=fail")
        .run(
            context ->
                assertThat(
                        context
                            .getBean(ProblemResolverStore.class)
                            .findResolver(DuplicatedException.class))
                    .get()
                    .isInstanceOf(PrioritizedResolver.class));
  }

  @Test
  void givenFailPolicyAndResolverCaching_whenContextStarts_thenCachingStoreResolves() {
    new WebApplicationContextRunner()
        .withConfiguration(autoConfigurations)
        .withUserConfiguration(DifferentOrderResolversConfiguration.class)
        .withPropertyValues(
            "problem4j.duplicate-resolver-policy=fail", "problem4j.resolver-caching.enabled=true")
        .run(
            context -> {
              ProblemResolverStore store = context.getBean(ProblemResolverStore.class);

              assertThat(store).isInstanceOf(CachingProblemResolverStore.class);
              assertThat(store.findResolver(DuplicatedException.class))
                  .get()
                  .isInstanceOf(PrioritizedResolver.class);
            });
  }

  @Configuration(proxyBeanMethods = false)
  static class EqualOrderResolversConfiguration {

    @Bean
    DuplicatedResolver firstDuplicatedResolver() {
      return new DuplicatedResolver();
    }

    @Bean
    DuplicatedResolver secondDuplicatedResolver() {
      return new DuplicatedResolver();
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class DifferentOrderResolversConfiguration {

    @Bean
    DuplicatedResolver duplicatedResolver() {
      return new DuplicatedResolver();
    }

    @Bean
    PrioritizedResolver prioritizedResolver() {
      return new PrioritizedResolver();
    }
  }

  static class DuplicatedResolver implements ProblemResolver {

    @Override
    public Class<? extends Exception> getExceptionClass() {
      return DuplicatedException.class;
    }

    @Override
    public Problem resolve(
        ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
      return Problem.builder().status(400).build();
    }
  }

  @Order(0)
  static class PrioritizedResolver extends DuplicatedResolver {}

  static class DuplicatedException extends RuntimeException {}
}
