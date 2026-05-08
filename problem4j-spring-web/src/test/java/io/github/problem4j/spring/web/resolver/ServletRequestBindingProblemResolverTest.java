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
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing request param");
    assertThat(problem.getExtensionMembers()).containsEntry("param", "page");
    assertThat(problem.getExtensionMembers()).containsEntry("kind", "integer");
  }

  @Test
  void givenMissingPathVariableException_whenResolve_thenReturnsMissingPathVariableProblem()
      throws NoSuchMethodException {
    MethodParameter parameter = methodParameter("pathMethod", String.class);
    MissingPathVariableException ex = new MissingPathVariableException("id", parameter);

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing path variable");
    assertThat(problem.getExtensionMembers()).containsEntry("name", "id");
  }

  @Test
  void givenMissingRequestHeaderException_whenResolve_thenReturnsMissingHeaderProblem()
      throws NoSuchMethodException {
    MethodParameter parameter = methodParameter("headerMethod", String.class);
    MissingRequestHeaderException ex =
        new MissingRequestHeaderException("Authorization", parameter);

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing header");
    assertThat(problem.getExtensionMembers()).containsEntry("header", "Authorization");
  }

  @Test
  void givenMissingRequestCookieException_whenResolve_thenReturnsMissingCookieProblem()
      throws NoSuchMethodException {
    MethodParameter parameter = methodParameter("cookieMethod", String.class);
    MissingRequestCookieException ex = new MissingRequestCookieException("session", parameter);

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing cookie");
    assertThat(problem.getExtensionMembers()).containsEntry("cookie", "session");
  }

  @Test
  void
      givenGenericExceptionWithSessionAttributeMessage_whenResolve_thenReturnsMissingSessionAttributeProblem() {
    ServletRequestBindingException ex =
        new ServletRequestBindingException("Missing session attribute 'userId'");

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing session attribute");
    assertThat(problem.getExtensionMembers()).containsEntry("attribute", "userId");
  }

  @Test
  void
      givenGenericExceptionWithRequestAttributeMessage_whenResolve_thenReturnsMissingRequestAttributeProblem() {
    ServletRequestBindingException ex =
        new ServletRequestBindingException("Missing request attribute 'tenantId'");

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Missing request attribute");
    assertThat(problem.getExtensionMembers()).containsEntry("attribute", "tenantId");
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
