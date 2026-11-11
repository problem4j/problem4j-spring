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

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Reports {@link DuplicateProblemResolverException} as a startup failure description, listing the
 * conflicting resolver beans for each exception class, instead of a stack trace of the bean
 * creation failure that wraps it.
 */
final class DuplicateProblemResolverFailureAnalyzer
    extends AbstractFailureAnalyzer<DuplicateProblemResolverException> {

  static final String ACTION =
      "Set precedence of these beans with @Order or Ordered, remove one of them, or set"
          + " problem4j.duplicate-resolver-policy to first or last to pick one by registration"
          + " order.";

  DuplicateProblemResolverFailureAnalyzer() {}

  @Override
  protected FailureAnalysis analyze(
      Throwable rootFailure, DuplicateProblemResolverException cause) {
    StringBuilder description =
        new StringBuilder(
            "Multiple ProblemResolver beans with equal order handle the same exception class:");
    description.append(System.lineSeparator());
    cause
        .getConflicts()
        .forEach((exceptionClass, resolvers) -> {
          description.append(System.lineSeparator()).append("    ").append(exceptionClass);
          description.append(':');
          resolvers.forEach(resolver ->
              description.append(System.lineSeparator()).append("        - ").append(resolver));
        });
    return new FailureAnalysis(description.toString(), ACTION, cause);
  }
}
