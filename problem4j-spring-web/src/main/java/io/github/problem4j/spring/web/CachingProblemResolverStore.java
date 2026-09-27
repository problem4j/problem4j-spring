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

import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * {@link ProblemResolverStore} implementation that caches resolver lookups for better performance.
 *
 * <p>Results are stored in an internal cache to avoid repeated resolution.
 *
 * @since 1.2.0
 */
public class CachingProblemResolverStore implements ProblemResolverStore {

  private final ProblemResolverStore delegate;
  private final Map<Class<? extends Exception>, Optional<ProblemResolver>> cache;
  private final Map<ScopedKey, Optional<ProblemResolver>> scopedCache;

  /**
   * Creates a new store initialized with the given delegate and an unbounded cache.
   *
   * @param delegate the delegate store to use for resolver lookups
   * @since 1.2.0
   */
  public CachingProblemResolverStore(ProblemResolverStore delegate) {
    this.delegate = delegate;
    this.cache = new ConcurrentHashMap<>();
    this.scopedCache = new ConcurrentHashMap<>();
  }

  /**
   * Returns a {@link ProblemResolver} for the given exception class.
   *
   * <p>Results are cached for subsequent lookups.
   *
   * @param clazz exception class to resolve
   * @return an {@link Optional} containing the matching resolver, or empty if none found
   * @since 1.2.0
   */
  @Override
  public Optional<ProblemResolver> findResolver(Class<? extends Exception> clazz) {
    return cache.computeIfAbsent(clazz, delegate::findResolver);
  }

  /**
   * Returns a {@link ProblemResolver} for the given exception class thrown from a handler of the
   * given type.
   *
   * <p>Results are cached per handler type for subsequent lookups.
   *
   * @param clazz exception class to resolve
   * @param handlerType the type of the handler the exception was thrown from, or {@code null} if
   *     unknown
   * @return an {@link Optional} containing the matching resolver, or empty if none found
   * @since 3.1.0
   */
  @Override
  public Optional<ProblemResolver> findResolver(
      Class<? extends Exception> clazz, @Nullable Class<?> handlerType) {
    if (handlerType == null) {
      return findResolver(clazz);
    }
    return scopedCache.computeIfAbsent(
        new ScopedKey(handlerType, clazz),
        key -> delegate.findResolver(key.exceptionClass(), key.handlerType()));
  }

  private record ScopedKey(Class<?> handlerType, Class<? extends Exception> exceptionClass) {}
}
