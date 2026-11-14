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

import static io.github.problem4j.spring.web.AttributeSupport.POST_PROCESSED_PROBLEM_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.DispatchExceptionHandler;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * {@link HandlerResultHandler} applying {@code ProblemPostProcessor} to {@link Problem}s and
 * Spring's {@link ProblemDetail}s (also carried by {@link ErrorResponse}) returned by controllers
 * and application {@code @ExceptionHandler}s.
 *
 * <p>A {@link ProblemDetail} stays a {@link ProblemDetail} (it is post-processed in place), so
 * content negotiation, status and headers selected by Spring remain unchanged. A {@link Problem}
 * returned as a plain value (not within {@link HttpEntity}) gets its status applied to the
 * response, unless a status other than {@code 200 OK} was already set (e.g. with
 * {@code @ResponseStatus}). If no content type was requested explicitly, a {@link Problem} is
 * written with a content type Spring would use for {@link ProblemDetail} (e.g. {@code
 * application/problem+json}).
 *
 * <p>Ordered before Spring's own result handlers. After transforming the return value, it delegates
 * to the first of the given result handlers (typically {@code ResponseEntityResultHandler} and
 * {@code ResponseBodyResultHandler}) supporting the original result, so content negotiation and
 * message encoding stay unchanged. Results of other types are not supported by this handler.
 *
 * <p>Part of the cooperate path, registered with {@code
 * problem4j.exception-handling.mode=cooperate}. With {@code
 * problem4j.exception-handling.mode=advice} (default), {@code Problem}s and {@code ProblemDetail}s
 * returned by the application are written without post-processing.
 *
 * @since 3.1.0
 */
public class ProblemHandlerResultHandler implements HandlerResultHandler, Ordered {

  private static final Logger log = LoggerFactory.getLogger(ProblemHandlerResultHandler.class);

  private final ProblemOperations problemOperations;
  private final ReactiveAdapterRegistry adapterRegistry;
  private final Supplier<? extends List<? extends HandlerResultHandler>> delegates;
  private final List<HttpMessageWriter<?>> messageWriters;

  private int order = -100;

  /**
   * Creates a new {@code ProblemHandlerResultHandler}.
   *
   * @param problemOperations the operations applying post-processing
   * @param adapterRegistry the registry of reactive type adapters
   * @param delegates supplies result handlers writing transformed results, in order of precedence
   * @param messageWriters the writers used to determine content type of {@link Problem}s
   * @since 3.1.0
   */
  public ProblemHandlerResultHandler(
      ProblemOperations problemOperations,
      ReactiveAdapterRegistry adapterRegistry,
      Supplier<? extends List<? extends HandlerResultHandler>> delegates,
      List<HttpMessageWriter<?>> messageWriters) {
    this.problemOperations = problemOperations;
    this.adapterRegistry = adapterRegistry;
    this.delegates = delegates;
    this.messageWriters = new ArrayList<>(messageWriters);
  }

  /**
   * Returns the order of this handler, {@code -100} by default, which precedes Spring's own result
   * handlers.
   *
   * @return the order of this handler
   * @since 3.1.0
   */
  @Override
  public int getOrder() {
    return order;
  }

  /**
   * Sets the order of this handler.
   *
   * @param order the order of this handler
   * @since 3.1.0
   */
  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * Checks whether the result may carry a {@link Problem}, {@link ProblemDetail} or {@link
   * ErrorResponse}, directly, as {@link HttpEntity} body, or wrapped in a single-value reactive
   * type, and whether any delegate supports it.
   *
   * @param result the result of handler invocation
   * @return {@code true} if the result may need transformation
   * @since 3.1.0
   */
  @Override
  public boolean supports(HandlerResult result) {
    return isCandidate(result) && findDelegate(result) != null;
  }

  private boolean isCandidate(HandlerResult result) {
    Object value = result.getReturnValue();
    ResolvableType type = result.getReturnType();
    if (type.toClass() == Object.class && value != null) {
      type = ResolvableType.forInstance(value);
    }
    ReactiveAdapter adapter = adapterRegistry.getAdapter(type.resolve(), value);
    if (adapter != null) {
      if (adapter.isMultiValue() || adapter.isNoValue()) {
        return false;
      }
      type = type.getGeneric();
    }
    if (HttpEntity.class.isAssignableFrom(type.toClass())) {
      type = type.as(HttpEntity.class).getGeneric();
    }
    Class<?> clazz = type.toClass();
    return clazz == Object.class
        || Problem.class.isAssignableFrom(clazz)
        || ProblemDetail.class.isAssignableFrom(clazz)
        || ErrorResponse.class.isAssignableFrom(clazz);
  }

  private @Nullable HandlerResultHandler findDelegate(HandlerResult result) {
    for (HandlerResultHandler delegate : delegates.get()) {
      if (delegate.supports(result)) {
        return delegate;
      }
    }
    return null;
  }

  /**
   * Transforms the return value and delegates to the first result handler supporting it.
   *
   * @param exchange the current exchange
   * @param result the result of handler invocation
   * @return a {@link Mono} that completes when the result is handled
   * @since 3.1.0
   */
  @Override
  public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
    HandlerResultHandler delegate = findDelegate(result);
    if (delegate == null) {
      return Mono.error(new IllegalStateException("No delegate supports " + result));
    }
    boolean entity = delegate instanceof ResponseEntityResultHandler;

    Object value = result.getReturnValue();
    ReactiveAdapter adapter =
        value != null ? adapterRegistry.getAdapter(result.getReturnType().resolve(), value) : null;

    if (adapter != null) {
      // The value is awaited before delegating, as the delegate selects the content type before
      // the value is emitted, so it would ignore the content type set while transforming it.
      return Mono.from(adapter.toPublisher(value))
          .map(v -> Optional.of(safeTransform(v, entity, exchange)))
          .defaultIfEmpty(Optional.empty())
          .flatMap(v ->
              delegate.handleResult(
                  exchange, withReturnValue(result, adapter.fromPublisher(Mono.justOrEmpty(v)))));
    }

    @Nullable Object transformed = value != null ? safeTransform(value, entity, exchange) : null;
    return delegate.handleResult(exchange, withReturnValue(result, transformed));
  }

  private static HandlerResult withReturnValue(HandlerResult result, @Nullable Object value) {
    HandlerResult transformedResult =
        new HandlerResult(
            result.getHandler(), value, result.getReturnTypeSource(), result.getBindingContext());
    DispatchExceptionHandler exceptionHandler = result.getExceptionHandler();
    if (exceptionHandler != null) {
      transformedResult.setExceptionHandler(exceptionHandler);
    }
    return transformedResult;
  }

  private Object safeTransform(Object value, boolean entity, ServerWebExchange exchange) {
    try {
      return transform(value, entity, exchange);
    } catch (RuntimeException e) {
      log.warn("Unable to post-process {} result", value.getClass().getName(), e);
      return value;
    }
  }

  /**
   * Post-processes {@link Problem}s and {@link ProblemDetail}s, keeping other values as they are. A
   * {@link Problem} rendered by a controller advice of Problem4J, which already post-processed it,
   * is kept as it is.
   *
   * @param value the value returned from the handler method
   * @param entity whether the delegate expects {@link HttpEntity} for values carrying a status
   * @param exchange the current exchange
   * @return the transformed value
   * @since 3.1.0
   */
  protected Object transform(Object value, boolean entity, ServerWebExchange exchange) {
    if (isPostProcessed(value, exchange)) {
      return value;
    }
    ProblemContext context = findProblemContext(exchange);
    if (value instanceof Problem problem) {
      Problem processed = problemOperations.postProcess(context, problem);
      HttpHeaders headers = withProblemContentType(HttpHeaders.EMPTY, exchange);
      HttpStatusCode status = exchange.getResponse().getStatusCode();
      if ((status == null || status.value() == HttpStatus.OK.value())
          && isValidStatus(processed.getStatus())) {
        status = HttpStatusCode.valueOf(processed.getStatus());
      }
      if (entity) {
        return new ResponseEntity<>(processed, headers, status != null ? status : HttpStatus.OK);
      }
      if (status != null) {
        exchange.getResponse().setStatusCode(status);
      }
      exchange.getResponse().getHeaders().putAll(headers);
      return processed;
    }
    if (value instanceof ProblemDetail detail) {
      return problemOperations.postProcess(context, detail);
    }
    if (value instanceof ErrorResponse response) {
      problemOperations.postProcess(context, response.getBody());
      return response;
    }
    if (value instanceof HttpEntity<?> httpEntity) {
      if (httpEntity.getBody() instanceof ProblemDetail body) {
        problemOperations.postProcess(context, body);
      } else if (httpEntity.getBody() instanceof Problem body) {
        Problem processed = problemOperations.postProcess(context, body);
        HttpHeaders headers = withProblemContentType(httpEntity.getHeaders(), exchange);
        return httpEntity instanceof ResponseEntity<?> responseEntity
            ? new ResponseEntity<>(processed, headers, responseEntity.getStatusCode())
            : new HttpEntity<>(processed, headers);
      }
    }
    return value;
  }

  // A problem may carry no status (0) or one outside the 100-999 range of HttpStatusCode.
  private static boolean isValidStatus(int status) {
    return status >= 100 && status <= 999;
  }

  // Identity of the rendered problem tells it apart from any other problem written as response.
  @SuppressWarnings("ReferenceEquality")
  private static boolean isPostProcessed(Object value, ServerWebExchange exchange) {
    Object body = value instanceof HttpEntity<?> httpEntity ? httpEntity.getBody() : value;
    Object postProcessed = exchange.getAttribute(POST_PROCESSED_PROBLEM_ATTRIBUTE);
    if (body instanceof Problem && body == postProcessed) {
      exchange.getAttributes().remove(POST_PROCESSED_PROBLEM_ATTRIBUTE);
      return true;
    }
    return false;
  }

  private HttpHeaders withProblemContentType(HttpHeaders headers, ServerWebExchange exchange) {
    HttpHeaders result = new HttpHeaders();
    result.putAll(headers);
    if (result.getContentType() == null
        && exchange.getResponse().getHeaders().getContentType() == null) {
      MediaType contentType = selectProblemDetailMediaType(exchange);
      if (contentType != null) {
        result.setContentType(contentType);
      }
    }
    return result;
  }

  /**
   * Selects the content type Spring would use for {@link ProblemDetail}, which is not selected
   * automatically for {@link Problem} body, if the return type is not declared as {@link
   * ProblemDetail}.
   */
  private @Nullable MediaType selectProblemDetailMediaType(ServerWebExchange exchange) {
    Object producible = exchange.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
    if (producible instanceof Collection<?> types && !types.isEmpty()) {
      return null;
    }

    ResolvableType detailType = ResolvableType.forClass(ProblemDetail.class);
    ResolvableType problemType = ResolvableType.forClass(Problem.class);
    List<MediaType> problemTypes = new ArrayList<>();
    for (HttpMessageWriter<?> writer : messageWriters) {
      for (MediaType type : writer.getWritableMediaTypes(detailType)) {
        if (type.isConcrete()
            && writer.canWrite(problemType, type)
            && !problemTypes.contains(type)) {
          problemTypes.add(type);
        }
      }
    }
    if (problemTypes.isEmpty()) {
      return null;
    }

    List<MediaType> acceptable = new ArrayList<>(exchange.getRequest().getHeaders().getAccept());
    if (acceptable.isEmpty()) {
      acceptable.add(MediaType.ALL);
    }
    MimeTypeUtils.sortBySpecificity(acceptable);
    for (MediaType type : acceptable) {
      for (MediaType problemMediaType : problemTypes) {
        if (type.isCompatibleWith(problemMediaType)) {
          return problemMediaType;
        }
      }
    }
    return problemTypes.get(0);
  }

  private static @Nullable ProblemContext findProblemContext(ServerWebExchange exchange) {
    return exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) instanceof ProblemContext context
        ? context
        : null;
  }
}
