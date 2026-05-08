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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.MissingRequestValueException;

class MissingRequestValueProblemResolverTest {

  private MissingRequestValueProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new MissingRequestValueProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsMissingRequestValueException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(MissingRequestValueException.class);
  }

  @Test
  void givenQueryParameterMissing_whenResolve_thenReturnsMissingParamProblem()
      throws NoSuchMethodException {
    MissingRequestValueException ex =
        new MissingRequestValueException("page", Integer.class, "query parameter", parameter());

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing request param");
    assertThat(problem.getExtensionMembers()).containsEntry("param", "page");
    assertThat(problem.getExtensionMembers()).containsEntry("kind", "integer");
  }

  @Test
  void givenHeaderMissing_whenResolve_thenReturnsMissingHeaderProblem()
      throws NoSuchMethodException {
    MissingRequestValueException ex =
        new MissingRequestValueException("Authorization", String.class, "header", parameter());

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing header");
    assertThat(problem.getExtensionMembers()).containsEntry("header", "Authorization");
  }

  @Test
  void givenPathVariableMissing_whenResolve_thenReturnsMissingPathVariableProblem()
      throws NoSuchMethodException {
    MissingRequestValueException ex =
        new MissingRequestValueException("id", String.class, "path parameter", parameter());

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing path variable");
    assertThat(problem.getExtensionMembers()).containsEntry("name", "id");
  }

  @Test
  void givenCookieMissing_whenResolve_thenReturnsMissingCookieProblem()
      throws NoSuchMethodException {
    MissingRequestValueException ex =
        new MissingRequestValueException("session", String.class, "cookie", parameter());

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing cookie");
    assertThat(problem.getExtensionMembers()).containsEntry("cookie", "session");
  }

  private static MethodParameter parameter() throws NoSuchMethodException {
    Method method = DummyHandler.class.getMethod("handle", String.class);
    return new MethodParameter(method, 0);
  }

  static class DummyHandler {
    public void handle(String value) {}
  }
}
