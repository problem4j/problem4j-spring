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

import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties;
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties.ErrorRecording;
import io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.OnMicrometerSource;
import io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.OnSimpleSource;
import io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.OnUnavailableSource;
import io.github.problem4j.spring.web.autoconfigure.UnavailableTracingSourceException;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice;
import io.github.problem4j.spring.webflux.MicrometerProblemContextWebFilter;
import io.github.problem4j.spring.webflux.ObservationErrorProblemInspector;
import io.github.problem4j.spring.webflux.ProblemContextMethodArgumentResolver;
import io.github.problem4j.spring.webflux.ProblemContextWebFilter;
import io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter;
import io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler;
import io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice;
import io.github.problem4j.spring.webflux.ProblemHandlerResultHandler;
import io.github.problem4j.spring.webflux.ProblemInspector;
import io.github.problem4j.spring.webflux.ProblemResponseEntityExceptionHandler;
import io.github.problem4j.spring.webflux.ProblemWebExceptionHandler;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration;
import org.springframework.boot.webflux.autoconfigure.error.ErrorWebFluxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityResultHandler;
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
public class ProblemWebFluxAutoConfiguration {

  /** Creates a new instance of this autoconfiguration. */
  public ProblemWebFluxAutoConfiguration() {}

  /**
   * Registers the default {@link ProblemContextMethodArgumentResolver}, resolving controller method
   * arguments of {@code ProblemContext} type without any annotation. Spring Boot's {@code
   * WebFluxAutoConfiguration} adds all {@code HandlerMethodArgumentResolver} beans to the custom
   * argument resolvers of WebFlux, so the bean is not registered via {@code WebFluxConfigurer}.
   */
  @ConditionalOnMissingBean(ProblemContextMethodArgumentResolver.class)
  @Bean
  ProblemContextMethodArgumentResolver problemContextMethodArgumentResolver() {
    return new ProblemContextMethodArgumentResolver();
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
   * Nested configuration that registers the {@link ProblemContextWebFilter} responsible for
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
     * Registers the default {@link ProblemContextWebFilter}, which takes trace identifiers from the
     * configured tracing header or generates new ones. Used with {@code
     * problem4j.tracing-source=simple}, or {@code auto} (default) without Micrometer Tracing.
     */
    @Conditional(OnSimpleSource.class)
    @Configuration(proxyBeanMethods = false)
    static class SimpleTracingConfiguration {

      /** Creates a new instance of this configuration. */
      SimpleTracingConfiguration() {}

      /**
       * Registers the default {@link ProblemContextWebFilter}, which initializes and propagates
       * Problem4J contextual metadata throughout the request lifecycle.
       *
       * <p>Creates deprecated {@link ProblemContextWebFluxFilter} (a {@link
       * ProblemContextWebFilter}) until it is removed, so that applications injecting the filter by
       * its former type keep working.
       */
      @ConditionalOnMissingBean(ProblemContextWebFilter.class)
      @Bean
      @SuppressWarnings("removal")
      ProblemContextWebFluxFilter problemContextWebFluxFilter(ProblemProperties properties) {
        return new ProblemContextWebFluxFilter(properties);
      }
    }

    /**
     * Registers {@link MicrometerProblemContextWebFilter}, which takes trace identifiers from the
     * current Micrometer Tracing span. Used with {@code problem4j.tracing-source=micrometer}, or
     * {@code auto} (default) with Micrometer Tracing.
     */
    @Conditional(OnMicrometerSource.class)
    @Configuration(proxyBeanMethods = false)
    static class MicrometerTracingConfiguration {

      /** Creates a new instance of this configuration. */
      MicrometerTracingConfiguration() {}

      /**
       * Registers {@link MicrometerProblemContextWebFilter}, which initializes and propagates
       * Problem4J contextual metadata throughout the request lifecycle.
       */
      @ConditionalOnMissingBean(ProblemContextWebFilter.class)
      @Bean
      ProblemContextWebFilter problemContextWebFluxFilter(ProblemProperties properties) {
        return new MicrometerProblemContextWebFilter(properties);
      }
    }

    /**
     * Fails application startup if {@code problem4j.tracing-source} is set to a source that cannot
     * be used, as its required dependencies are not on the classpath. Skipped if a custom {@link
     * ProblemContextWebFilter} bean is defined, as the tracing source is not used then.
     */
    @Conditional(OnUnavailableSource.class)
    @ConditionalOnMissingBean(ProblemContextWebFilter.class)
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
   * Nested configuration of {@code problem4j.exception-handling.mode=advice} (default), which
   * registers Problem4J's own controller advices and {@code ErrorWebExceptionHandler}.
   */
  @SuppressWarnings("removal")
  @ConditionalOnProperty(
      name = "problem4j.exception-handling.mode",
      havingValue = "advice",
      matchIfMissing = true)
  @Import({ProblemErrorWebFluxConfiguration.class})
  @Configuration(proxyBeanMethods = false)
  static class AdviceModeConfiguration {

    /** Creates a new instance of this configuration. */
    AdviceModeConfiguration() {}

    /**
     * Creates the default {@link ExceptionWebFluxAdvice} used for handling exceptions in WebFlux
     * applications.
     *
     * <p>The advice intercepts thrown exceptions and resolves them to {@code Problem} objects
     * according {@code ProblemResolver}-s managed by {@link ProblemResolverStore}.
     */
    @ConditionalOnBooleanProperty(
        name = "problem4j.webflux.exception-advice.enabled",
        matchIfMissing = true)
    @ConditionalOnMissingBean(ExceptionWebFluxAdvice.class)
    @Bean
    ExceptionWebFluxAdvice exceptionWebFluxAdvice(
        ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
      return new ExceptionWebFluxAdvice(problemOperations, problemInspectors);
    }

    /**
     * Creates the default {@link ProblemExceptionWebFluxAdvice}, responsible for handling
     * Problem4J-specific exception types in WebFlux pipelines.
     *
     * <p>This advice focuses on translating {@code Problem}-domain exceptions into standardized
     * problem responses, using the configured post processor and inspectors.
     */
    @ConditionalOnBooleanProperty(
        name = "problem4j.webflux.problem-exception-advice.enabled",
        matchIfMissing = true)
    @ConditionalOnMissingBean(ProblemExceptionWebFluxAdvice.class)
    @Bean
    ProblemExceptionWebFluxAdvice problemExceptionWebFluxAdvice(
        ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
      return new ProblemExceptionWebFluxAdvice(problemOperations, problemInspectors);
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
       *
       * <p>Creates deprecated {@link ProblemEnhancedWebFluxHandler} (a {@link
       * ProblemResponseEntityExceptionHandler}) until it is removed, so that applications injecting
       * it by its former type keep working.
       */
      @ConditionalOnMissingBean(ResponseEntityExceptionHandler.class)
      @Bean
      ResponseEntityExceptionHandler problemEnhancedWebFluxHandler(
          ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
        return new ProblemEnhancedWebFluxHandler(problemOperations, problemInspectors);
      }
    }
  }

  /**
   * Nested configuration of {@code problem4j.exception-handling.mode=cooperate}, which registers no
   * controller advices handling exceptions. Exceptions not handled by the application are rendered
   * by {@link ProblemWebExceptionHandler}, and {@code Problem}s or {@code ProblemDetail}s returned
   * by the application are post-processed by {@link ProblemHandlerResultHandler}.
   */
  @ConditionalOnProperty(name = "problem4j.exception-handling.mode", havingValue = "cooperate")
  @Configuration(proxyBeanMethods = false)
  static class CooperateModeConfiguration {

    /** Creates a new instance of this configuration. */
    CooperateModeConfiguration() {}

    /**
     * Nested configuration that warns at startup when Spring's own problem details are enabled with
     * {@code spring.webflux.problemdetails.enabled}. Spring Boot then registers its {@link
     * ResponseEntityExceptionHandler}, which renders standard Spring WebFlux exceptions before
     * {@link ProblemWebExceptionHandler}, as there is no Problem4J one in this mode.
     */
    @ConditionalOnBooleanProperty("spring.webflux.problemdetails.enabled")
    @Configuration(proxyBeanMethods = false)
    static class SpringProblemDetailsWarningConfiguration {

      private static final Logger log =
          LoggerFactory.getLogger(ProblemWebFluxAutoConfiguration.class);

      /** Creates a new instance of this configuration, logging the warning. */
      SpringProblemDetailsWarningConfiguration() {
        log.warn(
            "Spring's problem details (spring.webflux.problemdetails.enabled=true) override"
                + " Problem4J for standard Spring WebFlux exceptions in cooperate mode. Set"
                + " spring.webflux.problemdetails.enabled=false to let Problem4J handle them.");
      }
    }

    /**
     * Registers {@link ProblemWebExceptionHandler}, rendering exceptions known to Problem4J and not
     * handled by application exception handlers as {@code Problem} responses.
     */
    @ConditionalOnBooleanProperty(
        name = "problem4j.webflux.web-exception-handler.enabled",
        matchIfMissing = true)
    @ConditionalOnMissingBean(ProblemWebExceptionHandler.class)
    @Bean
    ProblemWebExceptionHandler problemWebExceptionHandler(
        ProblemOperations problemOperations,
        ObjectProvider<ServerCodecConfigurer> serverCodecConfigurer,
        List<? extends ProblemInspector> problemInspectors,
        ProblemProperties properties) {
      ProblemWebExceptionHandler handler =
          new ProblemWebExceptionHandler(
              problemOperations,
              serverCodecConfigurer.getIfAvailable(ServerCodecConfigurer::create).getWriters(),
              problemInspectors);
      handler.setUnknownExceptions(properties.getExceptionHandling().isUnknownExceptions());
      return handler;
    }

    /**
     * Registers {@link ProblemHandlerResultHandler}, applying post-processing to {@code Problem}s
     * and {@code ProblemDetail}s returned by controllers and exception handlers, before delegating
     * to Spring's {@link ResponseEntityResultHandler} or {@link ResponseBodyResultHandler}.
     */
    @ConditionalOnBooleanProperty(
        name = "problem4j.webflux.result-handler.enabled",
        matchIfMissing = true)
    @ConditionalOnMissingBean(ProblemHandlerResultHandler.class)
    @Bean
    ProblemHandlerResultHandler problemHandlerResultHandler(
        ProblemOperations problemOperations,
        ObjectProvider<ReactiveAdapterRegistry> adapterRegistry,
        ObjectProvider<ResponseEntityResultHandler> responseEntityResultHandler,
        ObjectProvider<ResponseBodyResultHandler> responseBodyResultHandler,
        ObjectProvider<ServerCodecConfigurer> serverCodecConfigurer) {
      return new ProblemHandlerResultHandler(
          problemOperations,
          adapterRegistry.getIfAvailable(ReactiveAdapterRegistry::getSharedInstance),
          () ->
              Stream.<HandlerResultHandler>concat(
                      responseEntityResultHandler.orderedStream(),
                      responseBodyResultHandler.orderedStream())
                  .toList(),
          serverCodecConfigurer.getIfAvailable(ServerCodecConfigurer::create).getWriters());
    }
  }
}
