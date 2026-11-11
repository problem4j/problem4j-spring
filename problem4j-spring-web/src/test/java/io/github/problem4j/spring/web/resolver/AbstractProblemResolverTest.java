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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

class AbstractProblemResolverTest {

  public static Stream<Arguments> exceptions() {
    return Stream.of(Arguments.of(IllegalArgumentException.class, IllegalStateException.class));
  }

  @ParameterizedTest
  @MethodSource("exceptions")
  void givenAnyMapping_shouldReturnExceptionClass(Class<? extends Exception> clazz) {
    AbstractProblemResolver resolver =
        new AbstractProblemResolver(clazz) {
          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            return Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
          }
        };

    Class<? extends Exception> exceptionClass = resolver.getExceptionClass();

    assertThat(exceptionClass).isEqualTo(clazz);
  }

  @Test
  void givenSetProblemFormat_whenResolve_thenUsesNewFormat() {
    AbstractProblemResolver resolver =
        new AbstractProblemResolver(RuntimeException.class) {
          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            return Problem.builder().status(500).detail(formatDetail("detail")).build();
          }
        };

    resolver.setProblemFormat(detail -> detail == null ? null : detail.toUpperCase());

    Problem problem =
        resolver.resolve(
            ProblemContext.create(),
            new RuntimeException("boom"),
            new HttpHeaders(),
            HttpStatusCode.valueOf(500));

    assertThat(problem.getDetail()).isEqualTo("DETAIL");
  }

  @Test
  void givenNoResolveProblemOverride_whenResolve_thenReturnsInternalServerError() {
    AbstractProblemResolver resolver =
        new AbstractProblemResolver(RuntimeException.class) {
          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            return Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
          }
        };

    Problem problem =
        resolver.resolve(
            ProblemContext.create(),
            new RuntimeException("boom"),
            new HttpHeaders(),
            HttpStatusCode.valueOf(500));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }

  public static Stream<Arguments> defaultConstructors() {
    return Stream.of(
        defaultConstructor("BindProblemResolver", BindProblemResolver::new),
        defaultConstructor(
            "ConstraintViolationProblemResolver", ConstraintViolationProblemResolver::new),
        defaultConstructor("DecodingProblemResolver", DecodingProblemResolver::new),
        defaultConstructor("ErrorResponseProblemResolver", ErrorResponseProblemResolver::new),
        defaultConstructor(
            "HandlerMethodValidationProblemResolver", HandlerMethodValidationProblemResolver::new),
        defaultConstructor(
            "HttpMediaTypeNotAcceptableProblemResolver",
            HttpMediaTypeNotAcceptableProblemResolver::new),
        defaultConstructor(
            "HttpMediaTypeNotSupportedProblemResolver",
            HttpMediaTypeNotSupportedProblemResolver::new),
        defaultConstructor(
            "HttpMessageNotReadableProblemResolver", HttpMessageNotReadableProblemResolver::new),
        defaultConstructor(
            "HttpRequestMethodNotSupportedProblemResolver",
            HttpRequestMethodNotSupportedProblemResolver::new),
        defaultConstructor(
            "MaxUploadSizeExceededProblemResolver", MaxUploadSizeExceededProblemResolver::new),
        defaultConstructor("MethodValidationProblemResolver", MethodValidationProblemResolver::new),
        defaultConstructor(
            "MissingRequestValueProblemResolver", MissingRequestValueProblemResolver::new),
        defaultConstructor(
            "MissingServletRequestPartProblemResolver",
            MissingServletRequestPartProblemResolver::new),
        defaultConstructor("MultipartProblemResolver", MultipartProblemResolver::new),
        defaultConstructor("ResponseStatusProblemResolver", ResponseStatusProblemResolver::new),
        defaultConstructor("ServerErrorProblemResolver", ServerErrorProblemResolver::new),
        defaultConstructor("ServerWebInputProblemResolver", ServerWebInputProblemResolver::new),
        defaultConstructor(
            "ServletRequestBindingProblemResolver", ServletRequestBindingProblemResolver::new),
        defaultConstructor("TypeMismatchProblemResolver", TypeMismatchProblemResolver::new),
        defaultConstructor("WebExchangeBindProblemResolver", WebExchangeBindProblemResolver::new));
  }

  @SuppressWarnings("removal")
  public static Stream<Arguments> problemFormatConstructors() {
    return Stream.of(
        problemFormatConstructor("BindProblemResolver", BindProblemResolver::new),
        problemFormatConstructor(
            "ConstraintViolationProblemResolver", ConstraintViolationProblemResolver::new),
        problemFormatConstructor("DecodingProblemResolver", DecodingProblemResolver::new),
        problemFormatConstructor("ErrorResponseProblemResolver", ErrorResponseProblemResolver::new),
        problemFormatConstructor(
            "HandlerMethodValidationProblemResolver", HandlerMethodValidationProblemResolver::new),
        problemFormatConstructor(
            "HttpMediaTypeNotAcceptableProblemResolver",
            HttpMediaTypeNotAcceptableProblemResolver::new),
        problemFormatConstructor(
            "HttpMediaTypeNotSupportedProblemResolver",
            HttpMediaTypeNotSupportedProblemResolver::new),
        problemFormatConstructor(
            "HttpMessageNotReadableProblemResolver", HttpMessageNotReadableProblemResolver::new),
        problemFormatConstructor(
            "HttpRequestMethodNotSupportedProblemResolver",
            HttpRequestMethodNotSupportedProblemResolver::new),
        problemFormatConstructor(
            "MaxUploadSizeExceededProblemResolver", MaxUploadSizeExceededProblemResolver::new),
        problemFormatConstructor(
            "MethodValidationProblemResolver", MethodValidationProblemResolver::new),
        problemFormatConstructor(
            "MissingRequestValueProblemResolver", MissingRequestValueProblemResolver::new),
        problemFormatConstructor(
            "MissingServletRequestPartProblemResolver",
            MissingServletRequestPartProblemResolver::new),
        problemFormatConstructor("MultipartProblemResolver", MultipartProblemResolver::new),
        problemFormatConstructor(
            "ResponseStatusProblemResolver", ResponseStatusProblemResolver::new),
        problemFormatConstructor("ServerErrorProblemResolver", ServerErrorProblemResolver::new),
        problemFormatConstructor(
            "ServerWebInputProblemResolver", ServerWebInputProblemResolver::new),
        problemFormatConstructor(
            "ServletRequestBindingProblemResolver", ServletRequestBindingProblemResolver::new),
        problemFormatConstructor("TypeMismatchProblemResolver", TypeMismatchProblemResolver::new),
        problemFormatConstructor(
            "WebExchangeBindProblemResolver", WebExchangeBindProblemResolver::new),
        problemFormatConstructor(
            "AbstractProblemResolver", AbstractProblemResolverTest::customResolver));
  }

  @ParameterizedTest
  @MethodSource("defaultConstructors")
  void givenDefaultConstructor_whenSetProblemFormat_thenUsesNewFormat(
      Supplier<AbstractProblemResolver> constructor) {
    AbstractProblemResolver resolver = constructor.get();
    ProblemFormat newFormat = detail -> detail;

    resolver.setProblemFormat(newFormat);

    assertThat(resolver.getProblemFormat()).isSameAs(newFormat);
  }

  @ParameterizedTest
  @MethodSource("problemFormatConstructors")
  void givenProblemFormatPassedToConstructor_whenSetProblemFormat_thenKeepsConstructorFormat(
      Function<ProblemFormat, AbstractProblemResolver> constructor) {
    ProblemFormat constructorFormat = detail -> detail;
    AbstractProblemResolver resolver = constructor.apply(constructorFormat);

    resolver.setProblemFormat(detail -> detail == null ? null : detail.toUpperCase());

    assertThat(resolver.getProblemFormat()).isSameAs(constructorFormat);
  }

  @Test
  @SuppressWarnings("removal")
  void givenProblemFormatPassedToConstructor_whenResolve_thenIgnoresSetProblemFormat() {
    AbstractProblemResolver resolver = customResolver(ProblemFormat.identity());

    resolver.setProblemFormat(detail -> detail == null ? null : detail.toUpperCase());

    Problem problem =
        resolver.resolve(
            ProblemContext.create(),
            new RuntimeException("boom"),
            new HttpHeaders(),
            HttpStatusCode.valueOf(500));

    assertThat(problem.getDetail()).isEqualTo("detail");
  }

  private static Arguments defaultConstructor(
      String name, Supplier<AbstractProblemResolver> constructor) {
    return Arguments.of(Named.of(name, constructor));
  }

  private static Arguments problemFormatConstructor(
      String name, Function<ProblemFormat, AbstractProblemResolver> constructor) {
    return Arguments.of(Named.of(name, constructor));
  }

  @SuppressWarnings("removal")
  private static AbstractProblemResolver customResolver(ProblemFormat problemFormat) {
    return new AbstractProblemResolver(RuntimeException.class, problemFormat) {
      @Override
      public Problem resolve(
          ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
        return Problem.builder().status(500).detail(formatDetail("detail")).build();
      }
    };
  }
}
