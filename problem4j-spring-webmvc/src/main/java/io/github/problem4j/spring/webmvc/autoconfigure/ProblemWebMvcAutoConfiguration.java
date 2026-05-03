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

package io.github.problem4j.spring.webmvc.autoconfigure;

import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties;
import io.github.problem4j.spring.webmvc.AdviceWebMvcInspector;
import io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter;
import io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler;
import io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Spring Boot autoconfiguration for Problem4J integration with Spring WebMVC web environment.
 *
 * <p>This class wires all necessary beans for producing standardized {@code Problem} responses from
 * Spring MVC controllers. It includes:
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
@EnableConfigurationProperties({ProblemWebMvcProperties.class})
@ConditionalOnBooleanProperty(name = "problem4j.webmvc.enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class, WebMvcAutoConfiguration.class})
@Import({ProblemErrorMvcConfiguration.class, ProblemResolverWebMvcConfiguration.class})
public class ProblemWebMvcAutoConfiguration {

  /**
   * Creates the default {@link ExceptionWebMvcAdvice} used for handling exceptions in WebMVC
   * applications.
   *
   * <p>The advice intercepts thrown exceptions and resolves them to {@code Problem} objects
   * according {@code ProblemResolver}-s managed by {@link ProblemResolverStore}.
   */
  @Order(Ordered.LOWEST_PRECEDENCE)
  @ConditionalOnBooleanProperty(
      name = "problem4j.webmvc.exception-advice.enabled",
      matchIfMissing = true)
  @ConditionalOnMissingBean(ExceptionWebMvcAdvice.class)
  @Bean
  ExceptionWebMvcAdvice exceptionWebMvcAdvice(
      ProblemMapper problemMapper,
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebMvcInspector> adviceWebMvcInspectors) {
    return new ExceptionWebMvcAdvice(
        problemMapper, problemResolverStore, problemPostProcessor, adviceWebMvcInspectors);
  }

  /**
   * Creates the default {@link ProblemExceptionWebMvcAdvice}, responsible for handling
   * Problem4J-specific exception types in WebMVC pipelines.
   *
   * <p>This advice focuses on translating {@code Problem}-domain exceptions into standardized
   * problem responses, using the configured post processor and inspectors.
   */
  @Order(Ordered.LOWEST_PRECEDENCE - 10)
  @ConditionalOnBooleanProperty(
      name = "problem4j.webmvc.problem-exception-advice.enabled",
      matchIfMissing = true)
  @ConditionalOnMissingBean(ProblemExceptionWebMvcAdvice.class)
  @Bean
  ProblemExceptionWebMvcAdvice problemExceptionWebMvcAdvice(
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebMvcInspector> adviceWebMvcInspectors) {
    return new ProblemExceptionWebMvcAdvice(problemPostProcessor, adviceWebMvcInspectors);
  }

  /**
   * Nested configuration that registers the {@link ProblemContextWebMvcFilter} responsible for
   * preparing and propagating the Problem4J context across WebMVC request handling.
   */
  @ConditionalOnBooleanProperty(
      name = "problem4j.webmvc.problem-context-filter.enabled",
      matchIfMissing = true)
  @ConditionalOnClass(OncePerRequestFilter.class)
  @Configuration(proxyBeanMethods = false)
  static class ProblemContextMvcFilterConfiguration {

    /**
     * Registers the default {@link ProblemContextWebMvcFilter}, which initializes and propagates
     * Problem4J contextual metadata throughout the request lifecycle.
     */
    @ConditionalOnMissingBean(ProblemContextWebMvcFilter.class)
    @Bean
    ProblemContextWebMvcFilter problemContextWebMvcFilter(ProblemProperties properties) {
      return new ProblemContextWebMvcFilter(properties);
    }
  }

  /**
   * Nested configuration that replaces the default WebMVC exception handler with a
   * Problem4J-enhanced implementation.
   */
  @ConditionalOnBooleanProperty(
      name = "problem4j.webmvc.exception-handler.enabled",
      matchIfMissing = true)
  @ConditionalOnClass(ResponseEntityExceptionHandler.class)
  @Configuration(proxyBeanMethods = false)
  static class ResponseEntityExceptionHandlerConfiguration {

    /**
     * Provides the Problem4J-enhanced {@link ResponseEntityExceptionHandler} implementation for
     * WebMVC applications.
     */
    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    @ConditionalOnMissingBean(ResponseEntityExceptionHandler.class)
    @Bean
    ResponseEntityExceptionHandler problemEnhancedWebMvcHandler(
        ProblemResolverStore problemResolverStore,
        ProblemPostProcessor problemPostProcessor,
        List<AdviceWebMvcInspector> adviceWebMvcInspectors) {
      return new ProblemEnhancedWebMvcHandler(
          problemResolverStore, problemPostProcessor, adviceWebMvcInspectors);
    }
  }
}
