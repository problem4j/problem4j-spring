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

import io.github.problem4j.core.DefaultProblemMapper;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.DefaultProblemFormat;
import io.github.problem4j.spring.web.DefaultProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

/**
 * Spring Boot autoconfiguration for Problem4J integration.
 *
 * <p>This class wires generic beans for creating and post-processing {@code Problem} objects, which
 * do not depend on HTTP nor require a web application. Web-related beans are wired by {@link
 * ProblemWebAutoConfiguration}.
 *
 * @see io.github.problem4j.core.Problem
 * @since 1.2.0
 */
@AutoConfiguration
@EnableConfigurationProperties({ProblemProperties.class})
@ConditionalOnBooleanProperty(name = "problem4j.enabled", matchIfMissing = true)
public class ProblemAutoConfiguration {

  /**
   * Creates a new instance of this autoconfiguration.
   *
   * @since 1.2.0
   */
  public ProblemAutoConfiguration() {}

  /**
   * Provides a {@link ProblemMapper} if none is defined.
   *
   * @return a new {@link ProblemMapper}
   */
  @ConditionalOnMissingBean(ProblemMapper.class)
  @Bean
  ProblemMapper problemMapper() {
    return new DefaultProblemMapper();
  }

  /**
   * Provides a {@link ProblemFormat} based on {@link ProblemProperties} if none is defined.
   *
   * @param properties the configuration properties
   * @return a new {@link DefaultProblemFormat}
   */
  @ConditionalOnMissingBean(ProblemFormat.class)
  @Bean
  ProblemFormat problemFormat(ProblemProperties properties) {
    return new DefaultProblemFormat(properties.getDetailFormat());
  }

  /**
   * Provides a {@link ProblemPostProcessor} that applies post-processing rules to {@code Problem}
   * instances before they are returned.
   *
   * <p>The default implementation, {@link DefaultProblemPostProcessor}, supports configurable
   * overrides for problem fields such as {@code type} and {@code instance}, based on the properties
   * defined in {@link ProblemProperties}. These overrides may include runtime placeholders such as:
   *
   * <ul>
   *   <li>{@code {problem.type}} - replaced with the original problem's type URI
   *   <li>{@code {problem.instance}} - replaced with the original problem's instance URI
   *   <li>{@code {context.<key>}} - replaced with the value for {@code key} from the current {@link
   *       ProblemContext}
   * </ul>
   *
   * <p>This allows enriching or normalizing problem responses without modifying the original
   * exception mapping logic.
   *
   * @param properties the configuration properties containing override templates and settings
   * @return a new {@link DefaultProblemPostProcessor} instance
   * @see io.github.problem4j.core.Problem
   */
  @ConditionalOnMissingBean(ProblemPostProcessor.class)
  @Bean
  ProblemPostProcessor problemPostProcessor(ProblemProperties properties) {
    return new DefaultProblemPostProcessor(properties);
  }

  /**
   * Provides a {@link TypeNameMapper} that maps Java types to string names for inclusion in problem
   * responses. The default implementation, {@link SimpleTypeNameMapper} supports simple Java types,
   * such as primitives, numbers, strings, arrays and lists.
   *
   * @return a new {@link SimpleTypeNameMapper}
   */
  @ConditionalOnMissingBean(TypeNameMapper.class)
  @Bean
  TypeNameMapper problemTypeNameMapper() {
    return new SimpleTypeNameMapper();
  }

  /**
   * Provides the single {@link ProblemBeanPostProcessor} that injects the container's Problem4J
   * collaborators ({@link ProblemFormat}, {@link TypeNameMapper}, {@link BindingResultSupport},
   * {@link MethodValidationResultSupport}, {@link MethodParameterSupport}) into every bean that
   * opts in through one of the {@code *Aware} callback interfaces, so overriding built-in resolvers
   * can be simplified.
   *
   * @param problemFormat provider for the container's {@link ProblemFormat} bean
   * @param typeNameMapper provider for the container's {@link TypeNameMapper} bean
   * @param bindingResultSupport provider for the container's {@link BindingResultSupport} bean
   * @param methodValidationResultSupport provider for the container's {@link
   *     MethodValidationResultSupport} bean
   * @param methodParameterSupport provider for the container's {@link MethodParameterSupport} bean
   * @return a new {@link ProblemBeanPostProcessor}
   */
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  @ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
  @Bean
  static ProblemBeanPostProcessor problemBeanPostProcessor(
      ObjectProvider<ProblemFormat> problemFormat,
      ObjectProvider<TypeNameMapper> typeNameMapper,
      ObjectProvider<BindingResultSupport> bindingResultSupport,
      ObjectProvider<MethodValidationResultSupport> methodValidationResultSupport,
      ObjectProvider<MethodParameterSupport> methodParameterSupport) {
    return ProblemBeanPostProcessor.builder()
        .problemFormat(problemFormat::getIfAvailable)
        .typeNameMapper(typeNameMapper::getIfAvailable)
        .bindingResultSupport(bindingResultSupport::getIfAvailable)
        .methodValidationResultSupport(methodValidationResultSupport::getIfAvailable)
        .methodParameterSupport(methodParameterSupport::getIfAvailable)
        .build();
  }
}
