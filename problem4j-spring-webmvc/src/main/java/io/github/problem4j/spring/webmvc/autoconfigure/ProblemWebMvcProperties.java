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

package io.github.problem4j.spring.webmvc.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for Problem Details WebMVC integration.
 *
 * <p>These properties can be set under the {@code problem4j.webmvc.*} prefix.
 */
@ConfigurationProperties(prefix = "problem4j.webmvc")
public class ProblemWebMvcProperties {

  /** Whether Problem4J integration with WebMVC is enabled. */
  private final boolean enabled;

  /** Configuration for {@code ExceptionWebMvcAdvice}. */
  private final ExceptionAdvice exceptionAdvice;

  /** Configuration for {@code ProblemExceptionWebMvcAdvice}. */
  private final ProblemExceptionAdvice problemExceptionAdvice;

  /** Configuration for {@code ProblemContextWebMvcFilter}. */
  private final ProblemContextFilter problemContextFilter;

  /** Configuration for {@code ProblemEnhancedWebMvcHandler} replacement. */
  private final ExceptionHandler exceptionHandler;

  /** Configuration for {@code ProblemErrorController} replacement. */
  private final ErrorController errorController;

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
   */
  public ProblemWebMvcProperties(
      @DefaultValue("true") boolean enabled,
      ExceptionAdvice exceptionAdvice,
      ProblemExceptionAdvice problemExceptionAdvice,
      ProblemContextFilter problemContextFilter,
      ExceptionHandler exceptionHandler,
      ErrorController errorController) {
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
    this.errorController =
        errorController != null ? errorController : ErrorController.createDefault();
  }

  /**
   * Indicates whether Problem4J integration with WebMVC is currently enabled.
   *
   * @return {@code true} if problem WebMVC is enabled; {@code false} otherwise
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns configuration for {@code ExceptionWebMvcAdvice}, which handles general exceptions and
   * converts them to {@code Problem} responses.
   *
   * @return the configuration for exception advice
   * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
   */
  public ExceptionAdvice getExceptionAdvice() {
    return exceptionAdvice;
  }

  /**
   * Returns configuration for {@code ProblemExceptionWebMvcAdvice}, which handles exceptions of
   * type {@code ProblemException} and converts them to Problem responses.
   *
   * @return the configuration for problem exception advice
   * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
   */
  public ProblemExceptionAdvice getProblemExceptionAdvice() {
    return problemExceptionAdvice;
  }

  /**
   * Returns configuration for {@code ProblemContextWebMvcFilter}, which enriches request handling
   * with {@code ProblemContext}.
   *
   * @return the configuration for the {@code ProblemContextWebMvcFilter}
   * @see io.github.problem4j.core.ProblemContext
   * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
   */
  public ProblemContextFilter getProblemContextFilter() {
    return problemContextFilter;
  }

  /**
   * Returns configuration for {@code ProblemEnhancedWebMvcHandler} replacement, which allows
   * Problem4J to take control of exception handling normally performed by Spring's {@code
   * ResponseEntityExceptionHandler}.
   *
   * @return the configuration for the overwritten exception handler
   * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
   * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
   */
  public ExceptionHandler getExceptionHandler() {
    return exceptionHandler;
  }

  /**
   * Returns configuration for {@code ProblemErrorController} replacement, which allows Problem4J to
   * take control of exception handling normally performed by Spring's {@code ErrorController}.
   *
   * @return the configuration for the overwritten error handler
   * @see org.springframework.boot.web.servlet.error.ErrorController
   * @see io.github.problem4j.spring.webmvc.ProblemErrorController
   */
  public ErrorController getErrorController() {
    return errorController;
  }

  /**
   * Configuration group for {@code ExceptionWebMvcAdvice}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.exception-advice.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
   */
  public static class ExceptionAdvice {

    /** Default enabled flag for {@code ExceptionWebMvcAdvice}. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value string for {@code ExceptionWebMvcAdvice}. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ExceptionAdvice createDefault() {
      return new ExceptionAdvice(DEFAULT_ENABLED);
    }

    /** Whether the {@code ExceptionWebMvcAdvice} bean should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group for {@code ExceptionWebMvcAdvice}.
     *
     * @param enabled whether the {@code ExceptionWebMvcAdvice} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
     */
    public ExceptionAdvice(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ExceptionWebMvcAdvice} should be registered.
     *
     * @return {@code true} if exception advice is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemExceptionWebMvcAdvice}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.problem-exception-advice.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
   */
  public static class ProblemExceptionAdvice {

    /** Default enabled flag for {@code ProblemExceptionWebMvcAdvice}. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value string for {@code ProblemExceptionWebMvcAdvice}. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ProblemExceptionAdvice createDefault() {
      return new ProblemExceptionAdvice(DEFAULT_ENABLED);
    }

    /** Whether the {@code ProblemExceptionWebMvcAdvice} bean should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group for {@code ProblemExceptionWebMvcAdvice}.
     *
     * @param enabled whether the {@code ProblemExceptionWebMvcAdvice} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
     */
    public ProblemExceptionAdvice(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemExceptionWebMvcAdvice} should be registered.
     *
     * @return {@code true} if the ProblemException advice is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemContextWebMvcFilter}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.problem-context-filter.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
   */
  public static class ProblemContextFilter {

    /** Default enabled flag for {@code ProblemContextWebMvcFilter}. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value string for {@code ProblemContextWebMvcFilter}. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ProblemContextFilter createDefault() {
      return new ProblemContextFilter(DEFAULT_ENABLED);
    }

    /** Whether the {@code ProblemContextWebMvcFilter} bean should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group for {@code ProblemContextWebMvcFilter}.
     *
     * @param enabled whether the {@code ProblemContextWebMvcFilter} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
     */
    public ProblemContextFilter(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemContextWebMvcFilter} should be registered.
     *
     * @return {@code true} if the context filter is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemEnhancedWebMvcHandler} override.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.exception-handler.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
   */
  public static class ExceptionHandler {

    /** Default enabled flag for {@code ExceptionHandler}. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value string for {@code ExceptionHandler}. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ExceptionHandler createDefault() {
      return new ExceptionHandler(DEFAULT_ENABLED);
    }

    /** Whether the {@code ProblemEnhancedWebMvcHandler} should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group for {@code ProblemEnhancedWebMvcHandler}.
     *
     * @param enabled whether the {@code ResponseEntityExceptionHandler} should be replaced with
     *     {@code ProblemEnhancedWebMvcHandler}
     * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
     */
    public ExceptionHandler(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemEnhancedWebMvcHandler} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemErrorController} override.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.error-web-exception-handler.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemErrorController
   * @see io.github.problem4j.spring.webmvc.autoconfigure.ProblemErrorMvcConfiguration
   * @see org.springframework.boot.web.servlet.error.ErrorController
   */
  public static class ErrorController {

    /** Default enabled flag for {@code ErrorController}. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Default enabled value string for {@code ErrorController}. */
    public static final String DEFAULT_ENABLED_VALUE = "true";

    private static ErrorController createDefault() {
      return new ErrorController(DEFAULT_ENABLED);
    }

    /** Whether the {@code ProblemErrorController} should be registered. */
    private final boolean enabled;

    /**
     * Creates a new configuration group.
     *
     * @param enabled whether the {@code ErrorController} should be replaced with {@code
     *     ProblemErrorController}
     * @see io.github.problem4j.spring.webmvc.ProblemErrorController
     * @see org.springframework.boot.web.servlet.error.ErrorController
     */
    public ErrorController(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemErrorController} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemErrorController
     */
    public boolean isEnabled() {
      return enabled;
    }
  }
}
