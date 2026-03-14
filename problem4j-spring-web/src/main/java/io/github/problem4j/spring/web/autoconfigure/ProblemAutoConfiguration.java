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

package io.github.problem4j.spring.web.autoconfigure;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.jackson2.ProblemModule;
import io.github.problem4j.spring.web.CachingProblemResolverStore;
import io.github.problem4j.spring.web.DefaultProblemFormat;
import io.github.problem4j.spring.web.DefaultProblemPostProcessor;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot autoconfiguration for Problem4J integration.
 *
 * <p>This class wires all necessary beans for producing standardized {@code Problem} responses from
 * Spring controllers. It includes:
 *
 * <p>Beans are conditional:
 *
 * <ul>
 *   <li>{@link ConditionalOnMissingBean} ensures user-defined beans override defaults.
 *   <li>{@link ConditionalOnClass} ensures compatibility with optional framework classes.
 * </ul>
 *
 * @see io.github.problem4j.core.Problem
 */
@AutoConfiguration
@EnableConfigurationProperties({ProblemProperties.class})
@ConditionalOnProperty(name = "problem4j.enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.ANY)
@Import({ProblemParameterConfiguration.class, ProblemResolverConfiguration.class})
public class ProblemAutoConfiguration {

  /**
   * Provides a {@link ProblemMapper} if none is defined.
   *
   * @return a new {@link ProblemMapper}
   */
  @ConditionalOnMissingBean(ProblemMapper.class)
  @Bean
  ProblemMapper problemMapper() {
    return ProblemMapper.create();
  }

  /**
   * Provides a {@link ProblemFormat} based on {@link ProblemProperties} if none is defined.
   *
   * @param properties the configuration properties
   * @return a new {@link DefaultProblemFormat}
   */
  @ConditionalOnMissingBean(ProblemFormat.class)
  @Bean
  ProblemFormat problemFormat(ProblemProperties properties) {
    return new DefaultProblemFormat(properties.getDetailFormat());
  }

  /**
   * Provides a {@link ProblemPostProcessor} that applies post-processing rules to {@code Problem}
   * instances before they are returned in HTTP responses.
   *
   * <p>The default implementation, {@link DefaultProblemPostProcessor}, supports configurable
   * overrides for problem fields such as {@code type} and {@code instance}, based on the properties
   * defined in {@link ProblemProperties}. These overrides may include runtime placeholders such as:
   *
   * <ul>
   *   <li>{@code {problem.type}} - replaced with the original problem’s type URI
   *   <li>{@code {problem.instance}} - replaced with the original problem’s instance URI
   *   <li>{@code {context.traceId}} - replaced with the current trace identifier, if available
   * </ul>
   *
   * <p>This allows enriching or normalizing problem responses without modifying the original
   * exception mapping logic.
   *
   * @param properties the configuration properties containing override templates and settings
   * @return a new {@link DefaultProblemPostProcessor} instance
   * @see io.github.problem4j.core.Problem
   */
  @ConditionalOnMissingBean(ProblemPostProcessor.class)
  @Bean
  ProblemPostProcessor problemPostProcessor(ProblemProperties properties) {
    return new DefaultProblemPostProcessor(properties);
  }

  /**
   * Provides a {@link ProblemResolverStore} that aggregates all {@link ProblemResolver}
   * implementations.
   *
   * @param problemResolvers all available {@link ProblemResolver} declared as components
   * @return {@link DefaultProblemResolverStore}, wrapped in {@link CachingProblemResolverStore} if
   *     caching is enabled
   */
  @ConditionalOnMissingBean(ProblemResolverStore.class)
  @Bean
  ProblemResolverStore problemResolverStore(
      List<ProblemResolver> problemResolvers, ProblemProperties properties) {
    ProblemResolverStore problemResolverStore = new DefaultProblemResolverStore(problemResolvers);

    if (properties.getResolverCaching().isEnabled()) {
      problemResolverStore =
          new CachingProblemResolverStore(
              problemResolverStore, properties.getResolverCaching().getMaxCacheSize());
    }

    return problemResolverStore;
  }

  /**
   * Provides a {@link TypeNameMapper} that maps Java types to string names for inclusion in problem
   * responses. The default implementation, {@link SimpleTypeNameMapper} supports simple Java types,
   * such as primitives, numbers, strings, arrays and lists.
   *
   * @return a new {@link SimpleTypeNameMapper}
   */
  @ConditionalOnMissingBean(TypeNameMapper.class)
  @Bean
  TypeNameMapper problemTypeNameMapper() {
    return new SimpleTypeNameMapper();
  }

  @ConditionalOnClass({ProblemModule.class, SimpleModule.class})
  @Configuration(proxyBeanMethods = false)
  static class ProblemModuleConfiguration {

    /**
     * Provides a {@link ProblemModule} if none is defined.
     *
     * @return a new {@link ProblemModule}
     */
    @ConditionalOnMissingBean(ProblemModule.class)
    @Bean
    ProblemModule problemJacksonModule() {
      return new ProblemModule();
    }
  }
}
