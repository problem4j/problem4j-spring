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

import static io.github.problem4j.spring.web.AttributeSupport.POST_PROCESSED_PROBLEM_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * {@link ResponseBodyAdvice} post-processing {@link Problem}s and {@link ProblemDetail}s returned
 * by the application.
 *
 * <p>A {@link ProblemDetail} is post-processed in place. A {@link Problem} returned without {@link
 * HttpEntity} gets its status applied, unless another one was already set, and {@code
 * application/problem+json} (or {@code +xml}) content type, unless one was requested.
 *
 * @since 3.2.0
 */
@ControllerAdvice
public class ProblemResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  private static final Logger log = LoggerFactory.getLogger(ProblemResponseBodyAdvice.class);

  private final ProblemOperations problemOperations;

  /**
   * Creates a new {@code ProblemResponseBodyAdvice}.
   *
   * @param problemOperations the operations applying post-processing
   * @since 3.2.0
   */
  public ProblemResponseBodyAdvice(ProblemOperations problemOperations) {
    this.problemOperations = problemOperations;
  }

  /**
   * Returns {@code true}, as the return type does not tell whether the body is a problem.
   *
   * @param returnType the return type
   * @param converterType the selected converter type
   * @return {@code true}
   * @since 3.2.0
   */
  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  /**
   * Post-processes {@link Problem} and {@link ProblemDetail} bodies. A problem already
   * post-processed by Problem4J is written as it is.
   *
   * @param body the body to be written
   * @param returnType the return type of the handler method
   * @param selectedContentType the selected content type
   * @param selectedConverterType the selected converter type
   * @param request the current request
   * @param response the current response
   * @return the body to write
   * @since 3.2.0
   */
  @Override
  public @Nullable Object beforeBodyWrite(
      @Nullable Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (!(body instanceof Problem) && !(body instanceof ProblemDetail)) {
      return body;
    }
    if (isPostProcessed(body, request)) {
      return body;
    }
    try {
      ProblemContext context = findProblemContext(request);
      if (body instanceof ProblemDetail detail) {
        return problemOperations.postProcess(context, detail);
      }
      Problem problem = problemOperations.postProcess(context, (Problem) body);
      if (!isEntity(returnType)) {
        applyStatus(problem, response);
      }
      applyContentType(selectedContentType, request, response);
      return problem;
    } catch (RuntimeException e) {
      log.warn("Unable to post-process {} response body", body.getClass().getName(), e);
      return body;
    }
  }

  // Identity of the rendered problem tells it apart from any other problem written as response.
  @SuppressWarnings("ReferenceEquality")
  private static boolean isPostProcessed(Object body, ServerHttpRequest request) {
    if (request instanceof ServletServerHttpRequest servletRequest
        && servletRequest.getServletRequest().getAttribute(POST_PROCESSED_PROBLEM_ATTRIBUTE)
            == body) {
      servletRequest.getServletRequest().removeAttribute(POST_PROCESSED_PROBLEM_ATTRIBUTE);
      return true;
    }
    return false;
  }

  private static boolean isEntity(MethodParameter returnType) {
    Class<?> type = returnType.getParameterType();
    return HttpEntity.class.isAssignableFrom(type) || ErrorResponse.class.isAssignableFrom(type);
  }

  private static void applyStatus(Problem problem, ServerHttpResponse response) {
    if (response instanceof ServletServerHttpResponse servletResponse
        && servletResponse.getServletResponse().getStatus() == HttpStatus.OK.value()
        && isValidStatus(problem.getStatus())) {
      response.setStatusCode(HttpStatusCode.valueOf(problem.getStatus()));
    }
  }

  // A problem may carry no status (0) or one outside the 100-999 range of HttpStatusCode.
  private static boolean isValidStatus(int status) {
    return status >= 100 && status <= 999;
  }

  private static void applyContentType(
      MediaType selectedContentType, ServerHttpRequest request, ServerHttpResponse response) {
    if (response.getHeaders().getContentType() != null || hasProducibleMediaTypes(request)) {
      return;
    }
    if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(selectedContentType)) {
      response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    } else if (MediaType.APPLICATION_XML.equalsTypeAndSubtype(selectedContentType)) {
      response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_XML);
    }
  }

  private static boolean hasProducibleMediaTypes(ServerHttpRequest request) {
    return request instanceof ServletServerHttpRequest servletRequest
        && servletRequest
                .getServletRequest()
                .getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE)
            instanceof Collection<?> types
        && !types.isEmpty();
  }

  private static @Nullable ProblemContext findProblemContext(ServerHttpRequest request) {
    return request instanceof ServletServerHttpRequest servletRequest
            && servletRequest.getServletRequest().getAttribute(PROBLEM_CONTEXT_ATTRIBUTE)
                instanceof ProblemContext context
        ? context
        : null;
  }
}
