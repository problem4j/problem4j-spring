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

package io.github.problem4j.spring.webmvc.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Problem Details WebMVC integration.
 *
 * <p>These properties can be set under the {@code problem4j.webmvc.*} prefix.
 *
 * @since 1.2.0
 */
@ConfigurationProperties(prefix = "problem4j.webmvc")
public class ProblemWebMvcProperties {

  /** Whether Problem4J integration with WebMVC is enabled. */
  private boolean enabled = true;

  /** Configuration for {@code ExceptionWebMvcAdvice}. */
  private ExceptionAdvice exceptionAdvice = new ExceptionAdvice();

  /** Configuration for {@code ProblemExceptionWebMvcAdvice}. */
  private ProblemExceptionAdvice problemExceptionAdvice = new ProblemExceptionAdvice();

  /** Configuration for {@code ProblemContextWebMvcFilter}. */
  private ProblemContextFilter problemContextFilter = new ProblemContextFilter();

  /** Configuration for {@code ProblemEnhancedWebMvcHandler} replacement. */
  private ExceptionHandler exceptionHandler = new ExceptionHandler();

  /** Configuration for {@code ProblemErrorController} replacement. */
  private ErrorController errorController = new ErrorController();

  /**
   * Creates a new instance of {@link ProblemWebMvcProperties} with default values.
   *
   * @since 3.1.0
   */
  public ProblemWebMvcProperties() {}

  /**
   * Creates a new instance.
   *
   * @param enabled whether Problem4J integration with WebMVC is enabled
   * @param exceptionAdvice configuration for {@code ExceptionWebMvcAdvice}
   * @param problemExceptionAdvice configuration for {@code ProblemExceptionWebMvcAdvice}
   * @param problemContextFilter configuration for {@code ProblemContextWebMvcFilter}
   * @param exceptionHandler configuration for {@code ProblemEnhancedWebMvcHandler}
   * @param errorController configuration for {@code ProblemErrorController}
   * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
   * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
   * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
   * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
   * @see io.github.problem4j.spring.webmvc.ProblemErrorController
   * @since 1.2.0
   * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
   *     together with the corresponding setters instead
   */
  @Deprecated(since = "3.1.0", forRemoval = true)
  public ProblemWebMvcProperties(
      boolean enabled,
      @Nullable ExceptionAdvice exceptionAdvice,
      @Nullable ProblemExceptionAdvice problemExceptionAdvice,
      @Nullable ProblemContextFilter problemContextFilter,
      @Nullable ExceptionHandler exceptionHandler,
      @Nullable ErrorController errorController) {
    this.enabled = enabled;
    if (exceptionAdvice != null) {
      this.exceptionAdvice = exceptionAdvice;
    }
    if (problemExceptionAdvice != null) {
      this.problemExceptionAdvice = problemExceptionAdvice;
    }
    if (problemContextFilter != null) {
      this.problemContextFilter = problemContextFilter;
    }
    if (exceptionHandler != null) {
      this.exceptionHandler = exceptionHandler;
    }
    if (errorController != null) {
      this.errorController = errorController;
    }
  }

  /**
   * Indicates whether Problem4J integration with WebMVC is currently enabled.
   *
   * @return {@code true} if problem WebMVC is enabled; {@code false} otherwise
   * @since 1.2.0
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets whether Problem4J integration with WebMVC is enabled.
   *
   * @param enabled whether Problem4J integration with WebMVC is enabled
   * @since 3.1.0
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns configuration for {@code ExceptionWebMvcAdvice}, which handles general exceptions and
   * converts them to {@code Problem} responses.
   *
   * @return the configuration for exception advice
   * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
   * @since 1.2.0
   */
  public ExceptionAdvice getExceptionAdvice() {
    return exceptionAdvice;
  }

  /**
   * Sets configuration for {@code ExceptionWebMvcAdvice}.
   *
   * @param exceptionAdvice configuration for {@code ExceptionWebMvcAdvice}
   * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
   * @since 3.1.0
   */
  public void setExceptionAdvice(ExceptionAdvice exceptionAdvice) {
    this.exceptionAdvice = exceptionAdvice;
  }

  /**
   * Returns configuration for {@code ProblemExceptionWebMvcAdvice}, which handles exceptions of
   * type {@code ProblemException} and converts them to Problem responses.
   *
   * @return the configuration for problem exception advice
   * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
   * @since 1.2.0
   */
  public ProblemExceptionAdvice getProblemExceptionAdvice() {
    return problemExceptionAdvice;
  }

  /**
   * Sets configuration for {@code ProblemExceptionWebMvcAdvice}.
   *
   * @param problemExceptionAdvice configuration for {@code ProblemExceptionWebMvcAdvice}
   * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
   * @since 3.1.0
   */
  public void setProblemExceptionAdvice(ProblemExceptionAdvice problemExceptionAdvice) {
    this.problemExceptionAdvice = problemExceptionAdvice;
  }

  /**
   * Returns configuration for {@code ProblemContextWebMvcFilter}, which enriches request handling
   * with {@code ProblemContext}.
   *
   * @return the configuration for the {@code ProblemContextWebMvcFilter}
   * @see io.github.problem4j.core.ProblemContext
   * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
   * @since 1.2.0
   */
  public ProblemContextFilter getProblemContextFilter() {
    return problemContextFilter;
  }

  /**
   * Sets configuration for {@code ProblemContextWebMvcFilter}.
   *
   * @param problemContextFilter configuration for {@code ProblemContextWebMvcFilter}
   * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
   * @since 3.1.0
   */
  public void setProblemContextFilter(ProblemContextFilter problemContextFilter) {
    this.problemContextFilter = problemContextFilter;
  }

  /**
   * Returns configuration for {@code ProblemEnhancedWebMvcHandler} replacement, which allows
   * Problem4J to take control of exception handling normally performed by Spring's {@code
   * ResponseEntityExceptionHandler}.
   *
   * @return the configuration for the overwritten exception handler
   * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
   * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
   * @since 1.2.0
   */
  public ExceptionHandler getExceptionHandler() {
    return exceptionHandler;
  }

  /**
   * Sets configuration for {@code ProblemEnhancedWebMvcHandler} replacement.
   *
   * @param exceptionHandler configuration for {@code ProblemEnhancedWebMvcHandler}
   * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
   * @since 3.1.0
   */
  public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  /**
   * Returns configuration for {@code ProblemErrorController} replacement, which allows Problem4J to
   * take control of exception handling normally performed by Spring's {@code ErrorController}.
   *
   * @return the configuration for the overwritten error handler
   * @see org.springframework.boot.webmvc.error.ErrorController
   * @see io.github.problem4j.spring.webmvc.ProblemErrorController
   * @since 1.2.0
   */
  public ErrorController getErrorController() {
    return errorController;
  }

  /**
   * Sets configuration for {@code ProblemErrorController} replacement.
   *
   * @param errorController configuration for {@code ProblemErrorController}
   * @see io.github.problem4j.spring.webmvc.ProblemErrorController
   * @since 3.1.0
   */
  public void setErrorController(ErrorController errorController) {
    this.errorController = errorController;
  }

  /**
   * Configuration group for {@code ExceptionWebMvcAdvice}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.exception-advice.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
   * @since 1.2.0
   */
  public static class ExceptionAdvice {

    /**
     * Default enabled flag for {@code ExceptionWebMvcAdvice}.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /** Whether the {@code ExceptionWebMvcAdvice} bean should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group for {@code ExceptionWebMvcAdvice} with default values.
     *
     * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
     * @since 3.1.0
     */
    public ExceptionAdvice() {}

    /**
     * Creates a new configuration group for {@code ExceptionWebMvcAdvice}.
     *
     * @param enabled whether the {@code ExceptionWebMvcAdvice} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
     * @since 1.2.0
     * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
     *     together with the corresponding setters instead
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public ExceptionAdvice(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ExceptionWebMvcAdvice} should be registered.
     *
     * @return {@code true} if exception advice is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether the {@code ExceptionWebMvcAdvice} bean should be registered.
     *
     * @param enabled whether the {@code ExceptionWebMvcAdvice} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemExceptionWebMvcAdvice}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.problem-exception-advice.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
   * @since 1.2.0
   */
  public static class ProblemExceptionAdvice {

    /**
     * Default enabled flag for {@code ProblemExceptionWebMvcAdvice}.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /** Whether the {@code ProblemExceptionWebMvcAdvice} bean should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group for {@code ProblemExceptionWebMvcAdvice} with default
     * values.
     *
     * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
     * @since 3.1.0
     */
    public ProblemExceptionAdvice() {}

    /**
     * Creates a new configuration group for {@code ProblemExceptionWebMvcAdvice}.
     *
     * @param enabled whether the {@code ProblemExceptionWebMvcAdvice} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
     * @since 1.2.0
     * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
     *     together with the corresponding setters instead
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public ProblemExceptionAdvice(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemExceptionWebMvcAdvice} should be registered.
     *
     * @return {@code true} if the ProblemException advice is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether the {@code ProblemExceptionWebMvcAdvice} bean should be registered.
     *
     * @param enabled whether the {@code ProblemExceptionWebMvcAdvice} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemContextWebMvcFilter}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.problem-context-filter.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
   * @since 1.2.0
   */
  public static class ProblemContextFilter {

    /**
     * Default enabled flag for {@code ProblemContextWebMvcFilter}.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /** Whether the {@code ProblemContextWebMvcFilter} bean should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group for {@code ProblemContextWebMvcFilter} with default values.
     *
     * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
     * @since 3.1.0
     */
    public ProblemContextFilter() {}

    /**
     * Creates a new configuration group for {@code ProblemContextWebMvcFilter}.
     *
     * @param enabled whether the {@code ProblemContextWebMvcFilter} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
     * @since 1.2.0
     * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
     *     together with the corresponding setters instead
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public ProblemContextFilter(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemContextWebMvcFilter} should be registered.
     *
     * @return {@code true} if the context filter is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether the {@code ProblemContextWebMvcFilter} bean should be registered.
     *
     * @param enabled whether the {@code ProblemContextWebMvcFilter} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemEnhancedWebMvcHandler} override.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.exception-handler.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
   * @since 1.2.0
   */
  public static class ExceptionHandler {

    /**
     * Default enabled flag for {@code ExceptionHandler}.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /** Whether the {@code ProblemEnhancedWebMvcHandler} should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group for {@code ProblemEnhancedWebMvcHandler} with default
     * values.
     *
     * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
     * @since 3.1.0
     */
    public ExceptionHandler() {}

    /**
     * Creates a new configuration group for {@code ProblemEnhancedWebMvcHandler}.
     *
     * @param enabled whether the {@code ResponseEntityExceptionHandler} should be replaced with
     *     {@code ProblemEnhancedWebMvcHandler}
     * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
     * @since 1.2.0
     * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
     *     together with the corresponding setters instead
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public ExceptionHandler(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemEnhancedWebMvcHandler} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether the {@code ProblemEnhancedWebMvcHandler} should be registered.
     *
     * @param enabled whether the {@code ResponseEntityExceptionHandler} should be replaced with
     *     {@code ProblemEnhancedWebMvcHandler}
     * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemErrorController} override.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.error-web-exception-handler.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemErrorController
   * @see io.github.problem4j.spring.webmvc.autoconfigure.ProblemErrorMvcConfiguration
   * @see org.springframework.boot.webmvc.error.ErrorController
   * @since 1.2.0
   */
  public static class ErrorController {

    /**
     * Default enabled flag for {@code ErrorController}.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /** Whether the {@code ProblemErrorController} should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group with default values.
     *
     * @see io.github.problem4j.spring.webmvc.ProblemErrorController
     * @since 3.1.0
     */
    public ErrorController() {}

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ErrorController} should be replaced with {@code
     *     ProblemErrorController}
     * @see io.github.problem4j.spring.webmvc.ProblemErrorController
     * @since 1.2.0
     * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
     *     together with the corresponding setters instead
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public ErrorController(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemErrorController} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemErrorController
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether the {@code ProblemErrorController} should be registered.
     *
     * @param enabled whether the {@code ErrorController} should be replaced with {@code
     *     ProblemErrorController}
     * @see io.github.problem4j.spring.webmvc.ProblemErrorController
     * @see org.springframework.boot.webmvc.error.ErrorController
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}
