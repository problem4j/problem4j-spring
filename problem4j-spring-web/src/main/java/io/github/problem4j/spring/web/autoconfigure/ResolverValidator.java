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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * Validates that for each exception class a single {@link ProblemResolver} bean has the highest
 * precedence, as determined by {@link org.springframework.core.Ordered} or {@link
 * org.springframework.core.annotation.Order @Order}.
 */
final class ResolverValidator {

  /**
   * Validates resolver precedence.
   *
   * @param problemResolvers resolvers by bean name
   * @throws IllegalStateException if multiple resolvers supporting the same exception class share
   *     the highest precedence; the message lists every such exception class
   */
  static void validate(Map<String, ? extends ProblemResolver> problemResolvers) {
    validate(problemResolvers, AnnotationAwareOrderComparator.INSTANCE);
  }

  /**
   * Validates resolver precedence using the given comparator.
   *
   * @param problemResolvers resolvers by bean name
   * @param orderComparator comparator deciding precedence of resolvers (lower wins)
   * @throws IllegalStateException if multiple resolvers supporting the same exception class share
   *     the highest precedence; the message lists every such exception class
   */
  static void validate(
      Map<String, ? extends ProblemResolver> problemResolvers, Comparator<Object> orderComparator) {
    Map<Class<? extends Exception>, List<Map.Entry<String, ProblemResolver>>> byExceptionClass =
        new LinkedHashMap<>();
    problemResolvers.forEach(
        (beanName, resolver) ->
            byExceptionClass
                .computeIfAbsent(resolver.getExceptionClass(), key -> new ArrayList<>())
                .add(Map.entry(beanName, resolver)));

    List<String> conflicts = new ArrayList<>();
    byExceptionClass.forEach(
        (exceptionClass, entries) ->
            findConflict(exceptionClass, entries, orderComparator).ifPresent(conflicts::add));

    if (!conflicts.isEmpty()) {
      throw new IllegalStateException(
          "Multiple ProblemResolver beans with equal order for "
              + String.join("; for ", conflicts)
              + ". Use @Order or Ordered to set their precedence.");
    }
  }

  private static Optional<String> findConflict(
      Class<? extends Exception> exceptionClass,
      List<Map.Entry<String, ProblemResolver>> entries,
      Comparator<Object> orderComparator) {
    entries.sort(Map.Entry.comparingByValue(orderComparator));
    ProblemResolver winner = entries.get(0).getValue();

    List<String> tied =
        entries.stream()
            .filter(entry -> orderComparator.compare(winner, entry.getValue()) == 0)
            .map(
                entry -> "'" + entry.getKey() + "' (" + entry.getValue().getClass().getName() + ")")
            .toList();

    if (tied.size() < 2) {
      return Optional.empty();
    }
    return Optional.of(exceptionClass.getName() + ": " + String.join(", ", tied));
  }

  private ResolverValidator() {}
}
