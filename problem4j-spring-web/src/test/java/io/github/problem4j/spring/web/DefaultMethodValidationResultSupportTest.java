/*
 * Copyright (c) 2025-2026 The Problem4J Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

    assertThat(violations.get(1).getField()).isNull();
    assertThat(violations.get(1).getError()).isEqualTo("size must be between 3 and 10");
  }

  static class SampleValidatedMethods {
    void sample(@RequestParam("p1") String first, String second) {}
  }
}
