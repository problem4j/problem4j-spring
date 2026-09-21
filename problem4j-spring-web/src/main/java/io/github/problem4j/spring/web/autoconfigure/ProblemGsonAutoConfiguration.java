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

import com.google.gson.Gson;
import io.github.problem4j.spring.web.ProblemGsonBuilderCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.gson.autoconfigure.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot autoconfiguration registering Problem4J serialization support for Gson.
 *
 * <p>It does not require a web application, so {@code Problem} objects are serialized consistently
 * also in non-web applications (e.g. those consuming Problem responses through an HTTP client).
 *
 * @since 3.1.0
 */
@AutoConfiguration
@ConditionalOnBooleanProperty(name = "problem4j.enabled", matchIfMissing = true)
@ConditionalOnClass({GsonBuilderCustomizer.class, Gson.class})
public class ProblemGsonAutoConfiguration {

  /**
   * Creates a new instance of this autoconfiguration.
   *
   * @since 3.1.0
   */
  public ProblemGsonAutoConfiguration() {}

  /**
   * Creates a {@link ProblemGsonBuilderCustomizer} to register the {@code
   * ProblemTypeAdapterFactory} in the Gson builder for consistent Problem serialization.
   *
   * @return a new ProblemGsonBuilderCustomizer bean
   * @see io.github.problem4j.gson.ProblemTypeAdapterFactory
   */
  @ConditionalOnMissingBean(ProblemGsonBuilderCustomizer.class)
  @Bean
  ProblemGsonBuilderCustomizer problemGsonBuilderCustomizer() {
    return new ProblemGsonBuilderCustomizer();
  }
}
