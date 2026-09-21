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

import com.fasterxml.jackson.databind.Module;
import io.github.problem4j.jackson2.ProblemModule;
import io.github.problem4j.spring.web.ProblemJsonMapperBuilderCustomizer;
import io.github.problem4j.spring.web.ProblemXmlMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.XmlMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;

/**
 * Spring Boot autoconfiguration registering Problem4J serialization support for Jackson mappers.
 *
 * <p>It does not require a web application, so {@code Problem} objects are serialized consistently
 * also in non-web applications (e.g. those consuming Problem responses through an HTTP client).
 *
 * @since 3.1.0
 */
@AutoConfiguration
@ConditionalOnBooleanProperty(name = "problem4j.enabled", matchIfMissing = true)
public class ProblemJacksonAutoConfiguration {

  /**
   * Creates a new instance of this autoconfiguration.
   *
   * @since 3.1.0
   */
  public ProblemJacksonAutoConfiguration() {}

  /** Configuration for JSON support in Problem serialization. */
  @ConditionalOnClass({JsonMapperBuilderCustomizer.class, JsonMapper.class})
  @Configuration(proxyBeanMethods = false)
  static class ProblemJsonMapperConfiguration {

    /** Creates a new instance of this configuration. */
    ProblemJsonMapperConfiguration() {}

    /**
     * Creates a {@link ProblemJsonMapperBuilderCustomizer} to add the {@code ProblemJacksonMixIn}
     * to the JSON mapper for consistent Problem serialization.
     *
     * @return a new ProblemJsonMapperBuilderCustomizer bean
     * @see io.github.problem4j.jackson3.ProblemJacksonMixIn
     */
    @ConditionalOnMissingBean(ProblemJsonMapperBuilderCustomizer.class)
    @Bean
    ProblemJsonMapperBuilderCustomizer problemJsonMapperBuilderCustomizer() {
      return new ProblemJsonMapperBuilderCustomizer();
    }
  }

  /** Configuration for XML support in Problem serialization. */
  @ConditionalOnClass({XmlMapperBuilderCustomizer.class, XmlMapper.class})
  @Configuration(proxyBeanMethods = false)
  static class ProblemXmlMapperConfiguration {

    /** Creates a new instance of this configuration. */
    ProblemXmlMapperConfiguration() {}

    /**
     * Creates a {@link ProblemXmlMapperBuilderCustomizer} to add the {@code ProblemJacksonMixIn} to
     * the XML mapper for consistent Problem serialization.
     *
     * @return a new ProblemXmlMapperBuilderCustomizer bean
     * @see io.github.problem4j.jackson3.ProblemJacksonMixIn
     */
    @ConditionalOnMissingBean(ProblemXmlMapperBuilderCustomizer.class)
    @Bean
    ProblemXmlMapperBuilderCustomizer problemXmlMapperBuilderCustomizer() {
      return new ProblemXmlMapperBuilderCustomizer();
    }
  }

  /**
   * If Jackson2 is present on the classpath, configures a {@link ProblemModule} bean. Note that
   * Spring Boot 4 does not include Jackson2 by default. To make it work, add {@code
   * spring-boot-jackson2} dependency manually.
   *
   * @see <a
   *     href="https://github.com/spring-projects/spring-boot/blob/v4.0.0/module/spring-boot-jackson2/src/main/java/org/springframework/boot/jackson2/autoconfigure/Jackson2AutoConfiguration.java#L86">
   *     <code>Jackson2AutoConfiguration</code></a>
   * @deprecated since 2.0.0 as Spring Boot team plans to remove Jackson 2 legacy support in 4.2.0
   */
  @ConditionalOnClass({ProblemModule.class, Module.class})
  @Configuration(proxyBeanMethods = false)
  @Deprecated(since = "2.0.0", forRemoval = true)
  static class ProblemJackson2ModuleConfiguration {

    /** Creates a new instance of this configuration. */
    ProblemJackson2ModuleConfiguration() {}

    /**
     * Provides a {@link ProblemModule} if none is defined.
     *
     * @return a new {@link ProblemModule}
     */
    @ConditionalOnMissingBean(ProblemModule.class)
    @Bean
    ProblemModule problemJackson2Module() {
      return new ProblemModule();
    }
  }
}
