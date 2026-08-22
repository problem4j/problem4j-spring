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
import java.util.Optional;
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
  void
      givenTypeNameMapperSetAfterConstruction_whenResolvingTypeMismatch_thenCascadesToNestedResolver() {
    serverWebInputMapping.setTypeNameMapper(type -> Optional.of("custom"));
    TypeMismatchException cause = new TypeMismatchException("42", Boolean.class);
    cause.initPropertyName("flag");
    ServerWebInputException ex = new ServerWebInputException("irrelevant reason", null, cause);

    Problem problem =
        serverWebInputMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem.getExtensions()).containsEntry("kind", "custom");
  }

  @Test
  void
      givenMethodParameterSupportSetAfterConstruction_whenResolvingWithoutPropertyName_thenUsesNewSupport()
          throws NoSuchMethodException {
    Method method = DummyController.class.getMethod("paramMethod", Boolean.class);
    MethodParameter parameter = new MethodParameter(method, 0);
    parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
    serverWebInputMapping.setMethodParameterSupport(mp -> Optional.of("custom-name"));

    TypeMismatchException cause = new TypeMismatchException("42", Boolean.class);
    ServerWebInputException ex = new ServerWebInputException("irrelevant reason", parameter, cause);

    Problem problem =
        serverWebInputMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem.getExtensions()).containsEntry("property", "custom-name");
  }

  @Test
  void
      givenTypeMismatchProblemResolverSetAfterConstruction_whenResolvingTypeMismatch_thenUsesNewResolver() {
    TypeMismatchProblemResolver custom = new TypeMismatchProblemResolver();
    custom.setTypeNameMapper(type -> Optional.of("custom-resolver"));
    serverWebInputMapping.setTypeMismatchProblemResolver(custom);

    TypeMismatchException cause = new TypeMismatchException("42", Boolean.class);
    cause.initPropertyName("flag");
    ServerWebInputException ex = new ServerWebInputException("irrelevant reason", null, cause);

    Problem problem =
        serverWebInputMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem.getExtensions()).containsEntry("kind", "custom-resolver");
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
        serverWebInputMapping.resolve(
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
        serverWebInputMapping.resolve(
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
        serverWebInputMapping.resolve(
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
