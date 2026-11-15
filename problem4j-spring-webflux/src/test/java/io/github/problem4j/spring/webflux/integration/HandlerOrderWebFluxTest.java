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

package io.github.problem4j.spring.webflux.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webflux.ProblemHandlerResultHandler;
import io.github.problem4j.spring.webflux.ProblemWebExceptionHandler;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler;
import org.springframework.web.server.WebExceptionHandler;

/**
 * Guards orders of Problem4J handlers against orders of Spring and Spring Boot handlers they must
 * precede, as neither of them exposes its orders as constants.
 */
@SpringBootTest(
    classes = {WebFluxTestApp.class},
    properties = {"problem4j.exception-handling.mode=fallback"})
class HandlerOrderWebFluxTest {

  @Autowired private ApplicationContext context;

  @Test
  void givenResultHandlers_whenOrdered_thenProblem4JOnePrecedesSpringOnes() {
    List<HandlerResultHandler> handlers =
        context.getBeanProvider(HandlerResultHandler.class).orderedStream().toList();

    assertThat(handlers).first().isInstanceOf(ProblemHandlerResultHandler.class);
    assertThat(handlers)
        .filteredOn(handler -> !(handler instanceof ProblemHandlerResultHandler))
        .isNotEmpty()
        .allSatisfy(handler ->
            assertThat(((Ordered) handler).getOrder())
                .isGreaterThan(ProblemHandlerResultHandler.PRECEDENCE));
  }

  @Test
  void givenWebExceptionHandlers_whenOrdered_thenProblem4JOnePrecedesSpringAndBootOnes() {
    List<WebExceptionHandler> handlers =
        context.getBeanProvider(WebExceptionHandler.class).orderedStream().toList();

    int problem4j = indexOf(handlers, ProblemWebExceptionHandler.class);
    assertThat(problem4j).isNotNegative();
    assertThat(problem4j).isLessThan(indexOf(handlers, ErrorWebExceptionHandler.class));
    assertThat(problem4j)
        .isLessThan(indexOf(handlers, WebFluxResponseStatusExceptionHandler.class));
  }

  private static int indexOf(List<WebExceptionHandler> handlers, Class<?> type) {
    for (int i = 0; i < handlers.size(); i++) {
      if (type.isInstance(handlers.get(i))) {
        return i;
      }
    }
    return -1;
  }
}
