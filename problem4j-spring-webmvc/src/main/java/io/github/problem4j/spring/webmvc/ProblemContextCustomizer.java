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

package io.github.problem4j.spring.webmvc;

import io.github.problem4j.core.ProblemContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Hook for modifying {@link ProblemContext} after {@link ProblemContextFilter} has built it, e.g.
 * to add a custom request identifier. All beans are invoked in their order and may overwrite any
 * entry, including {@code traceId}.
 *
 * @since 3.1.0
 */
@FunctionalInterface
public interface ProblemContextCustomizer {

  /**
   * Customizes the context of the current request.
   *
   * @param context the {@link ProblemContext} to customize
   * @param request the current {@link HttpServletRequest}
   * @param response the current {@link HttpServletResponse}
   * @since 3.1.0
   */
  void customize(ProblemContext context, HttpServletRequest request, HttpServletResponse response);
}
