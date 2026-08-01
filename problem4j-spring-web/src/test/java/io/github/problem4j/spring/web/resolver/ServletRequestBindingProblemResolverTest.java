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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;

class ServletRequestBindingProblemResolverTest {

  private ServletRequestBindingProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new ServletRequestBindingProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsServletRequestBindingException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(ServletRequestBindingException.class);
  }

  @Test
  void givenMissingServletRequestParameterException_whenResolve_thenReturnsMissingParamProblem() {
    MissingServletRequestParameterException ex =
        new MissingServletRequestParameterException("page", "Integer");

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing request param");
    assertThat(problem.getExtensions()).containsEntry("param", "page");
    assertThat(problem.getExtensions()).containsEntry("kind", "integer");
  }

  @Test
  void givenMissingPathVariableException_whenResolve_thenReturnsMissingPathVariableProblem()
      throws NoSuchMethodException {
    MethodParameter parameter = methodParameter("pathMethod", String.class);
    MissingPathVariableException ex = new MissingPathVariableException("id", parameter);

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing path variable");
    assertThat(problem.getExtensions()).containsEntry("name", "id");
  }

  @Test
  void givenMissingRequestHeaderException_whenResolve_thenReturnsMissingHeaderProblem()
      throws NoSuchMethodException {
    MethodParameter parameter = methodParameter("headerMethod", String.class);
    MissingRequestHeaderException ex =
        new MissingRequestHeaderException("Authorization", parameter);

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing header");
    assertThat(problem.getExtensions()).containsEntry("header", "Authorization");
  }

  @Test
  void givenMissingRequestCookieException_whenResolve_thenReturnsMissingCookieProblem()
      throws NoSuchMethodException {
    MethodParameter parameter = methodParameter("cookieMethod", String.class);
    MissingRequestCookieException ex = new MissingRequestCookieException("session", parameter);

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing cookie");
    assertThat(problem.getExtensions()).containsEntry("cookie", "session");
  }

  @Test
  void
      givenGenericExceptionWithSessionAttributeMessage_whenResolve_thenReturnsMissingSessionAttributeProblem() {
    ServletRequestBindingException ex =
        new ServletRequestBindingException("Missing session attribute 'userId'");

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing session attribute");
    assertThat(problem.getExtensions()).containsEntry("attribute", "userId");
  }

  @Test
  void
      givenGenericExceptionWithRequestAttributeMessage_whenResolve_thenReturnsMissingRequestAttributeProblem() {
    ServletRequestBindingException ex =
        new ServletRequestBindingException("Missing request attribute 'tenantId'");

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing request attribute");
    assertThat(problem.getExtensions()).containsEntry("attribute", "tenantId");
  }

  private static MethodParameter methodParameter(String methodName, Class<?>... paramTypes)
      throws NoSuchMethodException {
    Method method = DummyHandler.class.getMethod(methodName, paramTypes);
    return new MethodParameter(method, 0);
  }

  static class DummyHandler {
    public void pathMethod(String id) {}

    public void headerMethod(String value) {}

    public void cookieMethod(String value) {}
  }
}
