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

package io.github.problem4j.spring.web;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

/**
 * {@link ProblemPostProcessor} implementation that overrides selected fields of a {@link Problem}
 * based on configurable templates.
 *
 * <p>This processor allows the {@code type} and {@code instance} fields to be replaced with custom
 * values defined through {@link PostProcessorSettings}. Each override can include placeholders that
 * are resolved at runtime using data from the current {@link Problem} or {@link ProblemContext}.
 *
 * <p>Supported placeholders:
 *
 * <ul>
 *   <li><b>For {@code type} override:</b>
 *       <ul>
 *         <li>{@code {problem.type}} - replaced with the original problem's {@code type}
 *       </ul>
 *   <li><b>For {@code instance} override:</b>
 *       <ul>
 *         <li>{@code {problem.instance}} - replaced with the original problem's {@code instance}
 *         <li>{@code {context.traceId}} - replaced with the current request's trace identifier, if
 *             available
 *       </ul>
 * </ul>
 *
 * <p>If an override template produces the same value as the existing field, no change is applied.
 * If neither override results in a change, the original {@link Problem} instance is returned
 * unchanged.
 *
 * <p>Example configuration:
 *
 * <pre>{@code
 * problem4j.type-override=https://errors.example.com/{problem.type}
 * problem4j.instance-override=/errors/{context.traceId}
 * }</pre>
 */
public class DefaultProblemPostProcessor implements ProblemPostProcessor {

  private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)}");

  private final PostProcessorSettings settings;

  /**
   * Constructs a new {@link DefaultProblemPostProcessor}.
   *
   * @param settings the post-processor settings to use
   */
  public DefaultProblemPostProcessor(PostProcessorSettings settings) {
    this.settings = settings;
  }

  /**
   * Applies configured overrides to {@code type} and/or {@code instance}.
   *
   * <p>About {@code about:blank}: when the original type is {@code null} or {@link
   * Problem#BLANK_TYPE}, the placeholder {@code {problem.type}} resolves to an empty string
   * (templates naturally collapse). If the template is exactly {@code {problem.type}}, the original
   * {@code about:blank} value is preserved instead of becoming empty.
   */
  @Override
  public Problem process(@Nullable ProblemContext context, Problem problem) {
    if (context == null) {
      context = ProblemContext.create();
    }

    ProblemBuilder builder = null;

    // Override type only if {problem.type} is referenced and original type is valid
    builder = overrideProblemType(context, problem, builder);

    builder = overrideProblemInstance(context, problem, builder);

    return builder != null ? builder.build() : problem;
  }

  /**
   * Applies a {@code type} override based on the configured template.
   *
   * @param context the problem context
   * @param problem the original problem
   * @param builder the problem builder to update, or null to create a new one
   * @return the updated or original builder
   */
  protected @Nullable ProblemBuilder overrideProblemType(
      ProblemContext context, Problem problem, @Nullable ProblemBuilder builder) {
    if (!StringUtils.hasLength(settings.getTypeOverride())) {
      return builder;
    }

    String template = settings.getTypeOverride();
    boolean requiresProblemType = template.contains("{problem.type}");
    boolean hasProblemType = problem.isTypeNonBlank();

    if (canOverride(requiresProblemType, hasProblemType)) {
      Optional<String> newTypeCandidate = overrideType(context, problem);
      if (newTypeCandidate.isPresent()) {
        if (builder == null) {
          builder = problem.toBuilder();
        }
        builder = builder.type(newTypeCandidate.get());
      }
    }
    return builder;
  }

  /**
   * Determines whether the {@code type} can be overridden.
   *
   * @param requiresProblemType whether the template requires a problem type
   * @param hasProblemType whether the problem has a type
   * @return true if override is allowed
   */
  protected boolean canOverride(boolean requiresProblemType, boolean hasProblemType) {
    return !requiresProblemType || hasProblemType;
  }

  /**
   * Because this implementation does not try to resolve dynamic fields, it does not reuse the code
   * from {@code ProblemPostProcessor}. Instead, we rely on simple substring replacement in form of
   * {@link String#replace(CharSequence, CharSequence)}, and then removing all remaining unknown
   * variables.
   *
   * @param context the problem context
   * @param problem the original problem
   * @return an optional containing the new type if override is possible
   * @see io.github.problem4j.spring.web.ProblemPostProcessor
   */
  protected Optional<String> overrideType(ProblemContext context, Problem problem) {
    if (!problem.isTypeNonBlank() || !StringUtils.hasLength(settings.getTypeOverride())) {
      return Optional.empty();
    }

    String template = settings.getTypeOverride();
    String original = stringOrEmpty(problem.getType());
    String resolved = template.replace("{problem.type}", original);

    if (hasRemainingUnknownPlaceholders(resolved)) {
      return Optional.empty();
    }

    return Optional.of(resolved).filter(StringUtils::hasLength);
  }

  /**
   * Applies an {@code instance} override based on the configured template.
   *
   * @param context the problem context
   * @param problem the original problem
   * @param builder the problem builder to update, or null to create a new one
   * @return the updated or original builder
   */
  protected @Nullable ProblemBuilder overrideProblemInstance(
      ProblemContext context, Problem problem, @Nullable ProblemBuilder builder) {
    if (!StringUtils.hasLength(settings.getInstanceOverride())) {
      return builder;
    }

    String template = settings.getInstanceOverride();
    boolean needsProblemInstance = template.contains("{problem.instance}");
    boolean needsTraceId = template.contains("{context.traceId}");
    boolean hasProblemInstance =
        problem.getInstance() != null && StringUtils.hasLength(problem.getInstance().toString());
    boolean hasTraceId = StringUtils.hasLength(context.get("traceId"));

    if (canOverride(needsProblemInstance, hasProblemInstance, needsTraceId, hasTraceId)) {
      String newInstance = overrideInstance(context, problem);
      if (StringUtils.hasLength(newInstance)
          && !newInstance.equals(stringOrEmpty(problem.getInstance()))) {
        if (builder == null) {
          builder = problem.toBuilder();
        }
        builder.instance(newInstance);
      }
    }
    return builder;
  }

  /**
   * Determines whether the {@code instance} can be overridden.
   *
   * @param needsProblemInstance whether the template requires a problem instance
   * @param hasProblemInstance whether the problem has an instance
   * @param needsTraceId whether the template requires a trace ID
   * @param hasTraceId whether the context has a trace ID
   * @return true if override is allowed
   */
  protected boolean canOverride(
      boolean needsProblemInstance,
      boolean hasProblemInstance,
      boolean needsTraceId,
      boolean hasTraceId) {
    return (!needsProblemInstance || hasProblemInstance) && (!needsTraceId || hasTraceId);
  }

  /**
   * Because this implementation does not try to resolve dynamic fields, it does not reuse the code
   * from {@code ProblemPostProcessor}. Instead, we rely on simple substring replacement in form of
   * {@link String#replace(CharSequence, CharSequence)}, and then removing all remaining unknown
   * variables.
   *
   * <p>If the algorithm discovers remaining placeholders that are unresolved, overriding is aborted
   * and original value is restored.
   *
   * @param context the problem context
   * @param problem the original problem
   * @return the new instance string
   * @see io.github.problem4j.spring.web.ProblemPostProcessor
   */
  protected String overrideInstance(ProblemContext context, Problem problem) {
    if (!StringUtils.hasLength(settings.getInstanceOverride())) {
      return stringOrEmpty(problem.getInstance());
    }

    String template = settings.getInstanceOverride();
    String instanceValue = stringOrEmpty(problem.getInstance());
    String traceIdValue = stringOrEmpty(context.get("traceId"));

    template = template.replace("{problem.instance}", instanceValue);
    template = template.replace("{context.traceId}", traceIdValue);

    if (hasRemainingUnknownPlaceholders(template)) {
      return instanceValue;
    }

    return template;
  }

  /**
   * Converts the given value to a string or returns an empty string if {@code null}.
   *
   * @param value the value to convert
   * @return the string representation or empty string
   */
  protected String stringOrEmpty(@Nullable Object value) {
    return value != null ? value.toString() : "";
  }

  /**
   * Because this implementation does not try to resolve dynamic fields, it does not reuse the code
   * from {@code ProblemMapper}. Instead, we rely on simple substring replacement, and then removing
   * all remaining unknown variables.
   *
   * @param value the string to check
   * @return true if unresolved placeholders remain
   * @see io.github.problem4j.core.ProblemMapper
   */
  protected boolean hasRemainingUnknownPlaceholders(String value) {
    return PLACEHOLDER.matcher(value).find();
  }

  /**
   * Returns the post-processor configuration settings.
   *
   * @return the post-processor settings
   */
  protected PostProcessorSettings getSettings() {
    return settings;
  }
}
