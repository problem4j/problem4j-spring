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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.web.operations.ProblemTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.servlet.ModelAndView;

class ProblemHandlerExceptionResolverTest {

  private final ProblemOperations operations = ProblemTemplate.builder().build();

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private List<ProblemContext> inspectedContexts;

  @BeforeEach
  void beforeEach() {
    request = new MockHttpServletRequest("GET", "/test");
    response = new MockHttpServletResponse();
    inspectedContexts = new ArrayList<>();
  }

  private ProblemHandlerExceptionResolver resolver(
      ProblemOperations operations, @Nullable List<HttpMessageConverter<?>> converters) {
    return new ProblemHandlerExceptionResolver(
        operations,
        () -> converters,
        List.of(
            (context, problem, ex, headers, status, webRequest) -> inspectedContexts.add(context)));
  }

  private ProblemHandlerExceptionResolver resolver() {
    return resolver(operations, List.of(new JacksonJsonHttpMessageConverter()));
  }

  private @Nullable ModelAndView resolve(ProblemHandlerExceptionResolver resolver, Exception ex) {
    return resolver.resolveException(request, response, null, ex);
  }

  @Test
  void givenDefaults_whenTogglingUnknownExceptions_thenValueIsChanged() {
    ProblemHandlerExceptionResolver resolver = resolver();

    assertThat(resolver.isUnknownExceptions()).isFalse();
    resolver.setUnknownExceptions(true);
    assertThat(resolver.isUnknownExceptions()).isTrue();
  }

  @Test
  void givenProblemException_whenResolving_thenProblemIsWrittenAndInspected() throws Exception {
    ProblemContext context = ProblemContext.create().put("traceId", "abc");
    request.setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, context);

    ModelAndView mav = resolve(resolver(), new ProblemException(Problem.of(409, "conflict")));

    assertThat(mav).isNotNull();
    assertThat(response.getStatus()).isEqualTo(409);
    assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    assertThat(response.getContentAsString()).contains("\"status\":409").contains("conflict");
    assertThat(inspectedContexts).containsExactly(context);
  }

  @Test
  void givenNoContextAttribute_whenResolving_thenNewContextIsInspected() {
    resolve(resolver(), new ProblemException(Problem.of(409)));

    assertThat(inspectedContexts).hasSize(1);
  }

  @Test
  void givenErrorResponseWithMultiValuedHeader_whenResolving_thenAllValuesAreWritten() {
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.METHOD_NOT_ALLOWED);
    ex.getHeaders().add("Allow", "GET");
    ex.getHeaders().add("Allow", "POST");

    resolve(resolver(), ex);

    assertThat(response.getStatus()).isEqualTo(405);
    assertThat(response.getHeaders("Allow")).containsExactly("GET", "POST");
  }

  @Test
  void givenProblemWithUnknownStatus_whenResolving_thenInternalServerErrorStatusIsUsed() {
    resolve(resolver(), new ProblemException(Problem.of(599)));

    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  void givenCommittedResponse_whenResolving_thenNotResolved() {
    response.setCommitted(true);

    assertThat(resolve(resolver(), new ProblemException(Problem.of(409)))).isNull();
    assertThat(inspectedContexts).isEmpty();
  }

  @Test
  void givenDisconnectedClient_whenResolving_thenNotResolved() {
    assertThat(resolve(resolver(), new IOException("Broken pipe"))).isNull();
  }

  @Test
  void givenUnknownExceptionAndUnknownExceptionsDisabled_whenResolving_thenNotResolved() {
    assertThat(resolve(resolver(), new IllegalStateException("unknown"))).isNull();
    assertThat(inspectedContexts).isEmpty();
  }

  @Test
  void givenUnknownExceptionAndUnknownExceptionsEnabled_whenResolving_thenInternalServerError() {
    ProblemHandlerExceptionResolver resolver = resolver();
    resolver.setUnknownExceptions(true);

    assertThat(resolve(resolver, new IllegalStateException("unknown"))).isNotNull();
    assertThat(response.getStatus()).isEqualTo(500);
    assertThat(inspectedContexts).hasSize(1);
  }

  @Test
  void givenFailingTranslation_whenResolving_thenInternalServerErrorProblemIsWritten() {
    ProblemOperations failing = new FailingOperations();

    assertThat(
            resolve(
                resolver(failing, List.of(new JacksonJsonHttpMessageConverter())),
                new IllegalStateException()))
        .isNotNull();
    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  void givenNoConverters_whenResolving_thenNotResolved() {
    assertThat(resolve(resolver(operations, null), new ProblemException(Problem.of(409)))).isNull();
  }

  @Test
  void givenNoMatchingConverter_whenResolving_thenNotResolved() {
    assertThat(
            resolve(
                resolver(operations, List.of(new StringHttpMessageConverter())),
                new ProblemException(Problem.of(409))))
        .isNull();
  }

  @Test
  void givenFailingConverter_whenResolving_thenNotResolved() {
    JacksonJsonHttpMessageConverter failing =
        new JacksonJsonHttpMessageConverter() {
          @Override
          protected void writeInternal(Object object, HttpOutputMessage outputMessage)
              throws IOException {
            throw new IOException("broken");
          }
        };

    assertThat(
            resolve(resolver(operations, List.of(failing)), new ProblemException(Problem.of(409))))
        .isNull();
  }

  static class FailingOperations implements ProblemOperations {

    @Override
    public boolean supports(Exception ex) {
      return true;
    }

    @Override
    public Problem resolve(ProblemContext context, Exception ex, HttpHeaders headers) {
      throw new IllegalStateException("broken");
    }

    @Override
    public Problem resolve(
        ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
      throw new IllegalStateException("broken");
    }

    @Override
    public Problem postProcess(@Nullable ProblemContext context, Problem problem) {
      return problem;
    }

    @Override
    public ProblemDetail postProcess(@Nullable ProblemContext context, ProblemDetail detail) {
      return detail;
    }
  }
}
