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

import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.webmvc.ProblemErrorController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures MVC error handling to return {@code application/problem+json} responses according to
 * RFC 7807.
 *
 * <p>This setup replaces Spring Boot's default error controller {@code ErrorMvcAutoConfiguration}
 * with {@link ProblemErrorController}, which renders {@code Problem} objects instead of HTML or
 * plain JSON errors.
 *
 * @see io.github.problem4j.core.Problem
 * @see org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration
 */
@ConditionalOnBooleanProperty(
    name = "problem4j.webmvc.error-controller.enabled",
    matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ErrorController.class)
@Configuration(proxyBeanMethods = false)
class ProblemErrorMvcConfiguration {

  /** Creates a new instance of this configuration. */
  ProblemErrorMvcConfiguration() {}

  /**
   * Registers a default {@link ErrorAttributes} bean if none exists.
   *
   * <p>Used to expose error details to the {@link ProblemErrorController}.
   *
   * @return a default {@link DefaultErrorAttributes} instance
   */
  @ConditionalOnMissingBean(ErrorAttributes.class)
  @Bean
  ErrorAttributes problemErrorAttributes() {
    return new DefaultErrorAttributes();
  }

  /**
   * Registers a custom {@link ErrorController} that renders Problem JSON responses.
   *
   * <p>Replaces the default error controller when no other implementation is present.
   *
   * @param errorAttributes provides error information for requests
   * @return a new {@link ProblemErrorController} instance
   * @see org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration
   */
  @ConditionalOnMissingBean(ErrorController.class)
  @Bean
  ErrorController problemErrorController(
      ProblemPostProcessor problemPostProcessor, ErrorAttributes errorAttributes) {
    return new ProblemErrorController(problemPostProcessor, errorAttributes);
  }
}
