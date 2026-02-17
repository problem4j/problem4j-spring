/*
 * Copyright (c) 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.webmvc.autoconfigure;

import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.webmvc.ProblemErrorController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures MVC error handling to return {@code application/problem+json} responses according to
 * RFC 7807.
 *
 * <p>This setup replaces Spring Boot’s default error controller {@code ErrorMvcAutoConfiguration}
 * with {@link ProblemErrorController}, which renders {@code Problem} objects instead of HTML or
 * plain JSON errors.
 *
 * @see io.github.problem4j.core.Problem
 * @see org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
 */
@ConditionalOnProperty(name = "problem4j.webmvc.error-controller.enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ErrorController.class)
@Configuration(proxyBeanMethods = false)
class ProblemErrorMvcConfiguration {

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
   * @see org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
   */
  @ConditionalOnMissingBean(ErrorController.class)
  @Bean
  ErrorController problemErrorController(
      ProblemPostProcessor problemPostProcessor, ErrorAttributes errorAttributes) {
    return new ProblemErrorController(problemPostProcessor, errorAttributes);
  }
}
