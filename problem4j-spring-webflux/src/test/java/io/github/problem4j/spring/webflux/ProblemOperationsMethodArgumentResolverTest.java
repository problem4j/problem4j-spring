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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.problem4j.spring.web.support.ProblemOperations;
import io.github.problem4j.spring.web.support.ProblemTemplate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.BindingContext;
import reactor.core.publisher.Mono;

class ProblemOperationsMethodArgumentResolverTest {

  private final ProblemTemplate problemTemplate = ProblemTemplate.builder().build();
  private final ProblemOperationsMethodArgumentResolver resolver =
      new ProblemOperationsMethodArgumentResolver(problemTemplate);

  @Test
  void givenProblemOperationsParameter_whenCheckingSupport_thenParameterIsSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(0))).isTrue();
  }

  @Test
  void givenProblemTemplateParameterAndTemplateInUse_whenCheckingSupport_thenParameterIsSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(1))).isTrue();
  }

  @Test
  void givenProblemTemplateParameterAndOtherOperationsInUse_whenCheckingSupport_thenNotSupported()
      throws Exception {
    ProblemOperationsMethodArgumentResolver other =
        new ProblemOperationsMethodArgumentResolver(mock(ProblemOperations.class));

    assertThat(other.supportsParameter(parameter(0))).isTrue();
    assertThat(other.supportsParameter(parameter(1))).isFalse();
  }

  @Test
  void givenOtherParameter_whenCheckingSupport_thenParameterIsNotSupported() throws Exception {
    assertThat(resolver.supportsParameter(parameter(2))).isFalse();
    assertThat(resolver.supportsParameter(parameter(3))).isFalse();
  }

  @Test
  void givenProblemOperationsParameter_whenResolving_thenOperationsInUseAreReturned()
      throws Exception {
    Object resolved =
        resolver.resolveArgumentValue(
            parameter(0),
            new BindingContext(),
            MockServerWebExchange.from(MockServerHttpRequest.get("/")));

    assertThat(resolved).isSameAs(problemTemplate);
  }

  @Test
  void givenOptionalOrMonoProblemOperationsParameter_whenCheckingSupport_thenSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(4))).isTrue();
    assertThat(resolver.supportsParameter(parameter(6))).isTrue();
  }

  @Test
  void givenOptionalOrMonoOfOtherParameter_whenCheckingSupport_thenParameterIsNotSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(5))).isFalse();
    assertThat(resolver.supportsParameter(parameter(7))).isFalse();
  }

  @Test
  void givenOptionalProblemOperationsParameter_whenResolving_thenOperationsAreReturnedInOptional()
      throws Exception {
    Object resolved =
        resolver.resolveArgumentValue(
            parameter(4),
            new BindingContext(),
            MockServerWebExchange.from(MockServerHttpRequest.get("/")));

    assertThat(resolved).isEqualTo(Optional.of(problemTemplate));
  }

  @Test
  void givenMonoProblemOperationsParameter_whenResolving_thenOperationsAreReturnedInMono()
      throws Exception {
    Object resolved =
        resolver.resolveArgumentValue(
            parameter(6),
            new BindingContext(),
            MockServerWebExchange.from(MockServerHttpRequest.get("/")));

    assertThat(resolved).isInstanceOf(Mono.class);
    assertThat(((Mono<?>) resolved).block()).isSameAs(problemTemplate);
  }

  private static MethodParameter parameter(int index) throws NoSuchMethodException {
    return new MethodParameter(
        Handler.class.getDeclaredMethod(
            "handle",
            ProblemOperations.class,
            ProblemTemplate.class,
            String.class,
            Object.class,
            Optional.class,
            Optional.class,
            Mono.class,
            Mono.class),
        index);
  }

  static class Handler {

    void handle(
        ProblemOperations operations,
        ProblemTemplate template,
        String text,
        Object object,
        Optional<ProblemOperations> optionalOperations,
        Optional<String> optionalText,
        Mono<ProblemOperations> monoOperations,
        Mono<String> monoText) {}
  }
}
