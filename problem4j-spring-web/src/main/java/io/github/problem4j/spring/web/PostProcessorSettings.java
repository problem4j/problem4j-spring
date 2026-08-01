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

import org.jspecify.annotations.Nullable;

/**
 * Defines configuration settings used by {@link ProblemPostProcessor} implementations to control
 * how problem responses are modified before being returned to the client.
 *
 * <p>Implementations of this interface typically provide values from application configuration (for
 * example, {@code problem4j.type-override}, {@code problem4j.type-override} and {@code
 * problem4j.instance-override}) and may include runtime placeholders that are resolved during
 * post-processing.
 *
 * <p>These settings allow applications to dynamically customize problem types, titles, and
 * instances to match organizational or tracing conventions.
 *
 * @since 1.2.0
 */
public interface PostProcessorSettings {

  /**
   * Returns the configured override template for the {@code type} field of a problem.
   *
   * <p>The value may include placeholders such as {@code {problem.type}} or {@code
   * {context.<key>}}, which will be replaced at runtime.
   *
   * @return the configured type override template, or {@code null} if not set
   * @since 1.2.0
   */
  @Nullable String getTypeOverride();

  /**
   * Returns the configured override template for the {@code title} field of a problem.
   *
   * <p>The value may include placeholders such as {@code {problem.title}} or {@code
   * {context.<key>}}, which will be replaced at runtime.
   *
   * @return the configured title override template, or {@code null} if not set
   * @since 3.0.0
   */
  @Nullable String getTitleOverride();

  /**
   * Returns the configured override template for the {@code instance} field of a problem.
   *
   * <p>The value may include placeholders such as {@code {problem.instance}} or {@code
   * {context.<key>}}, which will be replaced at runtime.
   *
   * @return the configured instance override template, or {@code null} if not set
   * @since 1.2.0
   */
  @Nullable String getInstanceOverride();
}
