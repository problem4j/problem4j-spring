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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.problem4j.spring.web.resolver.AbstractProblemResolver;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultProblemResolverStoreTest {

  private static class MyBaseException extends Exception {}

  private static class MySubException extends MyBaseException {}

  private static class MySubSubException extends MySubException {}

  private static class OtherException extends Exception {}

  private static class TestResolver extends AbstractProblemResolver {
    TestResolver(Class<? extends Exception> clazz) {
      super(clazz);
    }
  }

  private ProblemResolverStore store;

  @BeforeEach
  void setUp() {
    ProblemResolver baseResolver = new TestResolver(MyBaseException.class);
    ProblemResolver subResolver = new TestResolver(MySubException.class);
    ProblemResolver otherResolver = new TestResolver(OtherException.class);
    store = new DefaultProblemResolverStore(List.of(baseResolver, subResolver, otherResolver));
  }

  @Test
  void testExactMatch() {
    Optional<ProblemResolver> result = store.findResolver(MySubException.class);

    assertTrue(result.isPresent());
    assertEquals(MySubException.class, result.get().getExceptionClass());
  }

  @Test
  void testClosestSuperclassMatch() {
    Optional<ProblemResolver> result = store.findResolver(MySubSubException.class);

    assertTrue(result.isPresent());
    assertEquals(MySubException.class, result.get().getExceptionClass());
  }

  @Test
  void testBaseClassMatch() {
    Optional<ProblemResolver> result = store.findResolver(MyBaseException.class);

    assertTrue(result.isPresent());
    assertEquals(MyBaseException.class, result.get().getExceptionClass());
  }

  @Test
  void testNoMatchForUnrelatedException() {
    Optional<ProblemResolver> result = store.findResolver(Exception.class);

    assertTrue(result.isEmpty());
  }
}
