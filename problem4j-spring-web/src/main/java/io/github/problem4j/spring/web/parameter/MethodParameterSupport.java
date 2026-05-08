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

package io.github.problem4j.spring.web.parameter;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;

/**
 * Strategy for resolving method parameter names, supporting Spring's common binding annotations
 * such as {@code @RequestParam}, {@code @PathVariable}, {@code @RequestHeader}, and
 * {@code @ModelAttribute}. If a supported annotation is present and specifies an explicit name,
 * that name is returned; otherwise, the parameter's discovered name is used. Unknown or unsupported
 * annotations are ignored, and the parameter name resolution falls back to the default behavior
 * (e.g. using Java 8+ parameter name discovery if available).
 *
 * @since 1.2.0
 */
@FunctionalInterface
public interface MethodParameterSupport {

  /**
   * Resolve a stable logical name for a method parameter, honoring supported Spring binding
   * annotations. If an annotation supplies an explicit {@code name} or {@code value}, that wins;
   * otherwise falls back to the parameter's discovered name. Unknown or unsupported annotations are
   * ignored.
   *
   * @param parameter Spring {@link MethodParameter} (may be {@code null})
   * @return optional parameter name; empty if the input is {@code null}
   * @since 1.2.0
   */
  Optional<String> findParameterName(@Nullable MethodParameter parameter);
}
