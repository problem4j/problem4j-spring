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

package io.github.problem4j.spring.web.operations;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;

class ProblemTemplateTest {

  private final ProblemOperations operations =
      ProblemTemplate.builder()
          .problemPostProcessor((context, problem) ->
              problem.toBuilder()
                  .instance(
                      URI.create(
                          "https://example.org/trace/"
                              + (context != null ? context.get("traceId") : null)))
                  .extension("processed", true)
                  .build())
          .build();

  @Test
  void givenProblemDetail_whenPostProcessing_thenSameInstanceIsPostProcessed() {
    CustomProblemDetail detail = new CustomProblemDetail();
    detail.setStatus(HttpStatus.CONFLICT.value());
    detail.setDetail("conflict");
    detail.setProperty("origin", "app");

    ProblemDetail result =
        operations.postProcess(ProblemContext.create().put("traceId", "abc"), detail);

    assertThat(result).isSameAs(detail);
    assertThat(result.getStatus()).isEqualTo(409);
    assertThat(result.getDetail()).isEqualTo("conflict");
    assertThat(result.getInstance()).isEqualTo(URI.create("https://example.org/trace/abc"));
    assertThat(result.getProperties())
        .containsEntry("origin", "app")
        .containsEntry("processed", true);
    assertThat(detail.custom).isEqualTo("kept");
  }

  @Test
  void givenProblemDetailWithoutType_whenPostProcessing_thenTypeStaysUnset() {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);

    ProblemDetail result = operations.postProcess(ProblemContext.create(), detail);

    assertThat(result.getType()).isNull();
  }

  @Test
  void givenProblemDetailWithType_whenPostProcessing_thenTypeRetained() {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    detail.setType(URI.create("https://example.org/conflict"));

    ProblemDetail result = operations.postProcess(ProblemContext.create(), detail);

    assertThat(result.getType()).isEqualTo(URI.create("https://example.org/conflict"));
  }

  @Test
  void givenProblem_whenPostProcessing_thenPostProcessed() {
    Problem result =
        operations.postProcess(ProblemContext.create().put("traceId", "abc"), Problem.of(409));

    assertThat(result.getInstance()).isEqualTo(URI.create("https://example.org/trace/abc"));
    assertThat(result.getExtensions()).containsEntry("processed", true);
  }

  @Test
  void givenKnownAndUnknownExceptions_whenCheckingSupport_thenOnlyKnownSupported() {
    assertThat(operations.supports(new ProblemException(Problem.of(400)))).isTrue();
    assertThat(operations.supports(new ResponseStatusException(HttpStatus.NOT_FOUND))).isTrue();
    assertThat(operations.supports(new IllegalStateException())).isFalse();
  }

  @Test
  void givenProblemException_whenResolving_thenProblemIsNotPostProcessed() {
    Problem problem = Problem.of(409, "conflict");

    Problem result =
        operations.resolve(
            ProblemContext.create(), new ProblemException(problem), new HttpHeaders());

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenErrorResponseException_whenResolving_thenItsStatusAndHeadersAreUsed() {
    HttpHeaders headers = new HttpHeaders();

    Problem result =
        operations.resolve(
            ProblemContext.create(),
            new MethodNotAllowedException(HttpMethod.POST, List.of(HttpMethod.GET)),
            headers);

    assertThat(result.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
    assertThat(result.getExtensions()).doesNotContainKey("processed");
    assertThat(headers.getAllow()).containsExactly(HttpMethod.GET);
  }

  @Test
  void givenUnknownException_whenResolving_thenInternalServerError() {
    Problem result =
        operations.resolve(ProblemContext.create(), new IllegalStateException(), new HttpHeaders());

    assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }

  @Test
  void givenUnknownExceptionAndStatus_whenResolving_thenProblemOfGivenStatus() {
    Problem result =
        operations.resolve(
            ProblemContext.create(),
            new IllegalStateException(),
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST);

    assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(result.getExtensions()).doesNotContainKey("processed");
  }

  @Test
  void givenResolverAndStatus_whenResolving_thenResolverReceivesGivenStatus() {
    ProblemResolver resolver =
        new ProblemResolver() {
          @Override
          public Class<? extends Exception> getExceptionClass() {
            return IllegalArgumentException.class;
          }

          @Override
          public Problem resolve(
              ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
            return Problem.of(status.value());
          }
        };
    ProblemOperations withResolver =
        ProblemTemplate.builder()
            .problemResolverStore(new DefaultProblemResolverStore(List.of(resolver)))
            .build();

    Problem result =
        withResolver.resolve(
            ProblemContext.create(),
            new IllegalArgumentException(),
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST);

    assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void givenErrorResponseExceptionAndStatus_whenResolving_thenGivenStatusIsUsedAndHeadersKept() {
    HttpHeaders headers = new HttpHeaders();

    Problem result =
        operations.resolve(
            ProblemContext.create(),
            new MethodNotAllowedException(HttpMethod.POST, List.of(HttpMethod.GET)),
            headers,
            HttpStatus.BAD_REQUEST);

    assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(headers.getAllow()).isEmpty();
  }

  @Test
  void givenProblemException_whenTranslating_thenResolvedProblemIsPostProcessed() {
    Problem result =
        operations.translate(
            ProblemContext.create().put("traceId", "abc"),
            new ProblemException(Problem.of(409, "conflict")),
            new HttpHeaders());

    assertThat(result.getStatus()).isEqualTo(409);
    assertThat(result.getDetail()).isEqualTo("conflict");
    assertThat(result.getInstance()).isEqualTo(URI.create("https://example.org/trace/abc"));
    assertThat(result.getExtensions()).containsEntry("processed", true);
  }

  @Test
  void
      givenBuilderWithDefaults_whenUsed_thenProblemsAreNotModifiedAndOnlyKnownExceptionsSupported() {
    ProblemOperations defaults = ProblemTemplate.builder().build();
    Problem problem = Problem.of(409);

    assertThat(defaults.postProcess(ProblemContext.create(), problem)).isSameAs(problem);
    assertThat(defaults.supports(new ProblemException(Problem.of(400)))).isTrue();
    assertThat(defaults.supports(new IllegalStateException())).isFalse();
  }

  static class CustomProblemDetail extends ProblemDetail {
    String custom = "kept";
  }

  @Test
  void
      givenProblemDetailWithoutPropertiesAndNoExtensionsAdded_whenPostProcessing_thenPropertiesStayUnset() {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);

    ProblemDetail result =
        ProblemTemplate.builder().build().postProcess(ProblemContext.create(), detail);

    assertThat(result.getProperties()).isNull();
  }

  @Test
  void givenProblemDetailWithEmptyProperties_whenPostProcessing_thenPropertiesStayEmpty() {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    detail.setProperties(new LinkedHashMap<>());

    ProblemDetail result =
        ProblemTemplate.builder().build().postProcess(ProblemContext.create(), detail);

    assertThat(result.getProperties()).isEmpty();
  }
}
