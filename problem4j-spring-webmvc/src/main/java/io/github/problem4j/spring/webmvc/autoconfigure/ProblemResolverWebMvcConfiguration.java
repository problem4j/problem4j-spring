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

  /** Creates a new instance of this configuration. */
  ProblemResolverWebMvcConfiguration() {}

  @ConditionalOnClass(NoHandlerFoundException.class)
  @Configuration(proxyBeanMethods = false)
  static class NoHandlerFoundProblemConfiguration {

    /** Creates a new instance of this configuration. */
    NoHandlerFoundProblemConfiguration() {}

    @ConditionalOnMissingBean(NoHandlerFoundProblemResolver.class)
    @Bean
    NoHandlerFoundProblemResolver noHandlerFoundProblemResolver() {
      return new NoHandlerFoundProblemResolver();
    }
  }

  @ConditionalOnClass(NoResourceFoundException.class)
  @Configuration(proxyBeanMethods = false)
  static class NoResourceFoundProblemConfiguration {

    /** Creates a new instance of this configuration. */
    NoResourceFoundProblemConfiguration() {}

    @ConditionalOnMissingBean(NoResourceFoundProblemResolver.class)
    @Bean
    NoResourceFoundProblemResolver noResourceFoundProblemResolver() {
      return new NoResourceFoundProblemResolver();
    }
  }
}
