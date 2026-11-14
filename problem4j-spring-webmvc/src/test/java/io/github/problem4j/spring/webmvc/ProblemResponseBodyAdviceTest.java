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

package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.web.operations.ProblemTemplate;
import java.lang.reflect.Method;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

class ProblemResponseBodyAdviceTest {

  private final ProblemOperations operations =
      ProblemTemplate.builder()
          .problemPostProcessor((context, problem) ->
              problem.toBuilder()
                  .extension("traceId", context != null ? context.get("traceId") : "no-context")
                  .build())
          .build();

  private final ProblemResponseBodyAdvice advice = new ProblemResponseBodyAdvice(operations);

  private MockHttpServletRequest servletRequest;
  private MockHttpServletResponse servletResponse;
  private ServletServerHttpRequest request;
  private ServletServerHttpResponse response;

  @BeforeEach
  void beforeEach() {
    servletRequest = new MockHttpServletRequest("GET", "/test");
    servletResponse = new MockHttpServletResponse();
    request = new ServletServerHttpRequest(servletRequest);
    response = new ServletServerHttpResponse(servletResponse);
  }

  private static MethodParameter returnType(String method) {
    try {
      Method m = Methods.class.getDeclaredMethod(method);
      return new MethodParameter(m, -1);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  private @Nullable Object write(@Nullable Object body, String method, MediaType contentType) {
    return write(body, method, contentType, request, response);
  }

  private @Nullable Object write(
      @Nullable Object body,
      String method,
      MediaType contentType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    return advice.beforeBodyWrite(
        body,
        returnType(method),
        contentType,
        JacksonJsonHttpMessageConverter.class,
        request,
        response);
  }

  private static Problem conflict() {
    return Problem.of(409, "conflict");
  }

  @Test
  void givenAnyReturnType_whenCheckingSupport_thenSupported() {
    assertThat(advice.supports(returnType("string"), JacksonJsonHttpMessageConverter.class))
        .isTrue();
  }

  @Test
  void givenOtherBody_whenWriting_thenBodyIsKept() {
    assertThat(write("value", "string", MediaType.APPLICATION_JSON)).isEqualTo("value");
    assertThat(write(null, "string", MediaType.APPLICATION_JSON)).isNull();
  }

  @Test
  void givenProblem_whenWriting_thenPostProcessedWithStatusAndProblemJson() {
    servletRequest.setAttribute(
        PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create().put("traceId", "abc"));

    Object body = write(conflict(), "problem", MediaType.APPLICATION_JSON);

    assertThat(body)
        .isInstanceOfSatisfying(
            Problem.class,
            problem -> assertThat(problem.getExtensions()).containsEntry("traceId", "abc"));
    assertThat(servletResponse.getStatus()).isEqualTo(409);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenProblemSelectedAsXml_whenWriting_thenProblemXmlContentType() {
    write(conflict(), "problem", MediaType.APPLICATION_XML);

    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenProblemSelectedAsOtherType_whenWriting_thenContentTypeIsNotSet() {
    write(conflict(), "problem", MediaType.TEXT_PLAIN);

    assertThat(response.getHeaders().getContentType()).isNull();
  }

  @Test
  void givenContentTypeAlreadySet_whenWritingProblem_thenContentTypeIsKept() {
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    write(conflict(), "problem", MediaType.APPLICATION_JSON);

    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
  }

  @Test
  void givenProducibleMediaTypes_whenWritingProblem_thenContentTypeIsNotSet() {
    servletRequest.setAttribute(
        HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, Set.of(MediaType.APPLICATION_JSON));

    write(conflict(), "problem", MediaType.APPLICATION_JSON);

    assertThat(response.getHeaders().getContentType()).isNull();
  }

  @Test
  void givenEmptyProducibleMediaTypes_whenWritingProblem_thenProblemJson() {
    servletRequest.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, Set.of());

    write(conflict(), "problem", MediaType.APPLICATION_JSON);

    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenStatusAlreadySet_whenWritingProblem_thenStatusIsKept() {
    servletResponse.setStatus(201);

    write(conflict(), "problem", MediaType.APPLICATION_JSON);

    assertThat(servletResponse.getStatus()).isEqualTo(201);
  }

  @Test
  void givenProblemWithUnknownStatus_whenWriting_thenStatusIsKept() {
    write(Problem.of(599), "problem", MediaType.APPLICATION_JSON);

    assertThat(servletResponse.getStatus()).isEqualTo(200);
  }

  @Test
  void givenProblemInResponseEntity_whenWriting_thenStatusIsNotChanged() {
    write(conflict(), "responseEntity", MediaType.APPLICATION_JSON);

    assertThat(servletResponse.getStatus()).isEqualTo(200);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenProblemDetail_whenWriting_thenPostProcessedInPlace() {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);

    Object body = write(detail, "problemDetail", MediaType.APPLICATION_PROBLEM_JSON);

    assertThat(body).isSameAs(detail);
    assertThat(detail.getProperties()).containsEntry("traceId", "no-context");
  }

  @Test
  void givenNonServletRequestAndResponse_whenWritingProblem_thenOnlyContentTypeIsApplied() {
    ServerHttpRequest request = mock(ServerHttpRequest.class);
    ServerHttpResponse response = mock(ServerHttpResponse.class);
    HttpHeaders headers = new HttpHeaders();
    when(response.getHeaders()).thenReturn(headers);

    Object body = write(conflict(), "problem", MediaType.APPLICATION_JSON, request, response);

    assertThat(body)
        .isInstanceOfSatisfying(
            Problem.class,
            problem -> assertThat(problem.getExtensions()).containsEntry("traceId", "no-context"));
    assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenFailingPostProcessor_whenWriting_thenOriginalBodyIsKept() {
    ProblemResponseBodyAdvice failing =
        new ProblemResponseBodyAdvice(
            ProblemTemplate.builder()
                .problemPostProcessor((context, problem) -> {
                  throw new IllegalStateException("broken");
                })
                .build());
    Problem problem = conflict();

    Object body =
        failing.beforeBodyWrite(
            problem,
            returnType("problem"),
            MediaType.APPLICATION_JSON,
            JacksonJsonHttpMessageConverter.class,
            request,
            response);

    assertThat(body).isSameAs(problem);
  }

  @SuppressWarnings("unused")
  static class Methods {

    Problem problem() {
      return conflict();
    }

    ProblemDetail problemDetail() {
      return ProblemDetail.forStatus(409);
    }

    ResponseEntity<Problem> responseEntity() {
      return ResponseEntity.ok(conflict());
    }

    String string() {
      return "value";
    }
  }
}
