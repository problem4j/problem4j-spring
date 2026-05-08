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

package io.github.problem4j.spring.web;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

/**
 * {@link ProblemPostProcessor} implementation that overrides selected fields of a {@link Problem}
 * based on configurable templates.
 *
 * <p>This processor allows the {@code type}, {@code title}, and {@code instance} fields to be
 * replaced with custom values defined through {@link PostProcessorSettings}. Each override can
 * include placeholders that are resolved at runtime using data from the current {@link Problem} or
 * {@link ProblemContext}.
 *
 * <p>Supported placeholders:
 *
 * <ul>
 *   <li><b>For {@code type} override:</b>
 *       <ul>
 *         <li>{@code {problem.type}} - replaced with the original problem's {@code type}
 *         <li>{@code {context.<key>}} - replaced with the value for {@code key} from the current
 *             {@link ProblemContext}
 *       </ul>
 *   <li><b>For {@code title} override:</b>
 *       <ul>
 *         <li>{@code {problem.title}} - replaced with the original problem's {@code title}
 *         <li>{@code {context.<key>}} - replaced with the value for {@code key} from the current
 *             {@link ProblemContext}
 *       </ul>
 *   <li><b>For {@code instance} override:</b>
 *       <ul>
 *         <li>{@code {problem.instance}} - replaced with the original problem's {@code instance}
 *         <li>{@code {context.<key>}} - replaced with the value for {@code key} from the current
 *             {@link ProblemContext}
 *       </ul>
 * </ul>
 *
 * <p>If an override template contains a {@code {context.<key>}} placeholder whose value is absent
 * or empty in the context, the override is skipped entirely for that field.
 *
 * <p>If an override template produces the same value as the existing field, no change is applied.
 * If no override results in a change, the original {@link Problem} instance is returned unchanged.
 *
 * <p>Example configuration:
 *
 * <pre>{@code
 * problem4j.type-override=https://errors.example.com/{problem.type}
 * problem4j.title-override={problem.title} on {context.env}
 * problem4j.instance-override=/errors/{context.traceId}
 * }</pre>
 *
 * @since 1.2.0
 */
public class DefaultProblemPostProcessor implements ProblemPostProcessor {

  /**
   * Prefix for context placeholders in override templates. For example, a placeholder like {@code
   * {context.userId}} would look for a value with key {@code userId} in the {@link ProblemContext}.
   *
   * @since 3.0.0
   */
  protected static final String CONTEXT_PREFIX = "context.";

  /**
   * Regular expression pattern to identify placeholders in override templates. It matches any
   * substring enclosed in curly braces, such as {@code {problem.type}} or {@code {context.userId}}.
   *
   * @since 3.0.0
   */
  protected static final String PLACEHOLDER_REGEX = "\\{([^}]+)}";

  /**
   * Compiled pattern for detecting placeholders in override templates. This is used to find and
   * process placeholders when applying overrides.
   *
   * @since 3.0.0
   */
  protected static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);

  private final PostProcessorSettings settings;

  /**
   * Constructs a new {@link DefaultProblemPostProcessor}.
   *
   * @param settings the post-processor settings to use
   * @since 1.2.0
   */
  public DefaultProblemPostProcessor(PostProcessorSettings settings) {
    this.settings = settings;
  }

  /**
   * Applies configured overrides to {@code type}, {@code title}, and/or {@code instance}.
   *
   * <p>About {@code about:blank}: when the original type is {@code null} or {@link
   * Problem#BLANK_TYPE}, the placeholder {@code {problem.type}} resolves to an empty string
   * (templates naturally collapse). If the template is exactly {@code {problem.type}}, the original
   * {@code about:blank} value is preserved instead of becoming empty.
   *
   * @since 1.2.0
   */
  @Override
  public Problem process(@Nullable ProblemContext context, Problem problem) {
    if (context == null) {
      context = ProblemContext.create();
    }
    problem = overrideProblemType(context, problem);
    problem = overrideProblemTitle(context, problem);
    problem = overrideProblemInstance(context, problem);
    return problem;
  }

  /**
   * Applies a {@code type} override based on the configured template.
   *
   * @param context the problem context
   * @param problem the original problem
   * @return the updated problem if override was applied, or the original problem unchanged
   * @since 1.2.0
   */
  protected Problem overrideProblemType(ProblemContext context, Problem problem) {
    if (!StringUtils.hasLength(settings.getTypeOverride())) {
      return problem;
    }

    String template = settings.getTypeOverride();
    boolean requiresProblemType = template.contains("{problem.type}");
    boolean hasProblemType = problem.isTypeNonBlank();

    if (canOverride(requiresProblemType, hasProblemType)
        && allContextPlaceholdersSatisfied(template, context)) {
      Optional<String> newTypeCandidate = overrideType(context, problem);
      if (newTypeCandidate.isPresent()) {
        return problem.toBuilder().type(newTypeCandidate.get()).build();
      }
    }
    return problem;
  }

  /**
   * Determines whether a field can be overridden given whether its problem-field placeholder is
   * required and available.
   *
   * @param requiresProblemField whether the template requires the problem field value
   * @param hasProblemField whether the problem has that field value
   * @return true if override is allowed
   * @since 1.2.0
   */
  protected final boolean canOverride(boolean requiresProblemField, boolean hasProblemField) {
    return !requiresProblemField || hasProblemField;
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
   * @since 1.2.0
   */
  protected Optional<String> overrideType(ProblemContext context, Problem problem) {
    if (!problem.isTypeNonBlank() || !StringUtils.hasLength(settings.getTypeOverride())) {
      return Optional.empty();
    }

    String template = settings.getTypeOverride();
    String original = stringOrEmpty(problem.getType());
    String resolved = template.replace("{problem.type}", original);
    resolved = resolveContextPlaceholders(resolved, context);

    if (hasRemainingUnknownPlaceholders(resolved)) {
      return Optional.empty();
    }

    return Optional.of(resolved).filter(StringUtils::hasLength);
  }

  /**
   * Applies a {@code title} override based on the configured template.
   *
   * @param context the problem context
   * @param problem the original problem
   * @return the updated problem if override was applied, or the original problem unchanged
   * @since 3.0.0
   */
  protected Problem overrideProblemTitle(ProblemContext context, Problem problem) {
    if (!StringUtils.hasLength(settings.getTitleOverride())) {
      return problem;
    }

    String template = settings.getTitleOverride();
    boolean requiresProblemTitle = template.contains("{problem.title}");
    boolean hasProblemTitle = StringUtils.hasLength(problem.getTitle());

    if (canOverride(requiresProblemTitle, hasProblemTitle)
        && allContextPlaceholdersSatisfied(template, context)) {
      Optional<String> newTitleCandidate = overrideTitle(context, problem);
      if (newTitleCandidate.isPresent()) {
        return problem.toBuilder().title(newTitleCandidate.get()).build();
      }
    }
    return problem;
  }

  /**
   * Because this implementation does not try to resolve dynamic fields, it does not reuse the code
   * from {@code ProblemPostProcessor}. Instead, we rely on simple substring replacement in form of
   * {@link String#replace(CharSequence, CharSequence)}, and then removing all remaining unknown
   * variables.
   *
   * @param context the problem context
   * @param problem the original problem
   * @return an optional containing the new title if override is possible
   * @see io.github.problem4j.spring.web.ProblemPostProcessor
   * @since 3.0.0
   */
  protected Optional<String> overrideTitle(ProblemContext context, Problem problem) {
    if (!StringUtils.hasLength(problem.getTitle())
        || !StringUtils.hasLength(settings.getTitleOverride())) {
      return Optional.empty();
    }

    String template = settings.getTitleOverride();
    String original = stringOrEmpty(problem.getTitle());
    String resolved = template.replace("{problem.title}", original);
    resolved = resolveContextPlaceholders(resolved, context);

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
   * @return the updated problem if override was applied, or the original problem unchanged
   * @since 1.2.0
   */
  protected Problem overrideProblemInstance(ProblemContext context, Problem problem) {
    if (!StringUtils.hasLength(settings.getInstanceOverride())) {
      return problem;
    }

    String template = settings.getInstanceOverride();
    boolean needsProblemInstance = template.contains("{problem.instance}");
    boolean hasProblemInstance =
        problem.getInstance() != null && StringUtils.hasLength(problem.getInstance().toString());

    if (canOverride(needsProblemInstance, hasProblemInstance)
        && allContextPlaceholdersSatisfied(template, context)) {
      String newInstance = overrideInstance(context, problem);
      if (StringUtils.hasLength(newInstance)
          && !newInstance.equals(stringOrEmpty(problem.getInstance()))) {
        return problem.toBuilder().instance(newInstance).build();
      }
    }
    return problem;
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
   * @since 1.2.0
   */
  protected String overrideInstance(ProblemContext context, Problem problem) {
    if (!StringUtils.hasLength(settings.getInstanceOverride())) {
      return stringOrEmpty(problem.getInstance());
    }

    String template = settings.getInstanceOverride();
    String instanceValue = stringOrEmpty(problem.getInstance());

    template = template.replace("{problem.instance}", instanceValue);
    template = resolveContextPlaceholders(template, context);

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
   * @since 1.2.0
   */
  protected final String stringOrEmpty(@Nullable Object value) {
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
   * @since 1.2.0
   */
  protected boolean hasRemainingUnknownPlaceholders(String value) {
    return PLACEHOLDER_PATTERN.matcher(value).find();
  }

  /**
   * Returns the post-processor configuration settings.
   *
   * @return the post-processor settings
   * @since 1.2.0
   */
  protected final PostProcessorSettings getSettings() {
    return settings;
  }

  private boolean allContextPlaceholdersSatisfied(String template, ProblemContext context) {
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
    while (matcher.find()) {
      String key = matcher.group(1);
      if (key.startsWith(CONTEXT_PREFIX)
          && !StringUtils.hasLength(context.get(key.substring(CONTEXT_PREFIX.length())))) {
        return false;
      }
    }
    return true;
  }

  private String resolveContextPlaceholders(String template, ProblemContext context) {
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1);
      if (key.startsWith(CONTEXT_PREFIX)) {
        String value = stringOrEmpty(context.get(key.substring(CONTEXT_PREFIX.length())));
        matcher.appendReplacement(result, Matcher.quoteReplacement(value));
      } else {
        matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
      }
    }
    matcher.appendTail(result);
    return result.toString();
  }
}
