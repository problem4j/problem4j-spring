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
import static org.mockito.Mockito.mock;

import io.github.problem4j.spring.web.support.ProblemOperations;
import io.github.problem4j.spring.web.support.ProblemTemplate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

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
        resolver.resolveArgument(
            parameter(0), null, new ServletWebRequest(new MockHttpServletRequest()), null);

    assertThat(resolved).isSameAs(problemTemplate);
  }

  @Test
  void givenOptionalProblemOperationsParameter_whenCheckingSupport_thenParameterIsSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(4))).isTrue();
  }

  @Test
  void givenOptionalOfOtherParameter_whenCheckingSupport_thenParameterIsNotSupported()
      throws Exception {
    assertThat(resolver.supportsParameter(parameter(5))).isFalse();
  }

  @Test
  void givenOptionalProblemOperationsParameter_whenResolving_thenOperationsAreReturnedInOptional()
      throws Exception {
    Object resolved =
        resolver.resolveArgument(
            parameter(4), null, new ServletWebRequest(new MockHttpServletRequest()), null);

    assertThat(resolved).isEqualTo(Optional.of(problemTemplate));
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
            Optional.class),
        index);
  }

  static class Handler {

    void handle(
        ProblemOperations operations,
        ProblemTemplate template,
        String text,
        Object object,
        Optional<ProblemOperations> optionalOperations,
        Optional<String> optionalText) {}
  }
}
