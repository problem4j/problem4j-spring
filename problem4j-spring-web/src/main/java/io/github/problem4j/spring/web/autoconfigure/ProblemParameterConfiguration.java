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

package io.github.problem4j.spring.web.autoconfigure;

import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.DefaultBindingResultSupport;
import io.github.problem4j.spring.web.parameter.DefaultMethodParameterSupport;
import io.github.problem4j.spring.web.parameter.DefaultMethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.method.MethodValidationResult;

/**
 * Configuration for parameter support components, such as method parameter name resolution and
 * binding / method validation result conversion.
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.ANY)
@Configuration(proxyBeanMethods = false)
class ProblemParameterConfiguration {

  /** Creates a new instance of this configuration. */
  ProblemParameterConfiguration() {}

  /**
   * Provides a default {@link MethodParameterSupport} bean if none is defined by the user.
   *
   * @return a new {@link DefaultMethodParameterSupport}
   */
  @ConditionalOnMissingBean(MethodParameterSupport.class)
  @Bean
  MethodParameterSupport problemMethodParameterSupport() {
    return new DefaultMethodParameterSupport();
  }

  @ConditionalOnClass(MethodValidationResult.class)
  @Configuration(proxyBeanMethods = false)
  static class ProblemMethodValidationConfiguration {

    /** Creates a new instance of this configuration. */
    ProblemMethodValidationConfiguration() {}

    /**
     * Provides a default {@link MethodValidationResultSupport} bean if none is defined by the user.
     *
     * @return a new {@link DefaultMethodValidationResultSupport}
     */
    @ConditionalOnMissingBean(MethodValidationResultSupport.class)
    @Bean
    MethodValidationResultSupport problemMethodValidationResultSupport(
        MethodParameterSupport methodParameterSupport) {
      return new DefaultMethodValidationResultSupport(methodParameterSupport);
    }
  }

  /**
   * Provides a default {@link BindingResultSupport} bean that uses field names for violations.
   *
   * @return a new {@link DefaultBindingResultSupport}
   */
  @ConditionalOnMissingBean(BindingResultSupport.class)
  @Bean
  BindingResultSupport problemBindingSupport() {
    return new DefaultBindingResultSupport();
  }
}
