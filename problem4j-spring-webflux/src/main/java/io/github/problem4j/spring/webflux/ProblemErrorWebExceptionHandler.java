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

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.webflux.WebFluxAdviceSupport.logAdviceException;
import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.resolver.ProblemResolutionFacade;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.autoconfigure.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * A WebFlux {@link org.springframework.boot.webflux.error.ErrorWebExceptionHandler} that renders
 * HTTP problems (RFC 7807) instead of the default JSON error response.
 *
 * <p>It replaces the response body with a {@link Problem} object while preserving the original HTTP
 * status and content type {@code application/problem+json}.
 *
 * @since 1.2.0
 */
public class ProblemErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ProblemErrorWebExceptionHandler.class);

  private final ProblemPostProcessor problemPostProcessor;
  private final ProblemResolutionFacade problemResolutionFacade;
  private final List<AdviceWebFluxInspector> adviceWebFluxInspectors;

  /**
   * Constructs a new {@link ProblemErrorWebExceptionHandler}, rendering only the HTTP status of
   * errors (see {@link ProblemResolutionFacade#statusOnly()}).
   *
   * @param problemPostProcessor the post-processor for problems
   * @param errorAttributes the error attributes used to obtain error details
   * @param resources configuration for static resources
   * @param errorProperties configuration for error handling
   * @param applicationContext the current application context
   * @since 1.2.0
   */
  public ProblemErrorWebExceptionHandler(
      ProblemPostProcessor problemPostProcessor,
      ErrorAttributes errorAttributes,
      WebProperties.Resources resources,
      ErrorProperties errorProperties,
      ApplicationContext applicationContext) {
    this(
        problemPostProcessor,
        errorAttributes,
        resources,
        errorProperties,
        applicationContext,
        ProblemResolutionFacade.statusOnly(),
        List.of());
  }

  /**
   * Constructs a new {@link ProblemErrorWebExceptionHandler}, resolving exceptions with given
   * {@link ProblemResolutionFacade}.
   *
   * @param problemPostProcessor the post-processor for problems
   * @param errorAttributes the error attributes used to obtain error details
   * @param resources configuration for static resources
   * @param errorProperties configuration for error handling
   * @param applicationContext the current application context
   * @param problemResolutionFacade resolves exceptions into problems
   * @param adviceWebFluxInspectors inspectors notified about resolved exceptions
   * @throws NullPointerException if {@code problemResolutionFacade} or {@code
   *     adviceWebFluxInspectors} is {@code null}
   * @since 3.1.0
   */
  public ProblemErrorWebExceptionHandler(
      ProblemPostProcessor problemPostProcessor,
      ErrorAttributes errorAttributes,
      WebProperties.Resources resources,
      ErrorProperties errorProperties,
      ApplicationContext applicationContext,
      ProblemResolutionFacade problemResolutionFacade,
      List<? extends AdviceWebFluxInspector> adviceWebFluxInspectors) {
    super(errorAttributes, resources, errorProperties, applicationContext);
    this.problemPostProcessor = problemPostProcessor;
    this.problemResolutionFacade = problemResolutionFacade;
    this.adviceWebFluxInspectors = new ArrayList<>(adviceWebFluxInspectors);
  }

  /**
   * Returns a routing function that handles all requests and delegates to {@link
   * #renderErrorResponse(ServerRequest)}.
   *
   * @param errorAttributes the error attributes used to resolve errors
   * @return a routing function for handling errors
   * @see #renderErrorResponse(ServerRequest)
   * @since 1.2.0
   */
  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return route(all(), this::renderErrorResponse);
  }

  /**
   * Renders an error response as a {@link Problem} in {@code application/problem+json} format,
   * resolving the exception that caused the error with {@link ProblemResolutionFacade}.
   *
   * @param request the current server request
   * @return a {@link Mono} emitting the problem response
   * @since 1.2.0
   */
  @Override
  protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    ServerWebExchange exchange = request.exchange();
    ProblemContext context =
        exchange.getAttributeOrDefault(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    HttpStatusCode errorStatus =
        HttpStatusCode.valueOf(
            getHttpStatus(getErrorAttributes(request, ErrorAttributeOptions.defaults())));
    Throwable error = getError(request);
    Exception ex = error instanceof Exception e ? e : new IllegalStateException(error);

    Problem problem;
    try {
      problem = problemResolutionFacade.resolve(context, ex, headers, errorStatus);
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, exchange, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    HttpStatus status =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    for (AdviceWebFluxInspector inspector : adviceWebFluxInspectors) {
      inspector.inspect(context, problem, ex, headers, status, exchange);
    }

    return ServerResponse.status(status)
        .headers(h -> h.addAll(headers))
        .body(BodyInserters.fromValue(problem));
  }
}
