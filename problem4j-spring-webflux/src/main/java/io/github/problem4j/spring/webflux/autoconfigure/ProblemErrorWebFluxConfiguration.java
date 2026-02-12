/*
 * Copyright (c) 2025-2026 Damian Malczewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.problem4j.spring.webflux.autoconfigure;

import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
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
 * <p>This configuration replaces Spring Boot’s default WebFlux error handler defined in {@code
 * ErrorWebFluxAutoConfiguration}.
 *
 * @see org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
 */
@ConditionalOnProperty(
    name = "problem4j.webflux.error-web-exception-handler.enabled",
    matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Configuration(proxyBeanMethods = false)
class ProblemErrorWebFluxConfiguration {

  private final ServerProperties serverProperties;

  ProblemErrorWebFluxConfiguration(ServerProperties serverProperties) {
    this.serverProperties = serverProperties;
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
   * @see org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
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
            this.serverProperties.getError(),
            applicationContext);
    exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
    exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
    exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
    return exceptionHandler;
  }
}
