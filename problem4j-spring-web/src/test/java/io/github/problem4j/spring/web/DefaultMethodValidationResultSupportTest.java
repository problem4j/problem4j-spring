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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.problem4j.spring.web.parameter.DefaultMethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.Violation;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.RequestParam;

class DefaultMethodValidationResultSupportTest {

  private final MethodValidationResultSupport support = new DefaultMethodValidationResultSupport();

  @Test
  void givenMethodValidationException_shouldResolveViolations() throws NoSuchMethodException {
    Method method =
        SampleValidatedMethods.class.getDeclaredMethod("sample", String.class, String.class);
    MethodParameter firstParam = new MethodParameter(method, 0);
    MethodParameter secondParam = new MethodParameter(method, 1);

    ParameterValidationResult firstResult = mock(ParameterValidationResult.class);
    when(firstResult.getMethodParameter()).thenReturn(firstParam);
    when(firstResult.getResolvableErrors())
        .thenReturn(List.of(new ObjectError("first", "must not be null")));

    ParameterValidationResult secondResult = mock(ParameterValidationResult.class);
    when(secondResult.getMethodParameter()).thenReturn(secondParam);
    when(secondResult.getResolvableErrors())
        .thenReturn(List.of(new ObjectError("second", "size must be between 3 and 10")));

    MethodValidationException ex = mock(MethodValidationException.class);
    when(ex.getValueResults()).thenReturn(List.of(firstResult, secondResult));

    List<Violation> violations = support.fetchViolations(ex);

    assertThat(violations).hasSize(2);
    assertThat(violations.get(0)).isEqualTo(new Violation("p1", "must not be null"));
    assertThat(violations.get(1))
        .isEqualTo(new Violation("second", "size must be between 3 and 10"));
  }

  static class SampleValidatedMethods {
    void sample(@RequestParam("p1") String first, String second) {}
  }
}
