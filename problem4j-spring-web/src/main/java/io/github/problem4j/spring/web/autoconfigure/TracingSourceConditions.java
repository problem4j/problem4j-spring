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

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Conditions used by WebMVC and WebFlux autoconfigurations to pick the {@code ProblemContext}
 * filter matching {@code problem4j.tracing.source}.
 *
 * @see ProblemProperties.Tracing#getSource()
 * @since 3.1.0
 */
public final class TracingSourceConditions {

  /**
   * Class that must be present on the classpath for Micrometer Tracing to be used.
   *
   * @since 3.1.0
   */
  public static final String MICROMETER_TRACING_CLASS =
      "io.micrometer.tracing.handler.TracingObservationHandler";

  /**
   * Message of the exception failing application startup if {@code problem4j.tracing.source} is set
   * to a source that cannot be used, as its required dependencies are not on the classpath.
   *
   * @since 3.1.0
   */
  public static final String UNAVAILABLE_SOURCE_MESSAGE =
      "problem4j.tracing.source is set to a source that is not available on the classpath"
          + " (micrometer requires io.micrometer:micrometer-tracing), add the missing dependency or"
          + " set problem4j.tracing.source to auto or simple";

  private static final String SOURCE_PROPERTY = "problem4j.tracing.source";

  private TracingSourceConditions() {}

  /**
   * Matches if {@code problem4j.tracing.source} is {@code micrometer} or {@code auto} (default),
   * and Micrometer Tracing is on the classpath.
   *
   * @since 3.1.0
   */
  public static final class OnMicrometerSource extends AnyNestedCondition {

    /**
     * Creates a new instance of this condition.
     *
     * @since 3.1.0
     */
    public OnMicrometerSource() {
      super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(name = SOURCE_PROPERTY, havingValue = "micrometer")
    @ConditionalOnClass(name = MICROMETER_TRACING_CLASS)
    static class MicrometerSource {}

    @ConditionalOnProperty(name = SOURCE_PROPERTY, havingValue = "auto", matchIfMissing = true)
    @ConditionalOnClass(name = MICROMETER_TRACING_CLASS)
    static class AutoSourceWithMicrometer {}
  }

  /**
   * Matches if {@code problem4j.tracing.source} is {@code simple}, or {@code auto} (default)
   * without Micrometer Tracing on the classpath.
   *
   * @since 3.1.0
   */
  public static final class OnSimpleSource extends AnyNestedCondition {

    /**
     * Creates a new instance of this condition.
     *
     * @since 3.1.0
     */
    public OnSimpleSource() {
      super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(name = SOURCE_PROPERTY, havingValue = "simple")
    static class SimpleSource {}

    @ConditionalOnProperty(name = SOURCE_PROPERTY, havingValue = "auto", matchIfMissing = true)
    @ConditionalOnMissingClass(MICROMETER_TRACING_CLASS)
    static class AutoSourceWithoutMicrometer {}
  }

  /**
   * Matches if {@code problem4j.tracing.source} is set to a source that cannot be used, as its
   * required dependencies are not on the classpath. This is the case for {@code micrometer} without
   * Micrometer Tracing.
   *
   * @since 3.1.0
   */
  public static final class OnUnavailableSource extends AnyNestedCondition {

    /**
     * Creates a new instance of this condition.
     *
     * @since 3.1.0
     */
    public OnUnavailableSource() {
      super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(name = SOURCE_PROPERTY, havingValue = "micrometer")
    @ConditionalOnMissingClass(MICROMETER_TRACING_CLASS)
    static class MicrometerSourceWithoutMicrometer {}
  }
}
