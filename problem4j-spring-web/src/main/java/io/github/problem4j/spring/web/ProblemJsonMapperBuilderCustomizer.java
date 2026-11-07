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

import io.github.problem4j.core.Problem;
import io.github.problem4j.jackson3.ProblemJacksonMixIn;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Customizes Spring Boot's JSON ObjectMapper by registering a mix-in for the {@link Problem}
 * interface. Ensures that all Problem objects are serialized and deserialized consistently
 * according to {@link ProblemJacksonMixIn}.
 *
 * @since 2.1.0
 */
public class ProblemJsonMapperBuilderCustomizer implements JsonMapperBuilderCustomizer {

  /**
   * Creates a new {@link ProblemJsonMapperBuilderCustomizer}.
   *
   * @since 2.1.0
   */
  public ProblemJsonMapperBuilderCustomizer() {}

  /**
   * Adds the {@link ProblemJacksonMixIn} to the JSON mapper builder for proper serialization and
   * deserialization of {@link Problem} objects.
   *
   * @param builder the JSON mapper builder to customize
   * @since 2.1.0
   */
  @Override
  public void customize(JsonMapper.Builder builder) {
    builder.addMixIn(Problem.class, ProblemJacksonMixIn.class);
  }
}
