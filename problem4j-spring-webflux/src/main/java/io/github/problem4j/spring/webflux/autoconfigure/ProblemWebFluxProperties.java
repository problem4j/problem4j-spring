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

package io.github.problem4j.spring.webflux.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for Problem Details WebFlux integration.
 *
 * <p>These properties can be set under the {@code problem4j.webflux.*} prefix.
 *
 * @since 1.2.0
 */
@ConfigurationProperties(prefix = "problem4j.webflux")
public class ProblemWebFluxProperties {

  /**
   * Whether Problem4J integration with WebFlux is enabled.
   *
   * @since 1.2.0
   */
  private final boolean enabled;

  /**
   * Configuration for {@code ExceptionWebFluxAdvice}.
   *
   * @since 1.2.0
   */
  private final ExceptionAdvice exceptionAdvice;

  /**
   * Configuration for {@code ProblemExceptionWebFluxAdvice}.
   *
   * @since 1.2.0
   */
  private final ProblemExceptionAdvice problemExceptionAdvice;

  /**
   * Configuration for {@code ProblemContextWebFluxFilter}.
   *
   * @since 1.2.0
   */
  private final ProblemContextFilter problemContextFilter;

  /**
   * Configuration for {@code ProblemEnhancedWebFluxHandler} replacement.
   *
   * @since 1.2.0
   */
  private final ExceptionHandler exceptionHandler;

  /**
   * Configuration for {@code ProblemErrorWebExceptionHandler} replacement.
   *
   * @since 1.2.0
   */
  private final ErrorWebExceptionHandler errorWebExceptionHandler;

  /**
   * Creates a new instance.
   *
   * @param enabled whether Problem4J integration with WebFlux is enabled
   * @param exceptionAdvice configuration for {@code ExceptionWebFluxAdvice}
   * @param problemExceptionAdvice configuration for {@code ProblemExceptionWebFluxAdvice}
   * @param problemContextFilter configuration for {@code ProblemContextWebFluxFilter}
   * @param exceptionHandler configuration for {@code ProblemEnhancedWebFluxHandler}
   * @param errorWebExceptionHandler configuration for {@code ProblemErrorWebExceptionHandler}
   * @see io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice
   * @see io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice
   * @see io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter
   * @see io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler
   * @see io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler
   * @since 1.2.0
   */
  public ProblemWebFluxProperties(
      @DefaultValue("true") boolean enabled,
      @Nullable ExceptionAdvice exceptionAdvice,
      @Nullable ProblemExceptionAdvice problemExceptionAdvice,
      @Nullable ProblemContextFilter problemContextFilter,
      @Nullable ExceptionHandler exceptionHandler,
      @Nullable ErrorWebExceptionHandler errorWebExceptionHandler) {
    this.enabled = enabled;
    this.exceptionAdvice =
        exceptionAdvice != null ? exceptionAdvice : ExceptionAdvice.createDefault();
    this.problemExceptionAdvice =
        problemExceptionAdvice != null
            ? problemExceptionAdvice
            : ProblemExceptionAdvice.createDefault();
    this.problemContextFilter =
        problemContextFilter != null ? problemContextFilter : ProblemContextFilter.createDefault();
    this.exceptionHandler =
        exceptionHandler != null ? exceptionHandler : ExceptionHandler.createDefault();
    this.errorWebExceptionHandler =
        errorWebExceptionHandler != null
            ? errorWebExceptionHandler
            : ErrorWebExceptionHandler.createDefault();
  }

  /**
   * Indicates whether Problem4J integration with WebFlux is currently enabled.
   *
   * @return {@code true} if problem WebFlux is enabled; {@code false} otherwise
   * @since 1.2.0
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns configuration for {@code ExceptionWebFluxAdvice}, which handles general exceptions and
   * converts them to {@code Problem} responses.
   *
   * @return the configuration for exception advice
   * @see io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice
   * @since 1.2.0
   */
  public ExceptionAdvice getExceptionAdvice() {
    return exceptionAdvice;
  }

  /**
   * Returns configuration for {@code ProblemExceptionWebFluxAdvice}, which handles exceptions of
   * type {@code ProblemException} and converts them to Problem responses.
   *
   * @return the configuration for problem exception advice
   * @see io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice
   * @since 1.2.0
   */
  public ProblemExceptionAdvice getProblemExceptionAdvice() {
    return problemExceptionAdvice;
  }

  /**
   * Returns configuration for {@code ProblemContextWebFluxFilter}, which enriches request handling
   * with {@code ProblemContext}.
   *
   * @return the configuration for the {@code ProblemContextWebFluxFilter}
   * @see io.github.problem4j.core.ProblemContext
   * @see io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter
   * @since 1.2.0
   */
  public ProblemContextFilter getProblemContextFilter() {
    return problemContextFilter;
  }

  /**
   * Returns configuration for {@code ProblemEnhancedWebFluxHandler} replacement, which allows
   * Problem4J to take control of exception handling normally performed by Spring's {@code
   * ResponseEntityExceptionHandler}.
   *
   * @return the configuration for the overwritten exception handler
   * @see io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler
   * @see org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
   * @since 1.2.0
   */
  public ExceptionHandler getExceptionHandler() {
    return exceptionHandler;
  }

  /**
   * Returns configuration for {@code ProblemErrorWebExceptionHandler} replacement, which allows
   * Problem4J to take control of exception handling normally performed by Spring's {@code
   * ErrorWebExceptionHandler}.
   *
   * @return the configuration for the overwritten error handler
   * @see io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler
   * @see org.springframework.boot.webflux.error.ErrorWebExceptionHandler
   * @since 1.2.0
   */
  public ErrorWebExceptionHandler getErrorWebExceptionHandler() {
    return errorWebExceptionHandler;
  }

  /**
   * Configuration group for {@code ExceptionWebFluxAdvice}.
   *
   * <p>Controlled by the property {@code problem4j.webflux.exception-advice.enabled}.
   *
   * @see io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice
   * @since 1.2.0
   */
  public static class ExceptionAdvice {

    /**
     * Default enabled value for {@code ExceptionAdvice} configuration group.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Default enabled value as a string for {@code ExceptionAdvice} configuration group.
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ExceptionAdvice createDefault() {
      return new ExceptionAdvice(DEFAULT_ENABLED);
    }

    /**
     * Whether the {@code ExceptionWebFluxAdvice} bean should be registered.
     *
     * @since 1.2.0
     */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ExceptionWebFluxAdvice} bean should be registered
     * @see io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice
     * @since 1.2.0
     */
    public ExceptionAdvice(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ExceptionWebFluxAdvice} should be registered.
     *
     * @return {@code true} if exception advice is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemExceptionWebFluxAdvice}.
   *
   * <p>Controlled by the property {@code problem4j.webflux.problem-exception-advice.enabled}.
   *
   * @see io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice
   * @since 1.2.0
   */
  public static class ProblemExceptionAdvice {

    /**
     * Default enabled value for {@code ProblemExceptionAdvice} configuration group.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Default enabled value as a string for {@code ProblemExceptionAdvice} configuration group.
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ProblemExceptionAdvice createDefault() {
      return new ProblemExceptionAdvice(DEFAULT_ENABLED);
    }

    /**
     * Whether the {@code ProblemExceptionWebFluxAdvice} bean should be registered.
     *
     * @since 1.2.0
     */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ProblemExceptionWebFluxAdvice} bean should be registered
     * @see io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice
     * @since 1.2.0
     */
    public ProblemExceptionAdvice(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemExceptionWebFluxAdvice} should be registered.
     *
     * @return {@code true} if the ProblemException advice is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemContextWebFluxFilter}.
   *
   * <p>Controlled by the property {@code problem4j.webflux.problem-context-filter.enabled}.
   *
   * @see io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter
   * @since 1.2.0
   */
  public static class ProblemContextFilter {

    /**
     * Default enabled value for {@code ProblemContextFilter} configuration group.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Default enabled value as a string for {@code ProblemContextFilter} configuration group.
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ProblemContextFilter createDefault() {
      return new ProblemContextFilter(DEFAULT_ENABLED);
    }

    /**
     * Whether the {@code ProblemContextWebFluxFilter} bean should be registered.
     *
     * @since 1.2.0
     */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ProblemContextWebFluxFilter} bean should be registered
     * @see io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter
     * @since 1.2.0
     */
    public ProblemContextFilter(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemContextWebFluxFilter} should be registered.
     *
     * @return {@code true} if the context filter is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemEnhancedWebFluxHandler} override.
   *
   * <p>Controlled by the property {@code problem4j.webflux.exception-handler.enabled}.
   *
   * @see io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler
   * @since 1.2.0
   */
  public static class ExceptionHandler {

    /**
     * Default enabled value for {@code ExceptionHandler} configuration group.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Default enabled value as a string for {@code ExceptionHandler} configuration group.
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ExceptionHandler createDefault() {
      return new ExceptionHandler(DEFAULT_ENABLED);
    }

    /**
     * Whether the {@code ProblemEnhancedWebFluxHandler} should be registered.
     *
     * @since 1.2.0
     */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ResponseEntityExceptionHandler} should be replaced with
     *     {@code ProblemEnhancedWebFluxHandler}
     * @see io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler
     * @see org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
     * @since 1.2.0
     */
    public ExceptionHandler(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemEnhancedWebFluxHandler} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemErrorWebExceptionHandler} override.
   *
   * <p>Controlled by the property {@code problem4j.webflux.error-web-exception-handler.enabled}.
   *
   * @see org.springframework.boot.webflux.error.ErrorWebExceptionHandler
   * @see io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler
   * @see io.github.problem4j.spring.webflux.autoconfigure.ProblemErrorWebFluxConfiguration
   * @since 1.2.0
   */
  public static class ErrorWebExceptionHandler {

    /**
     * Default enabled value for {@code ErrorWebExceptionHandler} configuration group.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Default enabled value as a string for {@code ErrorWebExceptionHandler} configuration group.
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ErrorWebExceptionHandler createDefault() {
      return new ErrorWebExceptionHandler(DEFAULT_ENABLED);
    }

    /**
     * Whether the {@code ProblemErrorWebExceptionHandler} should be registered.
     *
     * @since 1.2.0
     */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ErrorWebExceptionHandler} should be replaced with {@code
     *     ProblemErrorWebExceptionHandler}
     * @see io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler
     * @see org.springframework.boot.webflux.error.ErrorWebExceptionHandler
     * @since 1.2.0
     */
    public ErrorWebExceptionHandler(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemErrorWebExceptionHandler} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }
  }
}
