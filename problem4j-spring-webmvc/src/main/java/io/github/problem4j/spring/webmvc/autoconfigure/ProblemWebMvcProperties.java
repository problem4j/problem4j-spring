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

  /** Configuration for {@code ProblemContextFilter}. */
  private ProblemContextFilter problemContextFilter = new ProblemContextFilter();

  /** Configuration for {@code ProblemResponseEntityExceptionHandler} replacement. */
  private ExceptionHandler exceptionHandler = new ExceptionHandler();

  /** Configuration for {@code ProblemErrorController} replacement. */
  private ErrorController errorController = new ErrorController();

  /** Configuration for {@code ProblemHandlerExceptionResolver}. */
  private HandlerExceptionResolver handlerExceptionResolver = new HandlerExceptionResolver();

  /** Configuration for {@code ProblemResponseBodyAdvice}. */
  private ResponseBodyAdvice responseBodyAdvice = new ResponseBodyAdvice();

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
   * @param problemContextFilter configuration for {@code ProblemContextFilter}
   * @param exceptionHandler configuration for {@code ProblemResponseEntityExceptionHandler}
   * @param errorController configuration for {@code ProblemErrorController}
   * @see io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice
   * @see io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice
   * @see io.github.problem4j.spring.webmvc.ProblemContextFilter
   * @see io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler
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
   * Returns configuration for {@code ProblemContextFilter}, which enriches request handling with
   * {@code ProblemContext}.
   *
   * @return the configuration for the {@code ProblemContextFilter}
   * @see io.github.problem4j.core.ProblemContext
   * @see io.github.problem4j.spring.webmvc.ProblemContextFilter
   * @since 1.2.0
   */
  public ProblemContextFilter getProblemContextFilter() {
    return problemContextFilter;
  }

  /**
   * Sets configuration for {@code ProblemContextFilter}.
   *
   * @param problemContextFilter configuration for {@code ProblemContextFilter}
   * @see io.github.problem4j.spring.webmvc.ProblemContextFilter
   * @since 3.1.0
   */
  public void setProblemContextFilter(ProblemContextFilter problemContextFilter) {
    this.problemContextFilter = problemContextFilter;
  }

  /**
   * Returns configuration for {@code ProblemResponseEntityExceptionHandler} replacement, which
   * allows Problem4J to take control of exception handling normally performed by Spring's {@code
   * ResponseEntityExceptionHandler}.
   *
   * @return the configuration for the overwritten exception handler
   * @see io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler
   * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
   * @since 1.2.0
   */
  public ExceptionHandler getExceptionHandler() {
    return exceptionHandler;
  }

  /**
   * Sets configuration for {@code ProblemResponseEntityExceptionHandler} replacement.
   *
   * @param exceptionHandler configuration for {@code ProblemResponseEntityExceptionHandler}
   * @see io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler
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
   * Returns configuration for {@code ProblemHandlerExceptionResolver}, which renders exceptions not
   * handled by the application as {@code Problem} responses. Applies only to {@code
   * problem4j.exception-handling.mode=deferred}.
   *
   * @return the configuration for {@code ProblemHandlerExceptionResolver}
   * @since 3.1.0
   */
  public HandlerExceptionResolver getHandlerExceptionResolver() {
    return handlerExceptionResolver;
  }

  /**
   * Sets configuration for {@code ProblemHandlerExceptionResolver}.
   *
   * @param handlerExceptionResolver configuration for {@code ProblemHandlerExceptionResolver}
   * @since 3.1.0
   */
  public void setHandlerExceptionResolver(HandlerExceptionResolver handlerExceptionResolver) {
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  /**
   * Returns configuration for {@code ProblemResponseBodyAdvice}, which post-processes {@code
   * Problem}s and {@code ProblemDetail}s written as response bodies. Applies only to {@code
   * problem4j.exception-handling.mode=deferred}.
   *
   * @return the configuration for {@code ProblemResponseBodyAdvice}
   * @since 3.1.0
   */
  public ResponseBodyAdvice getResponseBodyAdvice() {
    return responseBodyAdvice;
  }

  /**
   * Sets configuration for {@code ProblemResponseBodyAdvice}.
   *
   * @param responseBodyAdvice configuration for {@code ProblemResponseBodyAdvice}
   * @since 3.1.0
   */
  public void setResponseBodyAdvice(ResponseBodyAdvice responseBodyAdvice) {
    this.responseBodyAdvice = responseBodyAdvice;
  }

  /**
   * Configuration group for {@code ExceptionWebMvcAdvice}. Applies only to {@code
   * problem4j.exception-handling.mode=advice} (default).
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

    /**
     * Default enabled value string for {@code ExceptionWebMvcAdvice}.
     *
     * @since 1.2.0
     * @deprecated since 3.1.0 as it is no longer used for property binding; use {@link
     *     #DEFAULT_ENABLED}
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static final String DEFAULT_ENABLED_VALUE = "true";

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
   * Configuration group for {@code ProblemExceptionWebMvcAdvice}. Applies only to {@code
   * problem4j.exception-handling.mode=advice} (default).
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

    /**
     * Default enabled value string for {@code ProblemExceptionWebMvcAdvice}.
     *
     * @since 1.2.0
     * @deprecated since 3.1.0 as it is no longer used for property binding; use {@link
     *     #DEFAULT_ENABLED}
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static final String DEFAULT_ENABLED_VALUE = "true";

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
   * Configuration group for {@code ProblemContextFilter}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.problem-context-filter.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemContextFilter
   * @since 1.2.0
   */
  public static class ProblemContextFilter {

    /**
     * Default enabled flag for {@code ProblemContextFilter}.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Default enabled value string for {@code ProblemContextFilter}.
     *
     * @since 1.2.0
     * @deprecated since 3.1.0 as it is no longer used for property binding; use {@link
     *     #DEFAULT_ENABLED}
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static final String DEFAULT_ENABLED_VALUE = "true";

    /** Whether the {@code ProblemContextFilter} bean should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group for {@code ProblemContextFilter} with default values.
     *
     * @see io.github.problem4j.spring.webmvc.ProblemContextFilter
     * @since 3.1.0
     */
    public ProblemContextFilter() {}

    /**
     * Creates a new configuration group for {@code ProblemContextFilter}.
     *
     * @param enabled whether the {@code ProblemContextFilter} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ProblemContextFilter
     * @since 1.2.0
     * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
     *     together with the corresponding setters instead
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public ProblemContextFilter(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether {@code ProblemContextFilter} should be registered.
     *
     * @return {@code true} if the context filter is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemContextFilter
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether the {@code ProblemContextFilter} bean should be registered.
     *
     * @param enabled whether the {@code ProblemContextFilter} bean should be registered
     * @see io.github.problem4j.spring.webmvc.ProblemContextFilter
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemResponseEntityExceptionHandler} override. Applies only to
   * {@code problem4j.exception-handling.mode=advice} (default).
   *
   * <p>Controlled by the property {@code problem4j.webmvc.exception-handler.enabled}.
   *
   * @see io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler
   * @since 1.2.0
   */
  public static class ExceptionHandler {

    /**
     * Default enabled flag for {@code ExceptionHandler}.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Default enabled value string for {@code ExceptionHandler}.
     *
     * @since 1.2.0
     * @deprecated since 3.1.0 as it is no longer used for property binding; use {@link
     *     #DEFAULT_ENABLED}
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static final String DEFAULT_ENABLED_VALUE = "true";

    /** Whether the {@code ProblemResponseEntityExceptionHandler} should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group for {@code ProblemResponseEntityExceptionHandler} with
     * default values.
     *
     * @see io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler
     * @since 3.1.0
     */
    public ExceptionHandler() {}

    /**
     * Creates a new configuration group for {@code ProblemResponseEntityExceptionHandler}.
     *
     * @param enabled whether the {@code ResponseEntityExceptionHandler} should be replaced with
     *     {@code ProblemResponseEntityExceptionHandler}
     * @see io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler
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
     * Returns whether {@code ProblemResponseEntityExceptionHandler} should be registered.
     *
     * @return {@code true} if the overwritten exception handler is enabled, otherwise {@code false}
     * @see io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether the {@code ProblemResponseEntityExceptionHandler} should be registered.
     *
     * @param enabled whether the {@code ResponseEntityExceptionHandler} should be replaced with
     *     {@code ProblemResponseEntityExceptionHandler}
     * @see io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemErrorController} override, rendering errors that do not
   * pass through Spring MVC exception resolvers (e.g. {@code response.sendError(...)} or exceptions
   * of servlet filters) as {@code Problem} responses. With {@code
   * problem4j.exception-handling.mode=deferred}, the exception that caused the error is translated
   * the same way as exceptions of controllers.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.error-controller.enabled}.
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

    /**
     * Default enabled value string for {@code ErrorController}.
     *
     * @since 1.2.0
     * @deprecated since 3.1.0 as it is no longer used for property binding; use {@link
     *     #DEFAULT_ENABLED}
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static final String DEFAULT_ENABLED_VALUE = "true";

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

  /**
   * Configuration group for {@code ProblemHandlerExceptionResolver}. Applies only to {@code
   * problem4j.exception-handling.mode=deferred}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.handler-exception-resolver.enabled}.
   *
   * @since 3.1.0
   */
  public static class HandlerExceptionResolver {

    /**
     * Default enabled flag for {@code ProblemHandlerExceptionResolver}.
     *
     * @since 3.1.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /** Whether the {@code ProblemHandlerExceptionResolver} should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group with default values.
     *
     * @since 3.1.0
     */
    public HandlerExceptionResolver() {}

    /**
     * Returns whether {@code ProblemHandlerExceptionResolver} should be registered.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     * @since 3.1.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether {@code ProblemHandlerExceptionResolver} should be registered.
     *
     * @param enabled whether {@code ProblemHandlerExceptionResolver} should be registered
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  /**
   * Configuration group for {@code ProblemResponseBodyAdvice}. Applies only to {@code
   * problem4j.exception-handling.mode=deferred}.
   *
   * <p>Controlled by the property {@code problem4j.webmvc.response-body-advice.enabled}.
   *
   * @since 3.1.0
   */
  public static class ResponseBodyAdvice {

    /**
     * Default enabled flag for {@code ProblemResponseBodyAdvice}.
     *
     * @since 3.1.0
     */
    public static final boolean DEFAULT_ENABLED = true;

    /** Whether the {@code ProblemResponseBodyAdvice} should be registered. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new configuration group with default values.
     *
     * @since 3.1.0
     */
    public ResponseBodyAdvice() {}

    /**
     * Returns whether {@code ProblemResponseBodyAdvice} should be registered.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     * @since 3.1.0
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether {@code ProblemResponseBodyAdvice} should be registered.
     *
     * @param enabled whether {@code ProblemResponseBodyAdvice} should be registered
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}
