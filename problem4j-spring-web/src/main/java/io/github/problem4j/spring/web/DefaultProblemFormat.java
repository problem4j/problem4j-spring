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

import io.github.problem4j.spring.web.autoconfigure.ProblemProperties;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link ProblemFormat} that applies text transformations to problem
 * details according to a configured format.
 *
 * <p>Property {@link ProblemProperties#getDetailFormat()} determines formatting behavior -
 * lowercase, capitalized, or uppercase.
 *
 * <p>This class is typically registered automatically as a Spring bean, but can also be
 * instantiated directly.
 *
 * @see ProblemFormat
 * @see ProblemProperties.DetailFormat
 * @since 1.2.0
 */
public class DefaultProblemFormat implements ProblemFormat {

  private final String detailFormat;

  /**
   * Constructs a new {@link DefaultProblemFormat}.
   *
   * @param detailFormat the detail format string to use
   * @since 1.2.0
   */
  public DefaultProblemFormat(String detailFormat) {
    this.detailFormat = detailFormat;
  }

  /**
   * Applies formatting to the given {@code "detail"} text according to following rules.
   *
   * <pre>{@code
   * lowercase     "Validation failed"  will be transformed to  "validation failed"
   * capitalized   "Validation failed"  will be transformed to  "Validation failed"
   * uppercase     "Validation failed"  will be transformed to  "VALIDATION FAILED"
   *
   * (any other)   "Validation failed"  will be transformed to  "Validation failed"
   * }</pre>
   *
   * @param detail the raw text, may be {@code null}
   * @return the formatted text, or {@code null} if input was {@code null}
   * @since 1.2.0
   */
  @Override
  public @Nullable String formatDetail(@Nullable String detail) {
    if (!StringUtils.hasLength(detail)) {
      return detail;
    }
    return switch (detailFormat.toLowerCase(Locale.ROOT)) {
      case ProblemProperties.DetailFormat.LOWERCASE -> detail.toLowerCase(Locale.ROOT);
      case ProblemProperties.DetailFormat.CAPITALIZED -> capitalize(detail);
      case ProblemProperties.DetailFormat.UPPERCASE -> detail.toUpperCase(Locale.ROOT);
      default -> detail;
    };
  }

  private String capitalize(String detail) {
    return Character.toTitleCase(detail.charAt(0)) + detail.substring(1);
  }
}
