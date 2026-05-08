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

package io.github.problem4j.spring.web.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.parameter.Violation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class ConstraintViolationProblemResolverTest {

  private ConstraintViolationProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new ConstraintViolationProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsConstraintViolationException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(ConstraintViolationException.class);
  }

  @Test
  void givenExceptionWithNoViolations_whenResolve_thenReturnsBadRequestWithEmptyErrors() {
    ConstraintViolationException ex = new ConstraintViolationException(Set.of());

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Validation failed");
    assertThat(problem.getExtensionMembers()).containsEntry("errors", List.of());
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenExceptionWithViolation_whenResolve_thenIncludesViolationInErrors() {
    Path.Node node = mock(Path.Node.class);
    when(node.getName()).thenReturn("email");
    Path path = mock(Path.class);
    when(path.iterator()).thenReturn(List.of(node).iterator());
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("must not be blank");

    ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Validation failed");
    assertThat((List<Violation>) problem.getExtensionValue("errors"))
        .containsExactly(new Violation("email", "must not be blank"));
  }
}
