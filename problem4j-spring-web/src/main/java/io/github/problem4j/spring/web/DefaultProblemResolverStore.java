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

package io.github.problem4j.spring.web;

import static io.github.problem4j.spring.web.HierarchyTraversalMode.SUPERCLASS;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;

import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link ProblemResolverStore} implementation evaluates resolved based on class and its
 * inheritance.
 *
 * <p>Resolvers are matched to exceptions by assignability, preferring the most specific exception
 * type.
 *
 * @since 1.2.0
 */
public class DefaultProblemResolverStore implements ProblemResolverStore {

  private final Map<Class<? extends Exception>, ProblemResolver> resolvers;
  private final ClassDistanceEvaluation classDistanceEvaluation;

  /**
   * Creates a new store initialized with the given resolvers.
   *
   * @param problemResolvers list of available {@link ProblemResolver} instances
   * @throws NullPointerException if any resolver or its exception class is {@code null}
   * @since 1.2.0
   */
  public DefaultProblemResolverStore(List<ProblemResolver> problemResolvers) {
    this(problemResolvers, new GraphClassDistanceEvaluation(SUPERCLASS));
  }

  /**
   * Creates a new store initialized with the given resolvers and a specific class distance
   * evaluation strategy.
   *
   * @param problemResolvers list of available {@link ProblemResolver} instances
   * @param classDistanceEvaluation the strategy used to evaluate the distance between exception
   *     classes (e.g., when finding the best resolver for a specific exception type).
   * @throws NullPointerException if any resolver or its exception class is {@code null}
   * @since 1.2.0
   */
  public DefaultProblemResolverStore(
      List<ProblemResolver> problemResolvers, ClassDistanceEvaluation classDistanceEvaluation) {
    Map<Class<? extends Exception>, ProblemResolver> copy = new HashMap<>(problemResolvers.size());
    problemResolvers.forEach(
        resolver -> copy.put(resolver.getExceptionClass(), requireNonNull(resolver)));
    this.resolvers = Map.copyOf(copy);
    this.classDistanceEvaluation = classDistanceEvaluation;
  }

  /**
   * Returns a {@link ProblemResolver} for the given exception class.
   *
   * <p>This method searches for resolvers whose exception type is assignable from the given class,
   * selecting the most specific match.
   *
   * @param clazz exception class to resolve
   * @return an {@link Optional} containing the matching resolver, or empty if none found
   * @since 1.2.0
   */
  @Override
  public Optional<ProblemResolver> findResolver(Class<? extends Exception> clazz) {
    List<ProblemResolver> candidates = new ArrayList<>();
    for (Map.Entry<Class<? extends Exception>, ProblemResolver> entry : resolvers.entrySet()) {
      if (entry.getKey().isAssignableFrom(clazz)) {
        candidates.add(entry.getValue());
      }
    }
    return candidates.stream().min(comparingInt(r -> calculateDistance(r, clazz)));
  }

  private int calculateDistance(ProblemResolver resolver, Class<? extends Exception> clazz) {
    return classDistanceEvaluation.calculate(clazz, resolver.getExceptionClass());
  }
}
