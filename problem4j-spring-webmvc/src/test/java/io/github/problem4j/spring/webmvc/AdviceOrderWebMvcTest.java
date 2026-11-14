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

package io.github.problem4j.spring.webmvc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.DefaultProblemMapper;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.ControllerAdviceBean;

class AdviceOrderWebMvcTest {

  private static final ProblemResolverStore STORE = new DefaultProblemResolverStore(List.of());

  @Test
  void givenAdvices_whenCheckingPrecedence_thenProblemAdvicesPrecedeCatchAllAdvice() {
    assertThat(ExceptionWebMvcAdvice.PRECEDENCE).isEqualTo(Ordered.LOWEST_PRECEDENCE);
    assertThat(ProblemExceptionWebMvcAdvice.PRECEDENCE).isEqualTo(Ordered.LOWEST_PRECEDENCE - 10);
    assertThat(ProblemResponseEntityExceptionHandler.PRECEDENCE)
        .isEqualTo(Ordered.LOWEST_PRECEDENCE - 10);
  }

  @SuppressWarnings("removal")
  @Test
  void givenAdvicesAndSubclasses_whenResolvingOrder_thenClassPrecedenceIsUsedAndInherited() {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.registerBean(
          ExceptionWebMvcAdvice.class,
          () ->
              new ExceptionWebMvcAdvice(
                  new DefaultProblemMapper(), STORE, ProblemPostProcessor.identity(), List.of()));
      context.registerBean(
          ProblemExceptionWebMvcAdvice.class,
          () -> new ProblemExceptionWebMvcAdvice(ProblemPostProcessor.identity(), List.of()));
      context.registerBean(InheritedHandler.class, InheritedHandler::new);
      context.registerBean(OrderedInheritedHandler.class, OrderedInheritedHandler::new);
      context.refresh();

      assertThat(orderOf(context, ExceptionWebMvcAdvice.class))
          .isEqualTo(ExceptionWebMvcAdvice.PRECEDENCE);
      assertThat(orderOf(context, ProblemExceptionWebMvcAdvice.class))
          .isEqualTo(ProblemExceptionWebMvcAdvice.PRECEDENCE);
      assertThat(orderOf(context, InheritedHandler.class))
          .isEqualTo(ProblemResponseEntityExceptionHandler.PRECEDENCE);
      assertThat(orderOf(context, OrderedInheritedHandler.class)).isEqualTo(1);
    }
  }

  private static int orderOf(AnnotationConfigApplicationContext context, Class<?> type) {
    return ControllerAdviceBean.findAnnotatedBeans(context).stream()
        .filter(bean -> bean.getBeanType() == type)
        .findFirst()
        .orElseThrow()
        .getOrder();
  }

  @RestControllerAdvice
  static class InheritedHandler extends ProblemResponseEntityExceptionHandler {

    @SuppressWarnings("removal")
    InheritedHandler() {
      super(STORE, ProblemPostProcessor.identity(), List.of());
    }
  }

  @Order(1)
  @RestControllerAdvice
  static class OrderedInheritedHandler extends ProblemResponseEntityExceptionHandler {

    @SuppressWarnings("removal")
    OrderedInheritedHandler() {
      super(STORE, ProblemPostProcessor.identity(), List.of());
    }
  }
}
