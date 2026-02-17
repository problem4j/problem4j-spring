/*
 * Copyright (c) 2025-2026 The Problem4J Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.problem4j.spring.web;

import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * {@link ProblemResolverStore} implementation that caches resolver lookups for better performance.
 *
 * <p>Results are stored in an internal cache to avoid repeated resolution.
 */
public class CachingProblemResolverStore implements ProblemResolverStore {

  private final ProblemResolverStore delegate;
  private final ResolverCache cache;

  /**
   * Creates a new store initialized with the given delegate and an unbounded cache.
   *
   * @param delegate the delegate store to use for resolver lookups
   */
  public CachingProblemResolverStore(ProblemResolverStore delegate) {
    this(delegate, -1);
  }

  /**
   * Creates a new store with an LRU cache limited to maxEntries.
   *
   * @param delegate the delegate store to use for resolver lookups
   * @param maxCacheSize maximum number of cached entries (unbounded if {@code -1})
   */
  public CachingProblemResolverStore(ProblemResolverStore delegate, int maxCacheSize) {
    this.delegate = delegate;
    this.cache = maxCacheSize > 0 ? new EvictingCache(maxCacheSize) : new NonEvictingCache();
  }

  /**
   * Returns a {@link ProblemResolver} for the given exception class.
   *
   * <p>Results are cached for subsequent lookups.
   *
   * @param clazz exception class to resolve
   * @return an {@link Optional} containing the matching resolver, or empty if none found
   */
  @Override
  public Optional<ProblemResolver> findResolver(Class<? extends Exception> clazz) {
    return cache.computeIfAbsent(clazz, delegate::findResolver);
  }

  /**
   * Simple abstraction for a thread-safe cache used to resolve {@link ProblemResolver} instances
   * based on an exception type. Implementations may or may not support eviction.
   */
  private interface ResolverCache {

    /**
     * Returns the cached resolver for the given exception type, computing and storing it if it is
     * not already present. Implementations must ensure thread safety.
     */
    Optional<ProblemResolver> computeIfAbsent(
        Class<? extends Exception> clazz,
        Function<Class<? extends Exception>, Optional<ProblemResolver>> supplier);
  }

  /**
   * Thread-safe LRU cache backed by a synchronized {@link LinkedHashMap}. Stores up to a fixed
   * number of entries and evicts the least recently used one when the limit is exceeded.
   */
  private static class EvictingCache implements ResolverCache {

    private final int maxCacheSize;

    private final Map<Class<? extends Exception>, Optional<ProblemResolver>> cache;

    /**
     * Creates an LRU cache with the given maximum number of entries. When the limit is exceeded,
     * the least recently used entry is evicted.
     */
    private EvictingCache(int maxCacheSize) {
      this.maxCacheSize = maxCacheSize;

      // 16 is the default initial capacity of HashMap;
      // 0.75f is the default load factor;
      // accessOrder=true - the last accessed entry is moved to the end of the underlying list
      this.cache =
          new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(
                Map.Entry<Class<? extends Exception>, Optional<ProblemResolver>> eldest) {
              return size() > EvictingCache.this.maxCacheSize;
            }
          };
    }

    /**
     * Computes or retrieves a cached resolver for the given exception type. The entire operation is
     * synchronized to ensure thread safety and consistent eviction behavior.
     */
    @Override
    public synchronized Optional<ProblemResolver> computeIfAbsent(
        Class<? extends Exception> clazz,
        Function<Class<? extends Exception>, Optional<ProblemResolver>> supplier) {
      return cache.computeIfAbsent(clazz, supplier);
    }
  }

  /**
   * Thread-safe unbounded cache backed by a {@link ConcurrentHashMap}. Stores all resolved entries
   * without eviction and supports concurrent access.
   */
  private static class NonEvictingCache implements ResolverCache {

    private final Map<Class<? extends Exception>, Optional<ProblemResolver>> cache;

    /**
     * Creates an unbounded thread-safe cache backed by a {@link ConcurrentHashMap}. No eviction is
     * performed.
     */
    private NonEvictingCache() {
      this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Computes or retrieves a cached resolver for the given exception type. Safe concurrent updates
     * are enforced via {@link ConcurrentHashMap#computeIfAbsent}.
     */
    @Override
    public Optional<ProblemResolver> computeIfAbsent(
        Class<? extends Exception> clazz,
        Function<Class<? extends Exception>, Optional<ProblemResolver>> supplier) {
      return cache.computeIfAbsent(clazz, supplier);
    }
  }
}
