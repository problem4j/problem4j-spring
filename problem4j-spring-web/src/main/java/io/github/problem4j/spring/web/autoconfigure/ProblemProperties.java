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

import io.github.problem4j.spring.web.PostProcessorSettings;
import io.github.problem4j.spring.web.ProblemContextSettings;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Problem Details integration.
 *
 * <p>These properties can be set under the {@code problem4j.*} prefix.
 *
 * @since 1.2.0
 */
@ConfigurationProperties(prefix = "problem4j")
public class ProblemProperties implements ProblemContextSettings, PostProcessorSettings {

  /** Decides if Problem4J integration is enabled. */
  private boolean enabled = true;

  /**
   * Defines the format for the {@code detail} field in Problem responses. Supported values are
   * {@code "lowercase"}, {@code "capitalized"}, and {@code "uppercase"}.
   */
  private String detailFormat = DetailFormat.CAPITALIZED;

  /**
   * Name of the HTTP header that carries a trace ID for simple tracing provided by this library. If
   * unset, the feature is disabled.
   */
  private @Nullable String tracingHeaderName;

  /**
   * Template for overriding the {@code type} field of a Problem response. May contain placeholders
   * like {@code {problem.type}} and {@code {context.<key>}}.
   */
  private @Nullable String typeOverride;

  /**
   * Template for overriding the {@code title} field of a Problem response. May contain placeholders
   * like {@code {problem.title}} and {@code {context.<key>}}.
   */
  private @Nullable String titleOverride;

  /**
   * Template for overriding the {@code instance} field of a Problem response. May contain
   * placeholders like {@code {problem.instance}} and {@code {context.<key>}} for dynamic values.
   */
  private @Nullable String instanceOverride;

  /**
   * Templates for overriding extension members of a Problem response, keyed by extension name. May
   * contain placeholders like {@code {problem.type}}, {@code {problem.title}}, {@code
   * {problem.status}}, {@code {problem.detail}}, {@code {problem.instance}} and {@code
   * {context.<key>}}. An overridden extension replaces an existing extension with the same name.
   */
  private Map<String, String> extensionOverrides = new LinkedHashMap<>();

  /**
   * Policy for multiple {@code ProblemResolver}-s supporting the same exception class with equal
   * order. {@code fail} fails application startup, {@code first} uses the first one registered in
   * the application context, {@code last} uses the last one registered in the application context.
   */
  private DuplicateResolverPolicy duplicateResolverPolicy = DuplicateResolverPolicy.FAIL;

  /** Caching configuration for resolver lookups in {@code CachingProblemResolverStore}. */
  private ResolverCaching resolverCaching = new ResolverCaching();

  /**
   * Tracing configuration, deciding where trace identifiers of {@code ProblemContext} come from.
   */
  private Tracing tracing = new Tracing();

  /**
   * Observation configuration, deciding which exceptions handled by Problem4J are recorded as
   * errors of the current HTTP server observation.
   */
  private Observation observation = new Observation();

  /**
   * Creates a new instance of {@link ProblemProperties} with default values.
   *
   * @since 3.1.0
   */
  public ProblemProperties() {}

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
   *     defaults to a new {@link ResolverCaching} instance
   * @see io.github.problem4j.spring.web.CachingProblemResolverStore
   * @since 1.2.0
   * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
   *     together with the corresponding setters instead
   */
  @Deprecated(since = "3.1.0", forRemoval = true)
  public ProblemProperties(
      boolean enabled,
      String detailFormat,
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
    if (resolverCaching != null) {
      this.resolverCaching = resolverCaching;
    }
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
   * Sets whether problem handling is enabled.
   *
   * @param enabled whether problem handling is enabled
   * @since 3.1.0
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
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
   * Sets the format for the {@code detail} field (one of {@link DetailFormat#LOWERCASE}, {@link
   * DetailFormat#CAPITALIZED}, {@link DetailFormat#UPPERCASE}).
   *
   * @param detailFormat format for the {@code detail} field
   * @since 3.1.0
   */
  public void setDetailFormat(String detailFormat) {
    this.detailFormat = detailFormat;
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
   * Sets the name of the HTTP header carrying a trace ID.
   *
   * @param tracingHeaderName name of the HTTP header carrying a trace ID, or {@code null} to
   *     disable tracing
   * @since 3.1.0
   */
  public void setTracingHeaderName(@Nullable String tracingHeaderName) {
    this.tracingHeaderName = tracingHeaderName;
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
   * Sets the template for overriding the {@code type} field.
   *
   * @param typeOverride template for overriding the {@code type} field; may contain {@code
   *     {context.<key>}} placeholders (nullable)
   * @since 3.1.0
   */
  public void setTypeOverride(@Nullable String typeOverride) {
    this.typeOverride = typeOverride;
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
   * Sets the template for overriding the {@code title} field.
   *
   * @param titleOverride template for overriding the {@code title} field; may contain {@code
   *     {context.<key>}} placeholders (nullable)
   * @since 3.1.0
   */
  public void setTitleOverride(@Nullable String titleOverride) {
    this.titleOverride = titleOverride;
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
   * Sets the template for overriding the {@code instance} field.
   *
   * @param instanceOverride template for overriding the {@code instance} field; may contain {@code
   *     {context.<key>}} placeholders (nullable)
   * @since 3.1.0
   */
  public void setInstanceOverride(@Nullable String instanceOverride) {
    this.instanceOverride = instanceOverride;
  }

  /**
   * Returns the configured extension overrides, keyed by extension name.
   *
   * <p>Each value may contain special placeholders that will be replaced at runtime:
   *
   * <ul>
   *   <li>{@code {problem.type}}, {@code {problem.title}}, {@code {problem.status}}, {@code
   *       {problem.detail}}, {@code {problem.instance}} - replaced with the corresponding field of
   *       the problem
   *   <li>{@code {context.<key>}} - replaced with the value for {@code key} from the current {@code
   *       ProblemContext}
   * </ul>
   *
   * <p>Extension names with dots or other special characters must be written in bracket notation,
   * e.g. {@code problem4j.extension-overrides[app.version]=1.0}.
   *
   * @return the configured extension override templates, never {@code null}
   * @see io.github.problem4j.core.ProblemContext
   * @since 3.1.0
   */
  @Override
  public Map<String, String> getExtensionOverrides() {
    return extensionOverrides;
  }

  /**
   * Sets the templates for overriding extension members, keyed by extension name.
   *
   * @param extensionOverrides templates for overriding extension members; may contain {@code
   *     {problem.<field>}} and {@code {context.<key>}} placeholders
   * @since 3.1.0
   */
  public void setExtensionOverrides(Map<String, String> extensionOverrides) {
    this.extensionOverrides = extensionOverrides;
  }

  /**
   * Returns the policy for resolvers supporting the same exception class with equal order.
   *
   * @return the duplicate resolver policy
   * @since 3.1.0
   */
  public DuplicateResolverPolicy getDuplicateResolverPolicy() {
    return duplicateResolverPolicy;
  }

  /**
   * Sets the policy for resolvers supporting the same exception class with equal order.
   *
   * @param duplicateResolverPolicy the duplicate resolver policy
   * @since 3.1.0
   */
  public void setDuplicateResolverPolicy(DuplicateResolverPolicy duplicateResolverPolicy) {
    this.duplicateResolverPolicy = duplicateResolverPolicy;
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
   * Sets the caching configuration for resolver lookups.
   *
   * @param resolverCaching caching for resolver lookups ({@code CachingProblemResolverStore})
   * @see io.github.problem4j.spring.web.CachingProblemResolverStore
   * @since 3.1.0
   */
  public void setResolverCaching(ResolverCaching resolverCaching) {
    this.resolverCaching = resolverCaching;
  }

  /**
   * Returns the tracing configuration.
   *
   * @return tracing settings
   * @since 3.1.0
   */
  public Tracing getTracing() {
    return tracing;
  }

  /**
   * Sets the tracing configuration.
   *
   * @param tracing tracing settings
   * @since 3.1.0
   */
  public void setTracing(Tracing tracing) {
    this.tracing = tracing;
  }

  /**
   * Returns the observation configuration.
   *
   * @return observation settings
   * @since 3.1.0
   */
  public Observation getObservation() {
    return observation;
  }

  /**
   * Sets the observation configuration.
   *
   * @param observation observation settings
   * @since 3.1.0
   */
  public void setObservation(Observation observation) {
    this.observation = observation;
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
     * @deprecated since 3.1.0 as it is no longer used for property binding; use {@link
     *     #DEFAULT_ENABLED}
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static final String DEFAULT_ENABLED_VALUE = "false";

    /** Indicates whether resolver lookup caching is enabled. */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * Creates a new instance of {@link ResolverCaching} with default values.
     *
     * @since 3.1.0
     */
    public ResolverCaching() {}

    /**
     * Creates a new caching configuration.
     *
     * @param enabled whether caching is enabled
     * @since 1.2.0
     * @deprecated retained only for binary backwards compatibility; use the no-arg constructor
     *     together with {@link #setEnabled(boolean)} instead
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    public ResolverCaching(boolean enabled) {
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

    /**
     * Sets whether caching is enabled.
     *
     * @param enabled whether caching is enabled
     * @since 3.1.0
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  /**
   * Tracing configuration, deciding where trace identifiers of {@code ProblemContext} come from.
   *
   * @since 3.1.0
   */
  public static class Tracing {

    /**
     * Source of trace identifiers. {@code auto} uses {@code micrometer} if {@code
     * io.micrometer:micrometer-tracing} is on the classpath, and {@code simple} otherwise. {@code
     * simple} takes the trace ID from the {@code problem4j.tracing-header-name} header or generates
     * a random one. {@code micrometer} takes the trace ID and span ID of the current Micrometer
     * Tracing span first, falling back to {@code simple} behavior if no span is available, and
     * fails application startup if Micrometer Tracing is not on the classpath.
     */
    private TracingSource source = TracingSource.AUTO;

    /**
     * Creates a new instance of {@link Tracing} with default values.
     *
     * @since 3.1.0
     */
    public Tracing() {}

    /**
     * Returns the source of trace identifiers.
     *
     * @return the tracing source
     * @since 3.1.0
     */
    public TracingSource getSource() {
      return source;
    }

    /**
     * Sets the source of trace identifiers.
     *
     * @param source the tracing source
     * @since 3.1.0
     */
    public void setSource(TracingSource source) {
      this.source = source;
    }
  }

  /**
   * Observation configuration, deciding which exceptions handled by Problem4J are recorded as
   * errors of the current HTTP server observation.
   *
   * @since 3.1.0
   */
  public static class Observation {

    /**
     * Which handled exceptions are recorded as errors of the current HTTP server observation
     * (affecting the {@code exception} key of {@code http.server.requests} metrics and spans).
     * {@code server} records exceptions resulting in a 5xx response, {@code all} records all of
     * them, {@code none} disables recording.
     */
    private ErrorRecording recordErrors = ErrorRecording.SERVER;

    /**
     * Creates a new instance of {@link Observation} with default values.
     *
     * @since 3.1.0
     */
    public Observation() {}

    /**
     * Returns which handled exceptions are recorded as observation errors.
     *
     * @return the error recording mode
     * @since 3.1.0
     */
    public ErrorRecording getRecordErrors() {
      return recordErrors;
    }

    /**
     * Sets which handled exceptions are recorded as observation errors.
     *
     * @param recordErrors the error recording mode
     * @since 3.1.0
     */
    public void setRecordErrors(ErrorRecording recordErrors) {
      this.recordErrors = recordErrors;
    }
  }

  /**
   * Decides which exceptions handled by Problem4J are recorded as errors of the current HTTP server
   * observation.
   *
   * @see Observation#getRecordErrors()
   * @since 3.1.0
   */
  public enum ErrorRecording {

    /**
     * No exceptions are recorded.
     *
     * @since 3.1.0
     */
    NONE,

    /**
     * Exceptions resulting in a 5xx response are recorded.
     *
     * @since 3.1.0
     */
    SERVER,

    /**
     * All handled exceptions are recorded, regardless of the response status.
     *
     * @since 3.1.0
     */
    ALL;

    /**
     * Decides whether an exception resulting in the given response status should be recorded.
     *
     * @param status the response status code
     * @return {@code true} if the exception should be recorded as an observation error
     * @since 3.1.0
     */
    public boolean matches(int status) {
      return this == ALL || (this == SERVER && status >= 500 && status < 600);
    }
  }

  /**
   * Source of trace identifiers stored in {@code ProblemContext}.
   *
   * @see Tracing#getSource()
   * @since 3.1.0
   */
  public enum TracingSource {

    /**
     * {@link #MICROMETER} if {@code io.micrometer:micrometer-tracing} is on the classpath, {@link
     * #SIMPLE} otherwise.
     *
     * @since 3.1.0
     */
    AUTO,

    /**
     * Trace ID is taken from the {@code problem4j.tracing-header-name} header, or generated as a
     * random UUID without dashes.
     *
     * @since 3.1.0
     */
    SIMPLE,

    /**
     * Trace ID and span ID are taken from the current Micrometer Tracing span. If no span is
     * available, falls back to {@link #SIMPLE} behavior. Requires {@code
     * io.micrometer:micrometer-tracing} on the classpath, otherwise application startup fails.
     *
     * @since 3.1.0
     */
    MICROMETER
  }

  /**
   * Supported values for {@code detailFormat}.
   *
   * @see #getDetailFormat()
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

  /**
   * Policy for handling multiple {@link io.github.problem4j.spring.web.resolver.ProblemResolver
   * ProblemResolver} beans supporting the same exception class with equal order.
   *
   * @see #getDuplicateResolverPolicy()
   * @since 3.1.0
   */
  public enum DuplicateResolverPolicy {

    /**
     * Application startup fails with an {@link IllegalStateException} naming the conflicting beans.
     *
     * @since 3.1.0
     */
    FAIL,

    /**
     * The first of the resolvers with equal order, in bean registration order, is used.
     *
     * @since 3.1.0
     */
    FIRST,

    /**
     * The last of the resolvers with equal order, in bean registration order, is used. As
     * auto-configured resolvers are registered after application-defined ones, a built-in resolver
     * wins over an application-defined resolver with equal order for the same exception class,
     * similarly to how versions prior to 3.1.0 behaved.
     *
     * @since 3.1.0
     */
    LAST
  }
}
