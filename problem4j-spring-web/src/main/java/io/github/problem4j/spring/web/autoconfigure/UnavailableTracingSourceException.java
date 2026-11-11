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

import static io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.UNAVAILABLE_SOURCE_MESSAGE;

import java.io.Serial;

/**
 * Thrown on application startup if {@code problem4j.tracing-source} is set to a source that cannot
 * be used, as its required dependencies are not on the classpath (e.g. {@code micrometer} without
 * {@code io.micrometer:micrometer-tracing}).
 *
 * <p>Reported as a startup failure description by {@code UnavailableTracingSourceFailureAnalyzer}.
 *
 * @see TracingSourceConditions.OnUnavailableSource
 * @since 3.1.0
 */
public class UnavailableTracingSourceException extends IllegalStateException {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@code UnavailableTracingSourceException}.
   *
   * @since 3.1.0
   */
  public UnavailableTracingSourceException() {
    super(UNAVAILABLE_SOURCE_MESSAGE);
  }
}
