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

import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.DefaultBindingResultSupport;
import io.github.problem4j.spring.web.parameter.Violation;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

class DefaultBindingResultSupportTest {

  private final BindingResultSupport support = new DefaultBindingResultSupport();

  @Test
  void givenBindingResultForValidationError_shouldResolveViolation() {
    TestObject target = new TestObject();
    BindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");
    bindingResult.addError(new FieldError("testObject", "name", "must not be blank"));
    bindingResult.addError(new ObjectError("testObject", "object invalid"));

    List<Violation> violations = support.fetchViolations(bindingResult);

    assertThat(violations)
        .containsExactly(
            new Violation("name", "must not be blank"), new Violation(null, "object invalid"));
  }

  @Test
  void givenBindingResultForBindingError_shouldResolveViolation() {
    TestObject target = new TestObject();
    BindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");

    bindingResult.addError(
        new FieldError("testObject", "age", target, true, null, null, "should be ignored message"));

    List<Violation> violations = support.fetchViolations(bindingResult);

    assertThat(violations).containsExactly(new Violation("age", "is not valid"));
  }

  @Test
  void givenBindingResultWithoutErrors_shouldNotReturnViolations() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new TestObject(), "testObject");

    List<Violation> violations = support.fetchViolations(bindingResult);

    assertThat(violations).isEmpty();
  }

  static class TestObject {
    private @Nullable String name;
    private @Nullable Integer age;
  }
}
