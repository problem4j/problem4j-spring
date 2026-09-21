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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

class ResolverValidatorTest {

  @Test
  void givenNoResolvers_whenValidating_thenPasses() {
    assertThatCode(() -> ResolverValidator.validate(Map.of())).doesNotThrowAnyException();
  }

  @Test
  void givenSingleResolverPerExceptionClass_whenValidating_thenPasses() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "first", new UnorderedResolver(FirstException.class),
            "second", new UnorderedResolver(SecondException.class));

    assertThatCode(() -> ResolverValidator.validate(resolvers)).doesNotThrowAnyException();
  }

  @Test
  void givenResolversForSameExceptionWithDifferentOrder_whenValidating_thenPasses() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "high", new OrderedResolver(FirstException.class, 1),
            "low", new OrderedResolver(FirstException.class, 2));

    assertThatCode(() -> ResolverValidator.validate(resolvers)).doesNotThrowAnyException();
  }

  @Test
  void givenOrderedAndUnorderedResolversForSameException_whenValidating_thenPasses() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "unordered", new UnorderedResolver(FirstException.class),
            "ordered", new AnnotatedResolver());

    assertThatCode(() -> ResolverValidator.validate(resolvers)).doesNotThrowAnyException();
  }

  @Test
  void givenTieBelowUniqueWinner_whenValidating_thenPasses() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "winner", new OrderedResolver(FirstException.class, 1),
            "tiedA", new OrderedResolver(FirstException.class, 5),
            "tiedB", new OrderedResolver(FirstException.class, 5));

    assertThatCode(() -> ResolverValidator.validate(resolvers)).doesNotThrowAnyException();
  }

  @Test
  void givenEqualOrderForDifferentExceptionClasses_whenValidating_thenPasses() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "first", new OrderedResolver(FirstException.class, 1),
            "second", new OrderedResolver(SecondException.class, 1));

    assertThatCode(() -> ResolverValidator.validate(resolvers)).doesNotThrowAnyException();
  }

  @Test
  void givenUnorderedResolversForSameException_whenValidating_thenThrows() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "firstResolver", new UnorderedResolver(FirstException.class),
            "secondResolver", new UnorderedResolver(FirstException.class));

    assertThatThrownBy(() -> ResolverValidator.validate(resolvers))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("equal order for " + FirstException.class.getName())
        .hasMessageContaining("'firstResolver' (" + UnorderedResolver.class.getName() + ")")
        .hasMessageContaining("'secondResolver' (" + UnorderedResolver.class.getName() + ")");
  }

  @Test
  void givenOrderedResolversWithEqualOrder_whenValidating_thenThrows() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "firstResolver", new OrderedResolver(FirstException.class, 3),
            "secondResolver", new OrderedResolver(FirstException.class, 3));

    assertThatThrownBy(() -> ResolverValidator.validate(resolvers))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("'firstResolver'")
        .hasMessageContaining("'secondResolver'");
  }

  @Test
  void givenOrderAnnotationAndOrderedInterfaceWithEqualValue_whenValidating_thenThrows() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "annotated", new AnnotatedResolver(),
            "ordered", new OrderedResolver(FirstException.class, 5));

    assertThatThrownBy(() -> ResolverValidator.validate(resolvers))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("'annotated'")
        .hasMessageContaining("'ordered'");
  }

  @Test
  void givenTieAtTopAndLowerPriorityResolver_whenValidating_thenNamesOnlyTiedBeans() {
    Map<String, ProblemResolver> resolvers =
        resolvers(
            "tiedA", new OrderedResolver(FirstException.class, 1),
            "tiedB", new OrderedResolver(FirstException.class, 1),
            "lower", new OrderedResolver(FirstException.class, 2));

    assertThatThrownBy(() -> ResolverValidator.validate(resolvers))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("'tiedA'")
        .hasMessageContaining("'tiedB'")
        .hasMessageNotContaining("'lower'");
  }

  private static Map<String, ProblemResolver> resolvers(Object... namesAndResolvers) {
    Map<String, ProblemResolver> resolvers = new LinkedHashMap<>();
    for (int i = 0; i < namesAndResolvers.length; i += 2) {
      resolvers.put((String) namesAndResolvers[i], (ProblemResolver) namesAndResolvers[i + 1]);
    }
    return resolvers;
  }

  static class UnorderedResolver implements ProblemResolver {

    private final Class<? extends Exception> exceptionClass;

    UnorderedResolver(Class<? extends Exception> exceptionClass) {
      this.exceptionClass = exceptionClass;
    }

    @Override
    public Class<? extends Exception> getExceptionClass() {
      return exceptionClass;
    }

    @Override
    public Problem resolve(
        ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
      return Problem.builder().status(400).build();
    }
  }

  static class OrderedResolver extends UnorderedResolver implements Ordered {

    private final int order;

    OrderedResolver(Class<? extends Exception> exceptionClass, int order) {
      super(exceptionClass);
      this.order = order;
    }

    @Override
    public int getOrder() {
      return order;
    }
  }

  @Order(5)
  static class AnnotatedResolver extends UnorderedResolver {

    AnnotatedResolver() {
      super(FirstException.class);
    }
  }

  static class FirstException extends RuntimeException {}

  static class SecondException extends RuntimeException {}
}
