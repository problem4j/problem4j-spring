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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

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

  private static final Logger log = LoggerFactory.getLogger(DefaultProblemResolverStore.class);

  private final Map<Class<? extends Exception>, Registration> resolvers;
  private final ClassDistanceEvaluation classDistanceEvaluation;

  /**
   * Creates a new store initialized with the given resolvers.
   *
   * <p>If multiple resolvers support the same exception class, the one with the highest precedence
   * is used, as determined by {@link org.springframework.core.Ordered} or {@link
   * org.springframework.core.annotation.Order @Order} (lower value wins). On tie, the first one in
   * the list is used.
   *
   * @param problemResolvers list of available {@link ProblemResolver} instances
   * @throws NullPointerException if any resolver or its exception class is {@code null}
   * @since 1.2.0
   */
  public DefaultProblemResolverStore(List<? extends ProblemResolver> problemResolvers) {
    this(problemResolvers, new GraphClassDistanceEvaluation(SUPERCLASS));
  }

  /**
   * Creates a new store initialized with the given resolvers and a specific class distance
   * evaluation strategy.
   *
   * <p>If multiple resolvers support the same exception class, the one with the highest precedence
   * is used, as determined by {@link org.springframework.core.Ordered} or {@link
   * org.springframework.core.annotation.Order @Order} (lower value wins). On tie, the first one in
   * the list is used.
   *
   * @param problemResolvers list of available {@link ProblemResolver} instances
   * @param classDistanceEvaluation the strategy used to evaluate the distance between exception
   *     classes (e.g., when finding the best resolver for a specific exception type).
   * @throws NullPointerException if any resolver or its exception class is {@code null}
   * @since 1.2.0
   */
  public DefaultProblemResolverStore(
      List<? extends ProblemResolver> problemResolvers,
      ClassDistanceEvaluation classDistanceEvaluation) {
    this.resolvers =
        index(
            problemResolvers.stream().map(resolver -> new Registration(null, resolver)),
            AnnotationAwareOrderComparator.INSTANCE);
    this.classDistanceEvaluation = classDistanceEvaluation;
  }

  /**
   * Creates a new store initialized with the given resolvers, keyed by bean name.
   *
   * <p>Bean names are used only for diagnostics, i.e. debug logging of which resolver takes
   * precedence when multiple resolvers support the same exception class. Resolver precedence rules
   * are the same as in {@link #DefaultProblemResolverStore(List)}.
   *
   * @param problemResolvers available {@link ProblemResolver} instances by bean name
   * @throws NullPointerException if any resolver or its exception class is {@code null}
   * @since 3.1.0
   */
  public DefaultProblemResolverStore(Map<String, ? extends ProblemResolver> problemResolvers) {
    this(problemResolvers, new GraphClassDistanceEvaluation(SUPERCLASS));
  }

  /**
   * Creates a new store initialized with the given resolvers, keyed by bean name, and a specific
   * class distance evaluation strategy.
   *
   * <p>Bean names are used only for diagnostics, i.e. debug logging of which resolver takes
   * precedence when multiple resolvers support the same exception class. Resolver precedence rules
   * are the same as in {@link #DefaultProblemResolverStore(List, ClassDistanceEvaluation)}.
   *
   * @param problemResolvers available {@link ProblemResolver} instances by bean name
   * @param classDistanceEvaluation the strategy used to evaluate the distance between exception
   *     classes (e.g., when finding the best resolver for a specific exception type).
   * @throws NullPointerException if any resolver or its exception class is {@code null}
   * @since 3.1.0
   */
  public DefaultProblemResolverStore(
      Map<String, ? extends ProblemResolver> problemResolvers,
      ClassDistanceEvaluation classDistanceEvaluation) {
    this(problemResolvers, classDistanceEvaluation, AnnotationAwareOrderComparator.INSTANCE);
  }

  /**
   * Creates a new store initialized with the given resolvers, keyed by bean name, a specific class
   * distance evaluation strategy and a comparator deciding resolver precedence.
   *
   * <p>If multiple resolvers support the same exception class, the one ordered first by {@code
   * orderComparator} is used. On tie, the first one in the map is used. Bean names are used only
   * for diagnostics.
   *
   * @param problemResolvers available {@link ProblemResolver} instances by bean name
   * @param classDistanceEvaluation the strategy used to evaluate the distance between exception
   *     classes (e.g., when finding the best resolver for a specific exception type).
   * @param orderComparator comparator deciding precedence of resolvers supporting the same
   *     exception class (lower wins)
   * @throws NullPointerException if any resolver or its exception class is {@code null}
   * @since 3.1.0
   */
  public DefaultProblemResolverStore(
      Map<String, ? extends ProblemResolver> problemResolvers,
      ClassDistanceEvaluation classDistanceEvaluation,
      Comparator<Object> orderComparator) {
    this.resolvers =
        index(
            problemResolvers.entrySet().stream()
                .map(entry -> new Registration(entry.getKey(), entry.getValue())),
            orderComparator);
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
    List<Registration> candidates = new ArrayList<>();
    for (Map.Entry<Class<? extends Exception>, Registration> entry : resolvers.entrySet()) {
      if (entry.getKey().isAssignableFrom(clazz)) {
        candidates.add(entry.getValue());
      }
    }
    return candidates.stream()
        .min(comparingInt(r -> calculateDistance(r.getResolver(), clazz)))
        .map(Registration::getResolver);
  }

  private int calculateDistance(ProblemResolver resolver, Class<? extends Exception> clazz) {
    return classDistanceEvaluation.calculate(clazz, resolver.getExceptionClass());
  }

  private static Map<Class<? extends Exception>, Registration> index(
      Stream<Registration> registrations, Comparator<Object> orderComparator) {
    List<Registration> sorted = new ArrayList<>(registrations.toList());
    sorted.sort((left, right) -> orderComparator.compare(left.getResolver(), right.getResolver()));

    Map<Class<? extends Exception>, List<Registration>> byExceptionClass = new HashMap<>();
    sorted.forEach(
        registration ->
            byExceptionClass
                .computeIfAbsent(
                    registration.getResolver().getExceptionClass(), key -> new ArrayList<>())
                .add(registration));

    Map<Class<? extends Exception>, Registration> copy = new HashMap<>(byExceptionClass.size());
    byExceptionClass.forEach(
        (exceptionClass, contenders) -> {
          Registration winner = contenders.get(0);
          logDebugSelectedProblemResolver(exceptionClass, contenders, winner);
          copy.put(exceptionClass, winner);
        });
    return Map.copyOf(copy);
  }

  private static void logDebugSelectedProblemResolver(
      Class<? extends Exception> clazz, List<Registration> contenders, Registration winner) {
    if (log.isDebugEnabled()) {
      if (contenders.size() == 1) {
        log.debug("Selected {} bean as ProblemResolver for {}", winner, clazz.getName());
      } else {
        log.debug(
            "Selected {} as ProblemResolver for {}, skipping {}",
            winner,
            clazz.getName(),
            contenders.subList(1, contenders.size()));
      }
    }
  }

  private static final class Registration {

    private final @Nullable String beanName;
    private final ProblemResolver resolver;

    private Registration(@Nullable String beanName, ProblemResolver resolver) {
      this.beanName = beanName;
      this.resolver = requireNonNull(resolver);
    }

    private ProblemResolver getResolver() {
      return resolver;
    }

    @Override
    public String toString() {
      String className = resolver.getClass().getName();
      return beanName != null ? "'" + beanName + "' [" + className + "]" : className;
    }
  }
}
