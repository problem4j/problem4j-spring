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

package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.parameter.ViolationSupport.ERRORS_EXTENSION;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.parameter.Violation;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.support.WebExchangeBindException;

class WebExchangeBindProblemResolverTest {

  private final List<Violation> constructorViolations =
      List.of(new Violation("field", "constructor"));
  private final List<Violation> customViolations = List.of(new Violation("field", "custom"));

  @Test
  void givenDefaultConstructor_whenSetBindingResultSupport_thenUsesNewSupport() throws Exception {
    WebExchangeBindProblemResolver resolver = new WebExchangeBindProblemResolver();
    resolver.setBindingResultSupport(result -> customViolations);

    assertThat(resolve(resolver).getExtensions()).containsEntry(ERRORS_EXTENSION, customViolations);
  }

  @Test
  @SuppressWarnings("removal")
  void givenSupportPassedToConstructor_whenSetBindingResultSupport_thenKeepsConstructorSupport()
      throws Exception {
    WebExchangeBindProblemResolver resolver =
        new WebExchangeBindProblemResolver(
            ProblemFormat.identity(), result -> constructorViolations);
    resolver.setBindingResultSupport(result -> customViolations);

    assertThat(resolve(resolver).getExtensions())
        .containsEntry(ERRORS_EXTENSION, constructorViolations);
  }

  @Test
  @SuppressWarnings("removal")
  void givenOnlyProblemFormatPassedToConstructor_whenSetBindingResultSupport_thenUsesNewSupport()
      throws Exception {
    WebExchangeBindProblemResolver resolver =
        new WebExchangeBindProblemResolver(ProblemFormat.identity());
    resolver.setBindingResultSupport(result -> customViolations);

    assertThat(resolve(resolver).getExtensions()).containsEntry(ERRORS_EXTENSION, customViolations);
  }

  private Problem resolve(WebExchangeBindProblemResolver resolver) throws Exception {
    return resolver.resolve(
        ProblemContext.create(),
        new WebExchangeBindException(
            new MethodParameter(
                WebExchangeBindProblemResolverTest.class.getDeclaredMethod("handle", Object.class),
                0),
            new BeanPropertyBindingResult(new Object(), "target")),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST);
  }

  @SuppressWarnings("unused")
  private void handle(Object target) {}
}
