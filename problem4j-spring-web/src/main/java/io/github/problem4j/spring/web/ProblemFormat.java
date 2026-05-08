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

import org.jspecify.annotations.Nullable;

/**
 * Defines a contract for formatting problem detail field and property names (mostly before they are
 * included in a {@code Problem} response).
 *
 * <p>Implementations can customize how details and property names are presented - for example, by
 * applying localization, case formatting, or message templating.
 *
 * @see io.github.problem4j.core.Problem
 * @since 1.2.0
 */
@FunctionalInterface
public interface ProblemFormat {

  /**
   * Returns the identity {@link ProblemFormat} that leaves all input unchanged.
   *
   * @return the identity instance
   * @since 3.0.0
   */
  static ProblemFormat identity() {
    return IdentityProblemFormat.INSTANCE;
  }

  /**
   * Format {@code detail} field of {@code Problem} model.
   *
   * @param detail the detail string to format
   * @return the formatted detail string
   * @see io.github.problem4j.core.Problem
   * @since 1.2.0
   */
  @Nullable String formatDetail(@Nullable String detail);
}
