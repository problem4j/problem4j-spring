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
 * {@link ResponseBodyAdvice} applying {@code ProblemPostProcessor} to {@link Problem}s and Spring's
 * {@link ProblemDetail}s written as response bodies by controllers and application
 * {@code @ExceptionHandler}s.
 *
 * <p>It does not handle any exception, so it does not interfere with exception handling model of
 * the application. A {@link ProblemDetail} stays a {@link ProblemDetail} (it is post-processed in
 * place), so content negotiation, status and headers selected by Spring remain unchanged.
 *
 * <p>A {@link Problem} returned as a plain body (not within {@link HttpEntity}) gets its status
 * applied to the response, unless a different status was already set (e.g. with
 * {@code @ResponseStatus}). If no content type was requested explicitly, a {@link Problem} written
 * as {@code application/json} or {@code application/xml} is written as {@code
 * application/problem+json} or {@code application/problem+xml}, similarly to {@link ProblemDetail}.
 *
 * <p>Part of the cooperate path, registered with {@code
 * problem4j.exception-handling.mode=cooperate}. With {@code
 * problem4j.exception-handling.mode=advice} (default), {@code Problem}s and {@code ProblemDetail}s
 * returned by the application are written without post-processing.
 *
 * @since 3.1.0
 */
@ControllerAdvice
public class ProblemResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  private static final Logger log = LoggerFactory.getLogger(ProblemResponseBodyAdvice.class);

  private final ProblemOperations problemOperations;

  /**
   * Creates a new {@code ProblemResponseBodyAdvice}.
   *
   * @param problemOperations the operations applying post-processing
   * @since 3.1.0
   */
  public ProblemResponseBodyAdvice(ProblemOperations problemOperations) {
    this.problemOperations = problemOperations;
  }

  /**
   * Returns {@code true}, as declared return type does not tell whether the body is a {@link
   * Problem} or {@link ProblemDetail} (e.g. {@code ResponseEntity<?>}).
   *
   * @param returnType the return type
   * @param converterType the selected converter type
   * @return {@code true}
   * @since 3.1.0
   */
  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  /**
   * Post-processes {@link Problem} and {@link ProblemDetail} bodies, keeping other bodies as they
   * are. If post-processing fails, the original body is written. A {@link Problem} rendered by a
   * controller advice or {@link ProblemErrorController} of Problem4J, which already post-processed
   * it, is written as it is.
   *
   * @param body the body to be written
   * @param returnType the return type of the handler method
   * @param selectedContentType the content type selected through content negotiation
   * @param selectedConverterType the converter type selected to write to the response
   * @param request the current request
   * @param response the current response
   * @return the body to write
   * @since 3.1.0
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
        && HttpStatus.resolve(problem.getStatus()) != null) {
      response.setStatusCode(HttpStatusCode.valueOf(problem.getStatus()));
    }
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
