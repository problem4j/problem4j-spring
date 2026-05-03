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

package io.github.problem4j.spring.web.autoconfigure;

import io.github.problem4j.spring.web.PostProcessorSettings;
import io.github.problem4j.spring.web.ProblemContextSettings;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for Problem Details integration.
 *
 * <p>These properties can be set under the {@code problem4j.*} prefix.
 *
 * @since 1.2.0
 */
@ConfigurationProperties(prefix = "problem4j")
public class ProblemProperties implements ProblemContextSettings, PostProcessorSettings {

  /**
   * Decides if Problem4J integration is enabled.
   *
   * @since 1.2.0
   */
  private final boolean enabled;

  /**
   * Defines the format for the {@code detail} field in Problem responses. Supported values are
   * {@code "lowercase"}, {@code "capitalized"}, and {@code "uppercase"}.
   *
   * @since 1.2.0
   */
  private final String detailFormat;

  /**
   * Name of the HTTP header that carries a trace ID for simple tracing provided by this library. If
   * unset, the feature is disabled.
   *
   * @since 1.2.0
   */
  private final @Nullable String tracingHeaderName;

  /**
   * Template for overriding the {@code type} field of a Problem response. May contain placeholders
   * like {@code {problem.type}} and {@code {context.<key>}}.
   *
   * @since 1.2.0
   */
  private final @Nullable String typeOverride;

  /**
   * Template for overriding the {@code title} field of a Problem response. May contain placeholders
   * like {@code {problem.title}} and {@code {context.<key>}}.
   *
   * @since 1.2.0
   */
  private final @Nullable String titleOverride;

  /**
   * Template for overriding the {@code instance} field of a Problem response. May contain
   * placeholders like {@code {problem.instance}} and {@code {context.<key>}} for dynamic values.
   *
   * @since 1.2.0
   */
  private final @Nullable String instanceOverride;

  /**
   * Caching configuration for resolver lookups in {@code CachingProblemResolverStore}.
   *
   * @since 1.2.0
   */
  private final ResolverCaching resolverCaching;

  /**
   * Constructs a new {@link ProblemProperties}.
   *
   * @param enabled whether problem handling is enabled
   * @param detailFormat format for the {@code detail} field (one of {@link DetailFormat#LOWERCASE},
   *     {@link DetailFormat#CAPITALIZED}, {@link DetailFormat#UPPERCASE})
   * @param tracingHeaderName name of the HTTP header carrying a trace ID (nullable)
   * @param typeOverride template for overriding the {@code type} field; may contain {@code
   *     {context.<key>}} placeholders (nullable)
   * @param titleOverride template for overriding the {@code title} field; may contain {@code
   *     {context.<key>}} placeholders (nullable)
   * @param instanceOverride template for overriding the {@code instance} field; may contain {@code
   *     {context.<key>}} placeholders (nullable)
   * @param resolverCaching caching for resolver lookups ({@code CachingProblemResolverStore});
   *     defaults to {@link ResolverCaching#createDefault()}
   * @see io.github.problem4j.spring.web.CachingProblemResolverStore
   * @since 1.2.0
   */
  public ProblemProperties(
      @DefaultValue("true") boolean enabled,
      @DefaultValue(DetailFormat.CAPITALIZED) String detailFormat,
      @Nullable String tracingHeaderName,
      @Nullable String typeOverride,
      @Nullable String titleOverride,
      @Nullable String instanceOverride,
      @Nullable ResolverCaching resolverCaching) {
    this.enabled = enabled;
    this.detailFormat = detailFormat;
    this.tracingHeaderName = tracingHeaderName;
    this.typeOverride = typeOverride;
    this.titleOverride = titleOverride;
    this.instanceOverride = instanceOverride;
    this.resolverCaching =
        resolverCaching != null ? resolverCaching : ResolverCaching.createDefault();
  }

  /**
   * Indicates whether problem handling is currently enabled.
   *
   * @return {@code true} if problem handling is enabled; {@code false} otherwise
   * @since 1.2.0
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns the configured format for the {@code "detail"} field.
   *
   * @return the detail format
   * @since 1.2.0
   */
  public String getDetailFormat() {
    return detailFormat;
  }

  /**
   * Returns the name of the HTTP header used for tracing requests.
   *
   * <p>This header provides the trace identifier that can be injected into responses. When combined
   * with {@link #instanceOverride}, the trace ID may be used to dynamically populate the {@code
   * instance} field of a {@code Problem} response.
   *
   * <p>If no header name is configured, this method may return {@code null}.
   *
   * @return the tracing header name, or {@code null} if not set
   * @see io.github.problem4j.core.Problem
   * @since 1.2.0
   */
  @Override
  public @Nullable String getTracingHeaderName() {
    return tracingHeaderName;
  }

  /**
   * Returns the configured type override.
   *
   * <p>This value defines a fixed or templated {@code type} URI to be used for all problems
   * processed by the post-processor, replacing the original problem type if present. The value may
   * include special placeholders that will be dynamically replaced at runtime:
   *
   * <ul>
   *   <li>{@code {problem.type}} - replaced with the original problem's type URI
   *   <li>{@code {context.<key>}} - replaced with the value for {@code key} from the current {@code
   *       ProblemContext}
   * </ul>
   *
   * <p>If no override is configured, this method may return {@code null}, and the original problem
   * type will be preserved.
   *
   * @return the configured type override string, or {@code null} if not set
   * @see io.github.problem4j.core.ProblemContext
   * @since 1.2.0
   */
  @Override
  public @Nullable String getTypeOverride() {
    return typeOverride;
  }

  /**
   * Returns the configured title override.
   *
   * <p>The value may include special placeholders that will be replaced at runtime:
   *
   * <ul>
   *   <li>{@code {problem.title}} - replaced with the original problem's title
   *   <li>{@code {context.<key>}} - replaced with the value for {@code key} from the current {@code
   *       ProblemContext}
   * </ul>
   *
   * @return the configured title override string, or {@code null} if not set
   * @see io.github.problem4j.core.ProblemContext
   * @since 1.2.0
   */
  @Override
  public @Nullable String getTitleOverride() {
    return titleOverride;
  }

  /**
   * Returns the configured instance override.
   *
   * <p>This value may contain special placeholders that will be replaced at runtime with contextual
   * or problem-specific data:
   *
   * <ul>
   *   <li>{@code {problem.instance}} - replaced with the original problem's instance URI
   *   <li>{@code {context.<key>}} - replaced with the value for {@code key} from the current {@code
   *       ProblemContext}
   * </ul>
   *
   * <p>This is useful if the {@code instance} field cannot be determined when throwing a {@code
   * ProblemException} (or an exception annotated with {@code @ProblemMapping}). Setting this
   * configuration, along with {@link #tracingHeaderName}, enables this feature.
   *
   * @return the configured instance override string, or {@code null} if not set
   * @see io.github.problem4j.core.ProblemContext
   * @see io.github.problem4j.core.ProblemException
   * @since 1.2.0
   */
  @Override
  public @Nullable String getInstanceOverride() {
    return instanceOverride;
  }

  /**
   * Returns the caching configuration.
   *
   * @return caching settings
   * @since 1.2.0
   */
  public ResolverCaching getResolverCaching() {
    return resolverCaching;
  }

  /**
   * Caching configuration for ({@code CachingProblemResolverStore}).
   *
   * <p>Controls whether resolver lookup caching is enabled and its maximum size.
   *
   * @see io.github.problem4j.spring.web.CachingProblemResolverStore
   * @since 1.2.0
   */
  public static class ResolverCaching {

    /**
     * Default enabled flag for resolver caching.
     *
     * @since 1.2.0
     */
    public static final boolean DEFAULT_ENABLED = false;

    /**
     * Default enabled value string for resolver caching.
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_ENABLED_VALUE = "false";

    private static ResolverCaching createDefault() {
      return new ResolverCaching(DEFAULT_ENABLED);
    }

    /**
     * Indicates whether resolver lookup caching is enabled.
     *
     * @since 1.2.0
     */
    private final boolean enabled;

    /**
     * Creates a new caching configuration.
     *
     * @param enabled whether caching is enabled
     * @since 1.2.0
     */
    public ResolverCaching(@DefaultValue(DEFAULT_ENABLED_VALUE) boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether caching is enabled.
     *
     * @return true if enabled
     * @since 1.2.0
     */
    public boolean isEnabled() {
      return enabled;
    }
  }

  /**
   * Supported values for {@code detailFormat}.
   *
   * @since 1.2.0
   */
  public static class DetailFormat {

    /**
     * All detail messages in lowercase.
     *
     * @since 1.2.0
     */
    public static final String LOWERCASE = "lowercase";

    /**
     * Detail messages with the first letter capitalized.
     *
     * @since 1.2.0
     */
    public static final String CAPITALIZED = "capitalized";

    /**
     * All detail messages in uppercase.
     *
     * @since 1.2.0
     */
    public static final String UPPERCASE = "uppercase";

    private DetailFormat() {}
  }
}
