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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.resolver.AbstractProblemResolver;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

class DefaultProblemResolverStoreTest {

  private static class MyBaseException extends Exception {}

  private static class MySubException extends MyBaseException {}

  private static class MySubSubException extends MySubException {}

  private static class OtherException extends Exception {}

  private static class TestResolver extends AbstractProblemResolver {
    TestResolver(Class<? extends Exception> clazz) {
      super(clazz);
    }

    @Override
    public Problem resolve(
        ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
      return Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
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
