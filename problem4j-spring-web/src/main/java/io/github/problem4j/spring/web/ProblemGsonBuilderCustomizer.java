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

package io.github.problem4j.spring.web;

import com.google.gson.GsonBuilder;
import io.github.problem4j.core.Problem;
import io.github.problem4j.gson.ProblemTypeAdapterFactory;
import org.springframework.boot.gson.autoconfigure.GsonBuilderCustomizer;

/**
 * Customizes Spring Boot's {@code Gson} instance by registering a {@link
 * ProblemTypeAdapterFactory}. Ensures that all {@link Problem} objects are serialized and
 * deserialized consistently according to the RFC 7807 representation.
 *
 * @since 3.1.0
 */
public class ProblemGsonBuilderCustomizer implements GsonBuilderCustomizer {

  /**
   * Creates a new {@link ProblemGsonBuilderCustomizer}.
   *
   * @since 3.1.0
   */
  public ProblemGsonBuilderCustomizer() {}

  /**
   * Registers the {@link ProblemTypeAdapterFactory} in the Gson builder for proper serialization
   * and deserialization of {@link Problem} objects.
   *
   * @param gsonBuilder the Gson builder to customize
   * @since 3.1.0
   */
  @Override
  public void customize(GsonBuilder gsonBuilder) {
    gsonBuilder.registerTypeAdapterFactory(new ProblemTypeAdapterFactory());
  }
}
