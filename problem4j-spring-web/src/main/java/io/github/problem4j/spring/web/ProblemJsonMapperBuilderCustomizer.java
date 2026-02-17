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

package io.github.problem4j.spring.web;

import io.github.problem4j.core.Problem;
import io.github.problem4j.jackson3.ProblemJacksonMixIn;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Customizes Spring Boot's JSON ObjectMapper by registering a mix-in for the {@link Problem}
 * interface. Ensures that all Problem objects are serialized and deserialized consistently
 * according to {@link ProblemJacksonMixIn}.
 */
public class ProblemJsonMapperBuilderCustomizer implements JsonMapperBuilderCustomizer {

  /**
   * Adds the {@link ProblemJacksonMixIn} to the JSON mapper builder for proper serialization and
   * deserialization of {@link Problem} objects.
   *
   * @param builder the JSON mapper builder to customize
   */
  @Override
  public void customize(JsonMapper.Builder builder) {
    builder.addMixIn(Problem.class, ProblemJacksonMixIn.class);
  }
}
