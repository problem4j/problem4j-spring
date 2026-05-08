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
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebInputException;

class ServerWebInputProblemResolverTest {

  private ServerWebInputProblemResolver serverWebInputMapping;

  @BeforeEach
  void beforeEach() {
    serverWebInputMapping = new ServerWebInputProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsServerWebInputException() {
    assertThat(serverWebInputMapping.getExceptionClass()).isEqualTo(ServerWebInputException.class);
  }

  @Test
  void givenExceptionWithCauseAndWithoutPropertyName_shouldDelegateAndIncludeMethodParameter()
      throws NoSuchMethodException {
    Method method = DummyController.class.getMethod("paramMethod", Boolean.class);
    MethodParameter parameter = new MethodParameter(method, 0);
    parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());

    TypeMismatchException cause = new TypeMismatchException("42", Boolean.class);

    ServerWebInputException ex = new ServerWebInputException("irrelevant reason", parameter, cause);

    Problem problem =
        serverWebInputMapping.resolveProblem(
            ProblemContext.create().put("traceId", "traceId"),
            ex,
            new HttpHeaders(),
            HttpStatusCode.valueOf(400));

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Type mismatch")
                .extension("property", "value")
                .extension("kind", "boolean")
                .build());
  }

  @Test
  void givenExceptionWithCauseAndWithoutParameter_shouldDelegateToMethodParameter() {
    TypeMismatchException cause = new TypeMismatchException("42", Boolean.class);
    cause.initPropertyName("flag");

    ServerWebInputException ex = new ServerWebInputException("irrelevant reason", null, cause);

    Problem problem =
        serverWebInputMapping.resolveProblem(
            ProblemContext.create().put("traceId", "traceId"),
            ex,
            new HttpHeaders(),
            HttpStatusCode.valueOf(400));

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Type mismatch")
                .extension("property", "flag")
                .extension("kind", "boolean")
                .build());
  }

  @Test
  void givenExceptionWithoutCause_shouldReturnSimpleProblem() {
    ServerWebInputException ex = new ServerWebInputException("irrelevant reason");

    Problem problem =
        serverWebInputMapping.resolveProblem(
            ProblemContext.create().put("traceId", "traceId"),
            ex,
            new HttpHeaders(),
            ex.getStatusCode());

    assertThat(problem).isEqualTo(Problem.of(ex.getStatusCode().value()));
  }

  static class DummyController {
    public void paramMethod(Boolean value) {}
  }
}
