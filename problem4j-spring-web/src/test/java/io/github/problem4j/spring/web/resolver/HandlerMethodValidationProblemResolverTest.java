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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.parameter.Violation;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

class HandlerMethodValidationProblemResolverTest {

  private HandlerMethodValidationProblemResolver handlerMethodValidationProblemResolver;

  @BeforeEach
  void beforeEach() {
    handlerMethodValidationProblemResolver = new HandlerMethodValidationProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsHandlerMethodValidationException() {
    assertThat(handlerMethodValidationProblemResolver.getExceptionClass())
        .isEqualTo(HandlerMethodValidationException.class);
  }

  @Test
  void givenHandlerMethodValidationException_shouldGenerateProblem() {
    MethodValidationResult mockMethodValidationResult = mock(MethodValidationResult.class);
    HandlerMethodValidationException ex =
        new HandlerMethodValidationException(mockMethodValidationResult);

    Problem problem =
        handlerMethodValidationProblemResolver.resolve(
            ProblemContext.create().put("traceId", "traceId"),
            ex,
            new HttpHeaders(),
            ex.getStatusCode());

    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
  }

  @Test
  void givenMethodValidationResultSupportSetAfterConstruction_whenResolve_thenUsesNewSupport() {
    List<Violation> violations = List.of(new Violation("field", "custom"));
    handlerMethodValidationProblemResolver.setMethodValidationResultSupport(result -> violations);
    HandlerMethodValidationException ex =
        new HandlerMethodValidationException(mock(MethodValidationResult.class));

    Problem problem =
        handlerMethodValidationProblemResolver.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), ex.getStatusCode());

    assertThat(problem.getExtensions()).containsEntry(ERRORS_EXTENSION, violations);
  }
}
