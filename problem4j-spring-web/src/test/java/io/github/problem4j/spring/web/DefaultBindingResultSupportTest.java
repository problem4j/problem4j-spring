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
