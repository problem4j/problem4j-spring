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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.problem4j.spring.web.resolver.AbstractProblemResolver;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CachingProblemResolverStoreTest {

  private static class TestException extends Exception {}

  private static class TestResolver extends AbstractProblemResolver {
    TestResolver(Class<? extends Exception> clazz) {
      super(clazz);
    }
  }

  @Test
  void givenProblemResolverStore_whenFindingResolver_thenCacheIsUsed() {
    AtomicInteger counter = new AtomicInteger(0);
    ProblemResolver resolver =
        new TestResolver(TestException.class) {
          @Override
          public Class<? extends Exception> getExceptionClass() {
            counter.incrementAndGet();
            return super.getExceptionClass();
          }
        };

    CachingProblemResolverStore store =
        new CachingProblemResolverStore(new DefaultProblemResolverStore(List.of(resolver)));

    Optional<ProblemResolver> firstLookup = store.findResolver(TestException.class);

    assertTrue(firstLookup.isPresent(), "resolver should be present");
    assertEquals(1, counter.get(), "counter should increment once");

    Optional<ProblemResolver> secondLookup = store.findResolver(TestException.class);

    assertTrue(secondLookup.isPresent(), "resolver should still be present");
    assertEquals(1, counter.get(), "counter should not increment again");
  }

  @Test
  void givenNoMatchingResolver_whenFindingResolver_thenSearchIsCached() {
    AtomicInteger computeCounter = new AtomicInteger(0);

    CachingProblemResolverStore store =
        new CachingProblemResolverStore(
            clazz -> {
              computeCounter.incrementAndGet();
              return Optional.empty();
            });

    Optional<ProblemResolver> first = store.findResolver(TestException.class);
    assertTrue(first.isEmpty(), "should be empty for unmapped exception");

    Optional<ProblemResolver> second = store.findResolver(TestException.class);
    assertTrue(second.isEmpty(), "should still be empty");
    assertEquals(1, computeCounter.get(), "computeResolver() should be called only once");
  }

  private static class DummyResolver extends AbstractProblemResolver {
    DummyResolver(Class<? extends Exception> clazz) {
      super(clazz);
    }
  }

  @Test
  void whenManyThreadsLookupSameException_thenComputeResolverRunsOnce()
      throws InterruptedException, ExecutionException {
    AtomicInteger computeCounter = new AtomicInteger(0);

    DummyResolver resolver = new DummyResolver(IOException.class);

    CachingProblemResolverStore store =
        new CachingProblemResolverStore(
            clazz -> {
              computeCounter.incrementAndGet();
              return Optional.of(resolver);
            });

    int threadCount = 20;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    List<Future<Optional<ProblemResolver>>> futures;
    try {
      List<Callable<Optional<ProblemResolver>>> tasks = new ArrayList<>();

      for (int i = 0; i < threadCount; i++) {
        tasks.add(() -> store.findResolver(IOException.class));
      }

      futures = executor.invokeAll(tasks);
    } finally {
      executor.shutdown();
    }

    assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));

    for (Future<Optional<ProblemResolver>> f : futures) {
      assertTrue(f.get().isPresent());
      assertSame(resolver, f.get().get(), "all threads should get the same instance");
    }

    assertEquals(1, computeCounter.get(), "computeResolver() should run exactly once");
  }

  private static class Ex1 extends Exception {}

  private static class Ex2 extends Exception {}

  private static class Ex3 extends Exception {}

  @Test
  void givenLimitedCache_whenExceeded_thenEvictsLeastRecentlyUsed() {
    int maxCacheSize = 2;

    DummyResolver r1 = new DummyResolver(Ex1.class);
    DummyResolver r2 = new DummyResolver(Ex2.class);
    DummyResolver r3 = new DummyResolver(Ex3.class);

    Map<Class<? extends Exception>, ProblemResolver> resolvers =
        Map.of(
            Ex1.class, r1,
            Ex2.class, r2,
            Ex3.class, r3);

    Map<Class<? extends Exception>, AtomicInteger> counters = new HashMap<>();

    ProblemResolverStore delegate =
        clazz -> {
          counters.computeIfAbsent(clazz, k -> new AtomicInteger()).incrementAndGet();
          return Optional.ofNullable(resolvers.get(clazz));
        };

    CachingProblemResolverStore store = new CachingProblemResolverStore(delegate, maxCacheSize);

    // Fill cache with Ex1, Ex2
    assertTrue(store.findResolver(Ex1.class).isPresent());
    assertTrue(store.findResolver(Ex2.class).isPresent());

    // Touch Ex1 to make it most recently used (LRU order now: Ex2 oldest, Ex1 newest)
    assertTrue(store.findResolver(Ex1.class).isPresent());

    // Add Ex3 -> should evict Ex2
    assertTrue(store.findResolver(Ex3.class).isPresent());

    // Access Ex2 again -> should be cache miss and re-computed
    assertTrue(store.findResolver(Ex2.class).isPresent());

    assertEquals(1, counters.get(Ex1.class).get(), "Ex1 should be computed once");
    assertEquals(2, counters.get(Ex2.class).get(), "Ex2 should be recomputed after eviction");
    assertEquals(1, counters.get(Ex3.class).get(), "Ex3 should be computed once");
  }
}
