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

import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties;
import io.github.problem4j.spring.webflux.AdviceWebFluxInspector;
import io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice;
import io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter;
import io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler;
import io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration;
import org.springframework.boot.webflux.autoconfigure.error.ErrorWebFluxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.WebFilter;

/**
 * Spring Boot autoconfiguration for Problem4J integration with Spring WebFlux web environment.
 *
 * <p>This class wires all necessary beans for producing standardized {@code Problem} responses from
 * Spring WebFlux controllers. It includes:
 *
 * <p>Beans are conditional:
 *
 * <ul>
 *   <li>{@link ConditionalOnMissingBean} ensures user-defined beans override defaults.
 *   <li>{@link ConditionalOnClass} ensures compatibility with optional framework classes.
 * </ul>
 *
 * @since 1.2.0
 */
@AutoConfiguration
@EnableConfigurationProperties({ProblemWebFluxProperties.class})
@ConditionalOnBooleanProperty(name = "problem4j.enabled", matchIfMissing = true)
@ConditionalOnBooleanProperty(name = "problem4j.webflux.enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@AutoConfigureBefore({ErrorWebFluxAutoConfiguration.class, WebFluxAutoConfiguration.class})
@Import({ProblemErrorWebFluxConfiguration.class})
public class ProblemWebFluxAutoConfiguration {

  /** Creates a new instance of this autoconfiguration. */
  public ProblemWebFluxAutoConfiguration() {}

  /**
   * Creates the default {@link ExceptionWebFluxAdvice} used for handling exceptions in WebFlux
   * applications.
   *
   * <p>The advice intercepts thrown exceptions and resolves them to {@code Problem} objects
   * according {@code ProblemResolver}-s managed by {@link ProblemResolverStore}.
   */
  @Order(Ordered.LOWEST_PRECEDENCE)
  @ConditionalOnBooleanProperty(
      name = "problem4j.webflux.exception-advice.enabled",
      matchIfMissing = true)
  @ConditionalOnMissingBean(ExceptionWebFluxAdvice.class)
  @Bean
  ExceptionWebFluxAdvice exceptionWebFluxAdvice(
      ProblemMapper problemMapper,
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<? extends AdviceWebFluxInspector> adviceWebFluxInspectors) {
    return new ExceptionWebFluxAdvice(
        problemMapper, problemResolverStore, problemPostProcessor, adviceWebFluxInspectors);
  }

  /**
   * Creates the default {@link ProblemExceptionWebFluxAdvice}, responsible for handling
   * Problem4J-specific exception types in WebFlux pipelines.
   *
   * <p>This advice focuses on translating {@code Problem}-domain exceptions into standardized
   * problem responses, using the configured post processor and inspectors.
   */
  @Order(Ordered.LOWEST_PRECEDENCE - 10)
  @ConditionalOnBooleanProperty(
      name = "problem4j.webflux.problem-exception-advice.enabled",
      matchIfMissing = true)
  @ConditionalOnMissingBean(ProblemExceptionWebFluxAdvice.class)
  @Bean
  ProblemExceptionWebFluxAdvice problemExceptionWebFluxAdvice(
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebFluxInspector> adviceWebFluxInspectors) {
    return new ProblemExceptionWebFluxAdvice(problemPostProcessor, adviceWebFluxInspectors);
  }

  /**
   * Nested configuration that registers the {@link ProblemContextWebFluxFilter} responsible for
   * preparing and propagating the Problem4J context across WebFlux request handling.
   */
  @ConditionalOnBooleanProperty(
      name = "problem4j.webflux.problem-context-filter.enabled",
      matchIfMissing = true)
  @ConditionalOnClass(WebFilter.class)
  @Configuration(proxyBeanMethods = false)
  static class ProblemContextWebFluxFilterConfiguration {

    /** Creates a new instance of this configuration. */
    ProblemContextWebFluxFilterConfiguration() {}

    /**
     * Registers the default {@link ProblemContextWebFluxFilter}, which initializes and propagates
     * Problem4J contextual metadata throughout the request lifecycle.
     */
    @ConditionalOnMissingBean(ProblemContextWebFluxFilter.class)
    @Bean
    ProblemContextWebFluxFilter problemContextWebFluxFilter(ProblemProperties properties) {
      return new ProblemContextWebFluxFilter(properties);
    }
  }

  /**
   * Nested configuration that replaces the default WebFlux exception handler with a
   * Problem4J-enhanced implementation.
   */
  @ConditionalOnBooleanProperty(
      name = "problem4j.webflux.exception-handler.enabled",
      matchIfMissing = true)
  @ConditionalOnClass(ResponseEntityExceptionHandler.class)
  @Configuration(proxyBeanMethods = false)
  static class ResponseEntityExceptionHandlerConfiguration {

    /** Creates a new instance of this configuration. */
    ResponseEntityExceptionHandlerConfiguration() {}

    /**
     * Provides the Problem4J-enhanced {@link ResponseEntityExceptionHandler} implementation for
     * WebFlux applications.
     */
    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    @ConditionalOnMissingBean(ResponseEntityExceptionHandler.class)
    @Bean
    ResponseEntityExceptionHandler problemEnhancedWebFluxHandler(
        ProblemResolverStore problemResolverStore,
        ProblemPostProcessor problemPostProcessor,
        List<AdviceWebFluxInspector> adviceWebFluxInspectors) {
      return new ProblemEnhancedWebFluxHandler(
          problemResolverStore, problemPostProcessor, adviceWebFluxInspectors);
    }
  }
}
