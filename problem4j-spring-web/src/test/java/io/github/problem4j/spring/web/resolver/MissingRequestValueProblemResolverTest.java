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
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing request param");
    assertThat(problem.getExtensions()).containsEntry("param", "page");
    assertThat(problem.getExtensions()).containsEntry("kind", "integer");
  }

  @Test
  void givenHeaderMissing_whenResolve_thenReturnsMissingHeaderProblem()
      throws NoSuchMethodException {
    MissingRequestValueException ex =
        new MissingRequestValueException("Authorization", String.class, "header", parameter());

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing header");
    assertThat(problem.getExtensions()).containsEntry("header", "Authorization");
  }

  @Test
  void givenPathVariableMissing_whenResolve_thenReturnsMissingPathVariableProblem()
      throws NoSuchMethodException {
    MissingRequestValueException ex =
        new MissingRequestValueException("id", String.class, "path parameter", parameter());

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing path variable");
    assertThat(problem.getExtensions()).containsEntry("name", "id");
  }

  @Test
  void givenCookieMissing_whenResolve_thenReturnsMissingCookieProblem()
      throws NoSuchMethodException {
    MissingRequestValueException ex =
        new MissingRequestValueException("session", String.class, "cookie", parameter());

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing cookie");
    assertThat(problem.getExtensions()).containsEntry("cookie", "session");
  }

  private static MethodParameter parameter() throws NoSuchMethodException {
    Method method = DummyHandler.class.getMethod("handle", String.class);
    return new MethodParameter(method, 0);
  }

  static class DummyHandler {
    public void handle(String value) {}
  }
}
