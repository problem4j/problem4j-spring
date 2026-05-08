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

import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.Optional;

/**
 * Registry that provides access to {@link ProblemResolver} instances based on exception types.
 *
 * <p>Implementations are responsible for locating the most specific resolver for a given {@link
 * Exception} class, typically by checking class assignability and caching results to improve
 * performance.
 *
 * @since 1.2.0
 */
@FunctionalInterface
public interface ProblemResolverStore {

  /**
   * Finds the most specific {@link ProblemResolver} for the given exception class.
   *
   * <p>This method searches the store for all resolvers whose keys are assignable from the provided
   * exception class. If multiple resolvers match, it returns the one closest in the class hierarchy
   * (i.e., the most specific resolver). If no resolver is found, an empty {@link Optional} is
   * returned. Results are cached to optimize repeated lookups for the same class.
   *
   * @param clazz the exception class for which to find a resolver
   * @return an {@link Optional} containing the most specific {@link ProblemResolver} if found,
   *     otherwise an empty {@link Optional}
   * @since 1.2.0
   */
  Optional<ProblemResolver> findResolver(Class<? extends Exception> clazz);
}
