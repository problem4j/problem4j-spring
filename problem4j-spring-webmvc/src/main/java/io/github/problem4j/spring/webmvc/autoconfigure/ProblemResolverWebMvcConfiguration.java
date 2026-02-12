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
package io.github.problem4j.spring.webmvc.autoconfigure;

import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.webmvc.resolver.NoHandlerFoundProblemResolver;
import io.github.problem4j.spring.webmvc.resolver.NoResourceFoundProblemResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Each nested configuration class is annotated with {@link ConditionalOnClass} to ensure that only
 * resolvers for classes present on the classpath are created. This design allows the library to
 * remain compatible previous versions.
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration(proxyBeanMethods = false)
class ProblemResolverWebMvcConfiguration {

  @ConditionalOnClass(NoHandlerFoundException.class)
  @Configuration(proxyBeanMethods = false)
  static class NoHandlerFoundProblemConfiguration {
    @ConditionalOnMissingBean(NoHandlerFoundProblemResolver.class)
    @Bean
    NoHandlerFoundProblemResolver noHandlerFoundProblemResolver(ProblemFormat problemFormat) {
      return new NoHandlerFoundProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(NoResourceFoundException.class)
  @Configuration(proxyBeanMethods = false)
  static class NoResourceFoundProblemConfiguration {
    @ConditionalOnMissingBean(NoResourceFoundProblemResolver.class)
    @Bean
    NoResourceFoundProblemResolver noResourceFoundProblemResolver(ProblemFormat problemFormat) {
      return new NoResourceFoundProblemResolver(problemFormat);
    }
  }
}
