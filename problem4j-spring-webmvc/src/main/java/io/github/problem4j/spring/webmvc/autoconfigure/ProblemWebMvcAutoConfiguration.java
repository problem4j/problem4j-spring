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

import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties;
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties.ErrorRecording;
import io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.OnMicrometerSource;
import io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.OnSimpleSource;
import io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.OnUnavailableSource;
import io.github.problem4j.spring.web.autoconfigure.UnavailableTracingSourceException;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.MicrometerProblemContextFilter;
import io.github.problem4j.spring.webmvc.ObservationErrorProblemInspector;
import io.github.problem4j.spring.webmvc.ProblemContextFilter;
import io.github.problem4j.spring.webmvc.ProblemContextMethodArgumentResolver;
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter;
import io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler;
import io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.ProblemInspector;
import io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler;
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
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
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
@ConditionalOnBooleanProperty(name = "problem4j.enabled", matchIfMissing = true)
@ConditionalOnBooleanProperty(name = "problem4j.webmvc.enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class, WebMvcAutoConfiguration.class})
@Import({ProblemErrorMvcConfiguration.class, ProblemResolverWebMvcConfiguration.class})
public class ProblemWebMvcAutoConfiguration {

  /**
   * Creates a new instance of this autoconfiguration.
   *
   * @since 1.2.0
   */
  public ProblemWebMvcAutoConfiguration() {}

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
      ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
    return new ExceptionWebMvcAdvice(problemOperations, problemInspectors);
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
      ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
    return new ProblemExceptionWebMvcAdvice(problemOperations, problemInspectors);
  }

  /**
   * Registers the default {@link ProblemContextMethodArgumentResolver}, resolving controller method
   * arguments of {@code ProblemContext} type without any annotation.
   */
  @ConditionalOnMissingBean(ProblemContextMethodArgumentResolver.class)
  @Bean
  ProblemContextMethodArgumentResolver problemContextMethodArgumentResolver() {
    return new ProblemContextMethodArgumentResolver();
  }

  /**
   * Adds {@link ProblemContextMethodArgumentResolver} to the custom argument resolvers of WebMVC.
   */
  @Bean
  WebMvcConfigurer problemContextMethodArgumentResolverConfigurer(
      ProblemContextMethodArgumentResolver resolver) {
    return new WebMvcConfigurer() {
      @Override
      public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
      }
    };
  }

  /**
   * Nested configuration that registers the {@link ObservationErrorProblemInspector}, recording
   * handled exceptions as errors of the current HTTP server observation.
   */
  @ConditionalOnClass(ServerRequestObservationContext.class)
  @Configuration(proxyBeanMethods = false)
  static class ObservationErrorConfiguration {

    /** Creates a new instance of this configuration. */
    ObservationErrorConfiguration() {}

    /**
     * Registers the default {@link ObservationErrorProblemInspector}, recording handled exceptions
     * according to {@code problem4j.observation-errors}.
     */
    @ConditionalOnMissingBean(ObservationErrorProblemInspector.class)
    @Bean
    ObservationErrorProblemInspector observationErrorProblemInspector(
        ProblemProperties properties) {
      ErrorRecording observationErrors = properties.getObservationErrors();
      return new ObservationErrorProblemInspector(
          status -> observationErrors.matches(status.value()));
    }
  }

  /**
   * Nested configuration that registers the {@link ProblemContextFilter} responsible for preparing
   * and propagating the Problem4J context across WebMVC request handling.
   */
  @ConditionalOnBooleanProperty(
      name = "problem4j.webmvc.problem-context-filter.enabled",
      matchIfMissing = true)
  @ConditionalOnClass(OncePerRequestFilter.class)
  @Configuration(proxyBeanMethods = false)
  static class ProblemContextMvcFilterConfiguration {

    /** Creates a new instance of this configuration. */
    ProblemContextMvcFilterConfiguration() {}

    /**
     * Registers the default {@link ProblemContextFilter}, which takes trace identifiers from the
     * configured tracing header or generates new ones. Used with {@code
     * problem4j.tracing-source=simple}, or {@code auto} (default) without Micrometer Tracing.
     */
    @Conditional(OnSimpleSource.class)
    @Configuration(proxyBeanMethods = false)
    static class SimpleTracingConfiguration {

      /** Creates a new instance of this configuration. */
      SimpleTracingConfiguration() {}

      /**
       * Registers the default {@link ProblemContextFilter}, which initializes and propagates
       * Problem4J contextual metadata throughout the request lifecycle.
       *
       * <p>Creates deprecated {@link ProblemContextWebMvcFilter} (a {@link ProblemContextFilter})
       * until it is removed, so that applications injecting the filter by its former type keep
       * working.
       */
      @ConditionalOnMissingBean(ProblemContextFilter.class)
      @Bean
      @SuppressWarnings("removal")
      ProblemContextWebMvcFilter problemContextWebMvcFilter(ProblemProperties properties) {
        return new ProblemContextWebMvcFilter(properties);
      }
    }

    /**
     * Registers {@link MicrometerProblemContextFilter}, which takes trace identifiers from the
     * current Micrometer Tracing span. Used with {@code problem4j.tracing-source=micrometer}, or
     * {@code auto} (default) with Micrometer Tracing.
     */
    @Conditional(OnMicrometerSource.class)
    @Configuration(proxyBeanMethods = false)
    static class MicrometerTracingConfiguration {

      /** Creates a new instance of this configuration. */
      MicrometerTracingConfiguration() {}

      /**
       * Registers {@link MicrometerProblemContextFilter}, which initializes and propagates
       * Problem4J contextual metadata throughout the request lifecycle.
       */
      @ConditionalOnMissingBean(ProblemContextFilter.class)
      @Bean
      ProblemContextFilter problemContextWebMvcFilter(ProblemProperties properties) {
        return new MicrometerProblemContextFilter(properties);
      }
    }

    /**
     * Fails application startup if {@code problem4j.tracing-source} is set to a source that cannot
     * be used, as its required dependencies are not on the classpath. Skipped if a custom {@link
     * ProblemContextFilter} bean is defined, as the tracing source is not used then.
     */
    @Conditional(OnUnavailableSource.class)
    @ConditionalOnMissingBean(ProblemContextFilter.class)
    @Configuration(proxyBeanMethods = false)
    static class UnavailableTracingSourceConfiguration {

      /**
       * Always throws, as the configured {@code problem4j.tracing-source} cannot be satisfied.
       *
       * @throws UnavailableTracingSourceException always
       */
      UnavailableTracingSourceConfiguration() {
        throw new UnavailableTracingSourceException();
      }
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

    /** Creates a new instance of this configuration. */
    ResponseEntityExceptionHandlerConfiguration() {}

    /**
     * Provides the Problem4J-enhanced {@link ResponseEntityExceptionHandler} implementation for
     * WebMVC applications.
     *
     * <p>Creates deprecated {@link ProblemEnhancedWebMvcHandler} (a {@link
     * ProblemResponseEntityExceptionHandler}) until it is removed, so that applications injecting
     * it by its former type keep working.
     */
    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    @ConditionalOnMissingBean(ResponseEntityExceptionHandler.class)
    @SuppressWarnings("removal")
    @Bean
    ResponseEntityExceptionHandler problemEnhancedWebMvcHandler(
        ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
      return new ProblemEnhancedWebMvcHandler(problemOperations, problemInspectors);
    }
  }
}
