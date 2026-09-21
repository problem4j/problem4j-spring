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

import io.github.problem4j.spring.web.CachingProblemResolverStore;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties.DuplicateResolverPolicy;
import io.github.problem4j.spring.web.config.ProblemHandlerMethodProcessor;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.DefaultBindingResultSupport;
import io.github.problem4j.spring.web.parameter.DefaultMethodParameterSupport;
import io.github.problem4j.spring.web.parameter.DefaultMethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.validation.method.MethodValidationResult;

/**
 * Spring Boot autoconfiguration for Problem4J web integration.
 *
 * <p>This class wires beans for producing standardized {@code Problem} responses from Spring
 * controllers, such as {@link ProblemResolver} implementations for Spring's web exceptions, {@link
 * ProblemResolverStore} aggregating them and parameter support components (method parameter name
 * resolution and binding / method validation result conversion). Generic beans are wired by {@link
 * ProblemAutoConfiguration}.
 *
 * @since 3.1.0
 */
@AutoConfiguration(after = ProblemAutoConfiguration.class)
@EnableConfigurationProperties({ProblemProperties.class})
@ConditionalOnBooleanProperty(name = "problem4j.enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.ANY)
@Import({ProblemResolverConfiguration.class})
public class ProblemWebAutoConfiguration {

  /**
   * Creates a new instance of this autoconfiguration.
   *
   * @since 3.1.0
   */
  public ProblemWebAutoConfiguration() {}

  /**
   * Provides a {@link ProblemResolverStore} that aggregates all {@link ProblemResolver}
   * implementations.
   *
   * @param problemResolvers all available {@link ProblemResolver} declared as components, by bean
   *     name
   * @param properties the configuration properties, used to decide whether resolver lookups are
   *     cached and how resolvers with equal order are handled
   * @return {@link DefaultProblemResolverStore}, wrapped in {@link CachingProblemResolverStore} if
   *     caching is enabled
   * @throws IllegalStateException if {@link ProblemProperties#getDuplicateResolverPolicy()} is
   *     {@link DuplicateResolverPolicy#FAIL} and resolvers supporting the same exception class have
   *     equal order
   */
  @ConditionalOnMissingBean(ProblemResolverStore.class)
  @Bean
  ProblemResolverStore problemResolverStore(
      Map<String, ? extends ProblemResolver> problemResolvers, ProblemProperties properties) {
    if (properties.getDuplicateResolverPolicy() == DuplicateResolverPolicy.FAIL) {
      ResolverValidator.validate(problemResolvers);
    }

    ProblemResolverStore problemResolverStore =
        new DefaultProblemResolverStore(new ArrayList<>(problemResolvers.values()));

    if (properties.getResolverCaching().isEnabled()) {
      problemResolverStore = new CachingProblemResolverStore(problemResolverStore);
    }

    return problemResolverStore;
  }

  /**
   * Provides the {@link ProblemHandlerMethodProcessor} registering methods annotated with {@code
   * ProblemHandler} as {@link ProblemResolver} beans.
   *
   * @return a new {@link ProblemHandlerMethodProcessor}
   */
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  @ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
  @Bean
  static ProblemHandlerMethodProcessor problemHandlerMethodProcessor() {
    return new ProblemHandlerMethodProcessor();
  }

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

  /**
   * Configuration for method validation support, active when Spring's method validation is present.
   */
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
}
