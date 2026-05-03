/*
 * Copyright 2025-2026 The Problem4J Authors
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
 * Settings used when building a {@code ProblemContext} for incoming requests.
 *
 * <p>Provides access to infrastructure configuration such as the HTTP header name that carries a
 * trace identifier. Implementations are typically backed by external configuration (e.g. Spring
 * Boot properties).
 *
 * @see io.github.problem4j.core.ProblemContext
 * @since 1.2.0
 */
public interface ProblemContextSettings {

  /**
   * Returns the name of the HTTP header that contains a trace / correlation ID.
   *
   * <p>The trace ID (if present) may be injected into generated Problem responses (e.g. via
   * placeholders in instance/type override templates) and echoed back to clients to aid in log
   * correlation and diagnostics.
   *
   * @return the tracing header name, or {@code null} if tracing is disabled / not configured
   * @since 1.2.0
   */
  @Nullable String getTracingHeaderName();
}
