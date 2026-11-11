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

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thrown on application startup if multiple {@code ProblemResolver} beans supporting the same
 * exception class share the highest precedence, and {@code problem4j.duplicate-resolver-policy} is
 * {@code fail} (default).
 *
 * <p>Reported as a startup failure description by {@code DuplicateProblemResolverFailureAnalyzer}.
 *
 * @since 3.1.0
 */
public class DuplicateProblemResolverException extends IllegalStateException {

  @Serial private static final long serialVersionUID = 1L;

  private final Map<String, List<String>> conflicts;

  /**
   * Creates a new {@code DuplicateProblemResolverException}.
   *
   * @param conflicts names of exception classes, mapped to descriptions of the resolver beans
   *     supporting them with equal precedence, in form of {@code 'beanName' (className)}
   * @since 3.1.0
   */
  public DuplicateProblemResolverException(Map<String, List<String>> conflicts) {
    super(buildMessage(conflicts));
    Map<String, List<String>> copy = new LinkedHashMap<>();
    conflicts.forEach(
        (exceptionClass, resolvers) -> copy.put(exceptionClass, List.copyOf(resolvers)));
    this.conflicts = Collections.unmodifiableMap(copy);
  }

  /**
   * Returns the conflicting resolver beans.
   *
   * @return names of exception classes, mapped to descriptions of the resolver beans supporting
   *     them with equal precedence, in form of {@code 'beanName' (className)}
   * @since 3.1.0
   */
  public Map<String, List<String>> getConflicts() {
    return conflicts;
  }

  private static String buildMessage(Map<String, List<String>> conflicts) {
    List<String> entries = new ArrayList<>(conflicts.size());
    conflicts.forEach((exceptionClass, resolvers) ->
        entries.add(exceptionClass + ": " + String.join(", ", resolvers)));
    return "Multiple ProblemResolver beans with equal order for "
        + String.join("; for ", entries)
        + ". Use @Order or Ordered to set their precedence.";
  }
}
