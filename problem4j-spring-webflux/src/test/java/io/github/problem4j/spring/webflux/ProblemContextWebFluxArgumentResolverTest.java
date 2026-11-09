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

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.ProblemContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.BindingContext;

class ProblemContextWebFluxArgumentResolverTest {

  private final ProblemContextWebFluxArgumentResolver resolver =
      new ProblemContextWebFluxArgumentResolver();

  private final MockServerWebExchange exchange =
      MockServerWebExchange.from(MockServerHttpRequest.get("/"));

  @Test
  void givenProblemContextParameter_whenCheckingSupport_thenParameterIsSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(0))).isTrue();
  }

  @Test
  void givenOtherParameter_whenCheckingSupport_thenParameterIsNotSupported() throws Exception {
    assertThat(resolver.supportsParameter(parameter(1))).isFalse();
  }

  @Test
  void givenContextAttribute_whenResolving_thenContextIsReturned() throws Exception {
    ProblemContext context = ProblemContext.create().put("traceId", "trace");
    exchange.getAttributes().put(PROBLEM_CONTEXT_ATTRIBUTE, context);

    Object resolved = resolver.resolveArgumentValue(parameter(0), new BindingContext(), exchange);

    assertThat(resolved).isSameAs(context);
  }

  @Test
  void givenNoContextAttribute_whenResolving_thenNullIsReturned() throws Exception {
    Object resolved = resolver.resolveArgumentValue(parameter(0), new BindingContext(), exchange);

    assertThat(resolved).isNull();
  }

  @Test
  void givenNonContextAttribute_whenResolving_thenNullIsReturned() throws Exception {
    exchange.getAttributes().put(PROBLEM_CONTEXT_ATTRIBUTE, "text");

    Object resolved = resolver.resolveArgumentValue(parameter(0), new BindingContext(), exchange);

    assertThat(resolved).isNull();
  }

  @Test
  void givenOptionalProblemContextParameter_whenCheckingSupport_thenParameterIsSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(2))).isTrue();
  }

  @Test
  void givenOptionalOfOtherParameter_whenCheckingSupport_thenParameterIsNotSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(3))).isFalse();
  }

  @Test
  void givenContextAttribute_whenResolvingOptional_thenContextIsReturnedInOptional()
      throws Exception {
    ProblemContext context = ProblemContext.create().put("traceId", "trace");
    exchange.getAttributes().put(PROBLEM_CONTEXT_ATTRIBUTE, context);

    Object resolved = resolver.resolveArgumentValue(parameter(2), new BindingContext(), exchange);

    assertThat(resolved).isEqualTo(Optional.of(context));
  }

  @Test
  void givenNoContextAttribute_whenResolvingOptional_thenEmptyOptionalIsReturned()
      throws Exception {
    Object resolved = resolver.resolveArgumentValue(parameter(2), new BindingContext(), exchange);

    assertThat(resolved).isEqualTo(Optional.empty());
  }

  private static MethodParameter parameter(int index) throws NoSuchMethodException {
    return new MethodParameter(
        Handler.class.getDeclaredMethod(
            "handle", ProblemContext.class, String.class, Optional.class, Optional.class),
        index);
  }

  static class Handler {

    void handle(
        ProblemContext context,
        String text,
        Optional<ProblemContext> optionalContext,
        Optional<String> optionalText) {}
  }
}
