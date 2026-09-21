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

import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * Validates that for each exception class a single {@link ProblemResolver} bean has the highest
 * precedence, as determined by {@link org.springframework.core.Ordered} or {@link
 * org.springframework.core.annotation.Order @Order}.
 */
final class ResolverValidator {

  private ResolverValidator() {}

  /**
   * Validates resolver precedence.
   *
   * @param problemResolvers resolvers by bean name
   * @throws IllegalStateException if multiple resolvers supporting the same exception class share
   *     the highest precedence
   */
  static void validate(Map<String, ? extends ProblemResolver> problemResolvers) {
    Map<Class<? extends Exception>, List<Map.Entry<String, ProblemResolver>>> byExceptionClass =
        new HashMap<>();
    problemResolvers.forEach(
        (beanName, resolver) ->
            byExceptionClass
                .computeIfAbsent(resolver.getExceptionClass(), key -> new ArrayList<>())
                .add(Map.entry(beanName, resolver)));

    byExceptionClass.forEach(ResolverValidator::validate);
  }

  private static void validate(
      Class<? extends Exception> exceptionClass, List<Map.Entry<String, ProblemResolver>> entries) {
    entries.sort(Map.Entry.comparingByValue(AnnotationAwareOrderComparator.INSTANCE));
    ProblemResolver winner = entries.get(0).getValue();

    List<String> tied =
        entries.stream()
            .filter(
                entry ->
                    AnnotationAwareOrderComparator.INSTANCE.compare(winner, entry.getValue()) == 0)
            .map(
                entry -> "'" + entry.getKey() + "' (" + entry.getValue().getClass().getName() + ")")
            .toList();

    if (tied.size() > 1) {
      throw new IllegalStateException(
          "Multiple ProblemResolver beans with equal order for "
              + exceptionClass.getName()
              + ": "
              + String.join(", ", tied)
              + ". Use @Order or Ordered to set their precedence.");
    }
  }
}
