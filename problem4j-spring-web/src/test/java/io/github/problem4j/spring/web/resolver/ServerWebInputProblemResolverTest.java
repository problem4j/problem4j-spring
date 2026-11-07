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
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.parameter.DefaultMethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebInputException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

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
  void givenProblemFormatSetAfterConstruction_whenResolvingTypeMismatch_thenUsesNewFormat() {
    serverWebInputMapping.setProblemFormat(detail -> detail == null ? null : detail.toUpperCase());
    TypeMismatchException cause = new TypeMismatchException("42", Boolean.class);
    cause.initPropertyName("flag");
    ServerWebInputException ex = new ServerWebInputException("irrelevant reason", null, cause);

    Problem problem =
        serverWebInputMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem.getDetail()).isEqualTo("TYPE MISMATCH");
  }

  @Test
  @SuppressWarnings("removal")
  void givenDeprecatedConstructors_whenResolvingTypeMismatch_thenProduceSameProblem() {
    TypeMismatchException cause = new TypeMismatchException("42", Boolean.class);
    cause.initPropertyName("flag");
    ServerWebInputException ex = new ServerWebInputException("irrelevant reason", null, cause);
    Problem expected =
        Problem.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("Type mismatch")
            .extension("property", "flag")
            .extension("kind", "boolean")
            .build();
    MethodParameterSupport support = new DefaultMethodParameterSupport();

    List<ServerWebInputProblemResolver> resolvers =
        List.of(
            new ServerWebInputProblemResolver(ProblemFormat.identity()),
            new ServerWebInputProblemResolver(ProblemFormat.identity(), support),
            new ServerWebInputProblemResolver(
                ProblemFormat.identity(),
                new TypeMismatchProblemResolver(),
                support,
                new SimpleTypeNameMapper()));

    for (ServerWebInputProblemResolver resolver : resolvers) {
      Problem problem =
          resolver.resolve(
              ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));
      assertThat(problem).isEqualTo(expected);
    }
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
  void givenDecodingExceptionWithMismatchedInputCause_whenResolve_thenResolvesMismatchedInput()
      throws IOException {
    serverWebInputMapping.setTypeNameMapper(type -> Optional.of("custom-int"));
    MismatchedInputException cause;
    try (JsonParser parser = JsonMapper.builder().build().createParser("{}")) {
      parser.nextToken();
      cause = MismatchedInputException.from(parser, Integer.class, "msg");
      cause.prependPath(ServerWebInputProblemResolverTest.class, "age");
    }
    ServerWebInputException ex =
        new ServerWebInputException(
            "irrelevant reason", null, new DecodingException("decoding failed", cause));

    Problem problem =
        serverWebInputMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem.getExtensions())
        .containsEntry("property", "age")
        .containsEntry("kind", "custom-int");
  }

  @Test
  void
      givenDecodingExceptionWithoutMismatchedInputCause_whenResolve_thenReturnsBadRequestProblem() {
    ServerWebInputException ex =
        new ServerWebInputException(
            "irrelevant reason", null, new DecodingException("decoding failed"));

    Problem problem =
        serverWebInputMapping.resolve(
            ProblemContext.create(), ex, new HttpHeaders(), HttpStatusCode.valueOf(400));

    assertThat(problem).isEqualTo(Problem.of(HttpStatus.BAD_REQUEST.value()));
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
