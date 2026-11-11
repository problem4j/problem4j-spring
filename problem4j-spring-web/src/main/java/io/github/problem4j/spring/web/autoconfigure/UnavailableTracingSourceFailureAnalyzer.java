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

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Reports {@link UnavailableTracingSourceException} as a startup failure description, instead of a
 * stack trace of the bean creation failure that wraps it.
 */
final class UnavailableTracingSourceFailureAnalyzer
    extends AbstractFailureAnalyzer<UnavailableTracingSourceException> {

  static final String DESCRIPTION =
      "problem4j.tracing-source is set to micrometer, but io.micrometer:micrometer-tracing is not"
          + " on the classpath.";

  static final String ACTION =
      "Add io.micrometer:micrometer-tracing with a tracing bridge (e.g."
          + " io.micrometer:micrometer-tracing-bridge-otel) to the classpath, or set"
          + " problem4j.tracing-source to auto or simple.";

  UnavailableTracingSourceFailureAnalyzer() {}

  @Override
  protected FailureAnalysis analyze(
      Throwable rootFailure, UnavailableTracingSourceException cause) {
    return new FailureAnalysis(DESCRIPTION, ACTION, cause);
  }
}
