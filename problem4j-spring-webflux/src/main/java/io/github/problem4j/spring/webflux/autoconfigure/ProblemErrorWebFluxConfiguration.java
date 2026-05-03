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

package io.github.problem4j.spring.webflux.autoconfigure;

import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

/**
 * Configures a custom {@link ErrorWebExceptionHandler} that produces {@code
 * application/problem+json} responses according to RFC 7807.
 *
 * <p>This configuration replaces Spring Boot's default WebFlux error handler defined in {@code
 * ErrorWebFluxAutoConfiguration}.
 *
 * @see org.springframework.boot.webflux.autoconfigure.error.ErrorWebFluxAutoConfiguration
 */
@ConditionalOnBooleanProperty(
    name = "problem4j.webflux.error-web-exception-handler.enabled",
    matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Configuration(proxyBeanMethods = false)
class ProblemErrorWebFluxConfiguration {

  private final WebProperties webProperties;

  ProblemErrorWebFluxConfiguration(WebProperties webProperties) {
    this.webProperties = webProperties;
  }

  /**
   * Registers a default {@link ErrorAttributes} bean if none is already defined.
   *
   * <p>Provides the error details used by the {@link ProblemErrorWebExceptionHandler} to build
   * {@code application/problem+json} responses.
   *
   * @return a default {@link DefaultErrorAttributes} instance
   */
  @ConditionalOnMissingBean(ErrorAttributes.class)
  @Bean
  ErrorAttributes problemErrorAttributes() {
    return new DefaultErrorAttributes();
  }

  /**
   * Overrides {@link ErrorWebExceptionHandler} defined in {@code ErrorWebFluxAutoConfiguration} to
   * return problem JSONs. Must be declared with {@code @Order(-2)}, as default one declared in
   * {@code ErrorWebFluxAutoConfiguration} has {@code @Order(-1)} (the lower value wins).
   *
   * @see org.springframework.boot.webflux.autoconfigure.error.ErrorWebFluxAutoConfiguration
   */
  @Order(-2)
  @ConditionalOnMissingBean(ErrorWebExceptionHandler.class)
  @Bean
  ErrorWebExceptionHandler problemErrorWebExceptionHandler(
      ProblemPostProcessor problemPostProcessor,
      ErrorAttributes errorAttributes,
      WebProperties webProperties,
      ObjectProvider<ViewResolver> viewResolvers,
      ServerCodecConfigurer serverCodecConfigurer,
      ApplicationContext applicationContext) {
    ProblemErrorWebExceptionHandler exceptionHandler =
        new ProblemErrorWebExceptionHandler(
            problemPostProcessor,
            errorAttributes,
            webProperties.getResources(),
            this.webProperties.getError(),
            applicationContext);
    exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
    exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
    exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
    return exceptionHandler;
  }
}
