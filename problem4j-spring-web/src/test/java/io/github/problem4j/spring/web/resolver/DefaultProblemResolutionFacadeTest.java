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

import io.github.problem4j.core.DefaultProblemMapper;
import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.core.ProblemMapping;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

class DefaultProblemResolutionFacadeTest {

  private final ProblemMapper problemMapper = new DefaultProblemMapper();

  @Test
  void givenProblemMappingAnnotatedException_whenResolving_thenMappingIsUsed() {
    ProblemResolutionFacade facade = facadeWith(List.of());

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new MappedException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem)
        .isEqualTo(Problem.builder().status(418).title("Mapped").detail("mapped detail").build());
  }

  @Test
  void givenExceptionWithMappingAndResolver_whenResolving_thenMappingTakesPrecedence() {
    ProblemResolutionFacade facade =
        facadeWith(List.of(fixedResolver(MappedException.class, Problem.of(409))));

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new MappedException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem.getStatus()).isEqualTo(418);
  }

  @Test
  void givenResolverForException_whenResolving_thenResolverIsUsed() {
    ProblemResolutionFacade facade =
        facadeWith(List.of(fixedResolver(PlainException.class, Problem.of(409))));

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new PlainException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem).isEqualTo(Problem.of(409));
  }

  @Test
  void givenResolverForSuperclass_whenResolvingSubclass_thenSuperclassResolverIsUsed() {
    ProblemResolutionFacade facade =
        facadeWith(List.of(fixedResolver(PlainException.class, Problem.of(409))));

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new PlainSubException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem).isEqualTo(Problem.of(409));
  }

  @Test
  void givenResolver_whenResolving_thenContextHeadersAndStatusArePassedToResolver() {
    AtomicReference<List<Object>> captured = new AtomicReference<>();
    ProblemResolver resolver =
        new AbstractProblemResolver(PlainException.class) {
          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            captured.set(List.of(context, ex, headers, status));
            headers.add("X-Resolved", "true");
            return Problem.of(status.value());
          }
        };
    ProblemResolutionFacade facade = facadeWith(List.of(resolver));
    ProblemContext context = ProblemContext.create();
    PlainException ex = new PlainException();
    HttpHeaders headers = new HttpHeaders();

    Problem problem = facade.resolve(context, ex, headers, HttpStatus.METHOD_NOT_ALLOWED);

    assertThat(captured.get()).containsExactly(context, ex, headers, HttpStatus.METHOD_NOT_ALLOWED);
    assertThat(headers.getFirst("X-Resolved")).isEqualTo("true");
    assertThat(problem).isEqualTo(Problem.of(405));
  }

  @Test
  void givenExceptionWithResolverAndResponseStatus_whenResolving_thenResolverTakesPrecedence() {
    ProblemResolutionFacade facade =
        facadeWith(List.of(fixedResolver(ReasonStatusException.class, Problem.of(409))));

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new ReasonStatusException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem).isEqualTo(Problem.of(409));
  }

  @Test
  void givenResponseStatusWithReason_whenResolving_thenStatusAndReasonAreUsed() {
    ProblemResolutionFacade facade = facadeWith(List.of());

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new ReasonStatusException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem).isEqualTo(Problem.builder().status(403).detail("not yours").build());
  }

  @Test
  void givenResponseStatusWithoutReason_whenResolving_thenOnlyStatusIsUsed() {
    ProblemResolutionFacade facade = facadeWith(List.of());

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new NoReasonStatusException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem).isEqualTo(Problem.of(410));
  }

  @Test
  void givenInheritedResponseStatus_whenResolvingSubclass_thenAnnotationOfSuperclassIsUsed() {
    ProblemResolutionFacade facade = facadeWith(List.of());

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new NoReasonStatusSubException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem).isEqualTo(Problem.of(410));
  }

  @Test
  void givenUnknownException_whenResolvingWithInternalServerError_thenInternalServerErrorIsUsed() {
    ProblemResolutionFacade facade = facadeWith(List.of());

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new PlainException(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem).isEqualTo(Problem.of(500));
  }

  @Test
  void givenUnknownException_whenResolvingWithOtherStatus_thenGivenStatusIsUsed() {
    ProblemResolutionFacade facade = facadeWith(List.of());

    Problem problem =
        facade.resolve(
            ProblemContext.create(),
            new PlainException(),
            new HttpHeaders(),
            HttpStatus.METHOD_NOT_ALLOWED);

    assertThat(problem).isEqualTo(Problem.of(405));
  }

  private ProblemResolutionFacade facadeWith(List<ProblemResolver> resolvers) {
    return new DefaultProblemResolutionFacade(
        problemMapper, new DefaultProblemResolverStore(resolvers));
  }

  private static ProblemResolver fixedResolver(
      Class<? extends Exception> exceptionClass, Problem problem) {
    return new AbstractProblemResolver(exceptionClass) {
      @Override
      public Problem resolve(
          ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
        return problem;
      }
    };
  }

  @ProblemMapping(status = 418, title = "Mapped", detail = "mapped detail")
  private static class MappedException extends RuntimeException {}

  private static class PlainException extends RuntimeException {}

  private static class PlainSubException extends PlainException {}

  @ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "not yours")
  private static class ReasonStatusException extends RuntimeException {}

  @ResponseStatus(HttpStatus.GONE)
  private static class NoReasonStatusException extends RuntimeException {}

  private static class NoReasonStatusSubException extends NoReasonStatusException {}
}
