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

package io.github.problem4j.spring.webflux.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for Problem Details WebFlux integration.
 *
 * <p>These properties can be set under the {@code problem4j.webflux.*} prefix.
 */
@ConfigurationProperties(prefix = "problem4j.webflux")
public class ProblemWebFluxProperties {

  /** Whether Problem4J integration with WebFlux is enabled. */
  private final boolean enabled;

  /** Configuration for {@code ExceptionWebFluxAdvice}. */
  private final ExceptionAdvice exceptionAdvice;

  /** Configuration for {@code ProblemExceptionWebFluxAdvice}. */
  private final ProblemExceptionAdvice problemExceptionAdvice;

  /** Configuration for {@code ProblemContextWebFluxFilter}. */
  private final ProblemContextFilter problemContextFilter;

  /** Configuration for {@code ProblemEnhancedWebFluxHandler} replacement. */
  private final ExceptionHandler exceptionHandler;

  /** Configuration for {@code ProblemErrorWebExceptionHandler} replacement. */
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
   */
  public ProblemWebFluxProperties(
      @DefaultValue("true") boolean enabled,
      ExceptionAdvice exceptionAdvice,
      ProblemExceptionAdvice problemExceptionAdvice,
      ProblemContextFilter problemContextFilter,
      ExceptionHandler exceptionHandler,
      ErrorWebExceptionHandler errorWebExceptionHandler) {
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
   * @see org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
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
   */
  public static class ExceptionAdvice {

    /** Default enabled value for {@code ExceptionAdvice} configuration group. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value as a string for {@code ExceptionAdvice} configuration group. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ExceptionAdvice createDefault() {
      return new ExceptionAdvice(DEFAULT_ENABLED);
    }

    /** Whether the {@code ExceptionWebFluxAdvice} bean should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ExceptionWebFluxAdvice} bean should be registered
     * @see io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice
     */
    public ExceptionAdvice(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ExceptionWebFluxAdvice} should be registered.
     *
     * @return {@code true} if exception advice is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice
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
   */
  public static class ProblemExceptionAdvice {

    /** Default enabled value for {@code ProblemExceptionAdvice} configuration group. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value as a string for {@code ProblemExceptionAdvice} configuration group. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ProblemExceptionAdvice createDefault() {
      return new ProblemExceptionAdvice(DEFAULT_ENABLED);
    }

    /** Whether the {@code ProblemExceptionWebFluxAdvice} bean should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ProblemExceptionWebFluxAdvice} bean should be registered
     * @see io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice
     */
    public ProblemExceptionAdvice(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemExceptionWebFluxAdvice} should be registered.
     *
     * @return {@code true} if the ProblemException advice is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice
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
   */
  public static class ProblemContextFilter {

    /** Default enabled value for {@code ProblemContextFilter} configuration group. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value as a string for {@code ProblemContextFilter} configuration group. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ProblemContextFilter createDefault() {
      return new ProblemContextFilter(DEFAULT_ENABLED);
    }

    /** Whether the {@code ProblemContextWebFluxFilter} bean should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ProblemContextWebFluxFilter} bean should be registered
     * @see io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter
     */
    public ProblemContextFilter(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemContextWebFluxFilter} should be registered.
     *
     * @return {@code true} if the context filter is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter
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
   */
  public static class ExceptionHandler {

    /** Default enabled value for {@code ExceptionHandler} configuration group. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value as a string for {@code ExceptionHandler} configuration group. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ExceptionHandler createDefault() {
      return new ExceptionHandler(DEFAULT_ENABLED);
    }

    /** Whether the {@code ProblemEnhancedWebFluxHandler} should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ResponseEntityExceptionHandler} should be replaced with
     *     {@code ProblemEnhancedWebFluxHandler}
     * @see io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler
     * @see org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
     */
    public ExceptionHandler(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemEnhancedWebFluxHandler} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ProblemEnhancedWebFluxHandler
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
   * @see org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
   * @see io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler
   * @see io.github.problem4j.spring.webflux.autoconfigure.ProblemErrorWebFluxConfiguration
   */
  public static class ErrorWebExceptionHandler {

    /** Default enabled value for {@code ErrorWebExceptionHandler} configuration group. */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Default enabled value as a string for {@code ErrorWebExceptionHandler} configuration group.
     */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ErrorWebExceptionHandler createDefault() {
      return new ErrorWebExceptionHandler(DEFAULT_ENABLED);
    }

    /** Whether the {@code ProblemErrorWebExceptionHandler} should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ErrorWebExceptionHandler} should be replaced with {@code
     *     ProblemErrorWebExceptionHandler}
     * @see io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler
     * @see org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
     */
    public ErrorWebExceptionHandler(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemErrorWebExceptionHandler} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler
     */
    public boolean isEnabled() {
      return enabled;
    }
  }
}
