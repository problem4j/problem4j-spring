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
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.web.operations.ProblemTemplate;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.accept.RequestedContentTypeResolverBuilder;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ProblemHandlerResultHandlerTest {

  private static List<HttpMessageWriter<?>> codecWriters() {
    return ServerCodecConfigurer.create().getWriters();
  }

  private final ProblemOperations operations =
      ProblemTemplate.builder()
          .problemPostProcessor((context, problem) ->
              problem.toBuilder()
                  .extension("traceId", context != null ? context.get("traceId") : "no-context")
                  .build())
          .build();

  private RecordingHandler bodyHandler;
  private RecordingEntityHandler entityHandler;

  @BeforeEach
  void beforeEach() {
    bodyHandler = new RecordingHandler();
    entityHandler = new RecordingEntityHandler();
  }

  private ProblemHandlerResultHandler handler(HandlerResultHandler... delegates) {
    return handler(codecWriters(), delegates);
  }

  private ProblemHandlerResultHandler handler(
      List<HttpMessageWriter<?>> writers, HandlerResultHandler... delegates) {
    return new ProblemHandlerResultHandler(
        operations, ReactiveAdapterRegistry.getSharedInstance(), () -> List.of(delegates), writers);
  }

  private static HandlerResult result(String method, @Nullable Object value) {
    try {
      Method m = Methods.class.getDeclaredMethod(method);
      return new HandlerResult(new Methods(), value, new MethodParameter(m, -1));
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  private static MockServerWebExchange exchange() {
    return MockServerWebExchange.from(MockServerHttpRequest.get("/test"));
  }

  private static MockServerWebExchange exchange(MockServerHttpRequest.BaseBuilder<?> request) {
    return MockServerWebExchange.from(request);
  }

  private static Problem conflict() {
    return Problem.builder().title("Conflict").status(409).detail("conflict").build();
  }

  @Test
  void givenDefaultOrder_whenSettingOrder_thenOrderIsChanged() {
    ProblemHandlerResultHandler handler = handler(bodyHandler);

    assertThat(handler.getOrder()).isEqualTo(-100);
    handler.setOrder(5);
    assertThat(handler.getOrder()).isEqualTo(5);
  }

  @Test
  void givenProblemLikeResults_whenCheckingSupport_thenSupported() {
    ProblemHandlerResultHandler handler = handler(bodyHandler);

    assertThat(handler.supports(result("problem", conflict()))).isTrue();
    assertThat(handler.supports(result("problemDetail", ProblemDetail.forStatus(409)))).isTrue();
    assertThat(
            handler.supports(result("errorResponse", new ErrorResponseException(HttpStatus.GONE))))
        .isTrue();
    assertThat(handler.supports(result("responseEntity", ResponseEntity.ok(conflict())))).isTrue();
    assertThat(handler.supports(result("mono", Mono.just(conflict())))).isTrue();
    assertThat(handler.supports(result("object", conflict()))).isTrue();
    assertThat(handler.supports(result("object", null))).isTrue();
  }

  @Test
  void givenOtherResults_whenCheckingSupport_thenNotSupported() {
    ProblemHandlerResultHandler handler = handler(bodyHandler);

    assertThat(handler.supports(result("string", "value"))).isFalse();
    assertThat(handler.supports(result("object", "value"))).isFalse();
    assertThat(handler.supports(result("flux", Flux.just(conflict())))).isFalse();
    assertThat(handler.supports(result("empty", Mono.empty()))).isFalse();
    assertThat(handler.supports(result("stringEntity", ResponseEntity.ok("value")))).isFalse();
  }

  @Test
  void givenNoSupportingDelegate_whenCheckingSupportAndHandling_thenNotSupportedAndFails() {
    RecordingHandler unsupported = new RecordingHandler();
    unsupported.supported = false;
    ProblemHandlerResultHandler handler = handler(unsupported);
    HandlerResult result = result("problem", conflict());

    assertThat(handler.supports(result)).isFalse();
    assertThat(
            handler
                .handleResult(exchange(), result)
                .onErrorResume(e -> {
                  assertThat(e).isInstanceOf(IllegalStateException.class);
                  return Mono.empty();
                })
                .block())
        .isNull();
    assertThat(unsupported.result).isNull();
  }

  @Test
  void givenProblem_whenHandlingWithBodyDelegate_thenStatusContentTypeAndPostProcessingApplied() {
    MockServerWebExchange exchange = exchange();
    exchange
        .getAttributes()
        .put(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create().put("traceId", "abc"));

    handler(bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(exchange.getResponse().getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(bodyHandler.value())
        .isInstanceOfSatisfying(
            Problem.class,
            problem -> assertThat(problem.getExtensions()).containsEntry("traceId", "abc"));
  }

  @Test
  void givenPostProcessedProblem_whenHandling_thenItIsKeptAsItIs() {
    MockServerWebExchange exchange = exchange();
    Problem postProcessed = conflict();
    exchange.getAttributes().put(POST_PROCESSED_PROBLEM_ATTRIBUTE, postProcessed);

    handler(bodyHandler).handleResult(exchange, result("problem", postProcessed)).block();

    assertThat(bodyHandler.value()).isSameAs(postProcessed);
    assertThat(exchange.getResponse().getStatusCode()).isNull();
    assertThat(exchange.getAttributes()).doesNotContainKey(POST_PROCESSED_PROBLEM_ATTRIBUTE);
  }

  @Test
  void givenPostProcessedProblemInResponseEntity_whenHandling_thenItIsKeptAsItIs() {
    MockServerWebExchange exchange = exchange();
    Problem postProcessed = conflict();
    ResponseEntity<Problem> entity = ResponseEntity.status(HttpStatus.CONFLICT).body(postProcessed);
    exchange.getAttributes().put(POST_PROCESSED_PROBLEM_ATTRIBUTE, postProcessed);

    handler(entityHandler).handleResult(exchange, result("responseEntity", entity)).block();

    assertThat(entityHandler.value()).isSameAs(entity);
    assertThat(exchange.getAttributes()).doesNotContainKey(POST_PROCESSED_PROBLEM_ATTRIBUTE);
  }

  @Test
  void givenOtherProblemThanPostProcessed_whenHandling_thenPostProcessed() {
    MockServerWebExchange exchange = exchange();
    Problem postProcessed = conflict();
    exchange.getAttributes().put(POST_PROCESSED_PROBLEM_ATTRIBUTE, postProcessed);
    exchange
        .getAttributes()
        .put(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create().put("traceId", "abc"));

    handler(bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(bodyHandler.value())
        .isInstanceOfSatisfying(
            Problem.class,
            problem -> assertThat(problem.getExtensions()).containsEntry("traceId", "abc"));
    assertThat(exchange.getAttributes())
        .containsEntry(POST_PROCESSED_PROBLEM_ATTRIBUTE, postProcessed);
  }

  @Test
  void givenProblemAndStatusAlreadySet_whenHandling_thenStatusIsKept() {
    MockServerWebExchange exchange = exchange();
    exchange.getResponse().setStatusCode(HttpStatus.ACCEPTED);

    handler(bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  void givenProblem_whenHandlingWithEntityDelegate_thenWrappedInResponseEntity() {
    handler(entityHandler).handleResult(exchange(), result("problem", conflict())).block();

    assertThat(entityHandler.value())
        .isInstanceOfSatisfying(
            ResponseEntity.class,
            entity -> {
              assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
              assertThat(entity.getHeaders().getContentType())
                  .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
              assertThat(entity.getBody()).isInstanceOf(Problem.class);
            });
  }

  @Test
  void givenProducibleMediaTypes_whenHandlingProblem_thenContentTypeIsNotSet() {
    MockServerWebExchange exchange = exchange();
    exchange
        .getAttributes()
        .put(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, Set.of(MediaType.APPLICATION_XML));

    handler(bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(exchange.getResponse().getHeaders().getContentType()).isNull();
  }

  @Test
  void givenNoWriters_whenHandlingProblem_thenContentTypeIsNotSet() {
    MockServerWebExchange exchange = exchange();

    handler(List.of(), bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(exchange.getResponse().getHeaders().getContentType()).isNull();
  }

  @Test
  void givenAcceptNotMatchingProblemTypes_whenHandlingProblem_thenFirstProblemTypeIsUsed() {
    MockServerWebExchange exchange =
        exchange(MockServerHttpRequest.get("/test").accept(MediaType.TEXT_PLAIN));

    handler(bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(exchange.getResponse().getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenProblemDetail_whenHandling_thenPostProcessedInPlace() {
    ProblemDetail detail = ProblemDetail.forStatus(409);

    handler(bodyHandler).handleResult(exchange(), result("problemDetail", detail)).block();

    assertThat(bodyHandler.value()).isSameAs(detail);
    assertThat(requireNonNull(detail.getProperties())).containsEntry("traceId", "no-context");
  }

  @Test
  void givenErrorResponse_whenHandling_thenBodyPostProcessedInPlace() {
    ErrorResponseException response = new ErrorResponseException(HttpStatus.GONE);

    handler(entityHandler).handleResult(exchange(), result("errorResponse", response)).block();

    assertThat(entityHandler.value()).isSameAs(response);
    assertThat(requireNonNull(response.getBody().getProperties()))
        .containsEntry("traceId", "no-context");
  }

  @Test
  void givenEntityWithProblemDetail_whenHandling_thenBodyPostProcessedInPlace() {
    ProblemDetail detail = ProblemDetail.forStatus(409);
    ResponseEntity<ProblemDetail> entity = ResponseEntity.status(409).body(detail);

    handler(entityHandler).handleResult(exchange(), result("responseEntity", entity)).block();

    assertThat(entityHandler.value()).isSameAs(entity);
    assertThat(requireNonNull(detail.getProperties())).containsEntry("traceId", "no-context");
  }

  @Test
  void givenResponseEntityWithProblem_whenHandling_thenNewEntityWithProcessedProblem() {
    ResponseEntity<Problem> entity =
        ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).header("X-Custom", "1").body(conflict());

    handler(entityHandler).handleResult(exchange(), result("responseEntity", entity)).block();

    assertThat(entityHandler.value())
        .isInstanceOfSatisfying(
            ResponseEntity.class,
            transformed -> {
              assertThat(transformed.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
              assertThat(transformed.getHeaders().getFirst("X-Custom")).isEqualTo("1");
              assertThat(transformed.getHeaders().getContentType())
                  .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
              assertThat(((Problem) requireNonNull(transformed.getBody())).getExtensions())
                  .containsEntry("traceId", "no-context");
            });
  }

  @Test
  void givenResponseEntityWithContentType_whenHandling_thenContentTypeIsKept() {
    ResponseEntity<Problem> entity =
        ResponseEntity.status(409).contentType(MediaType.APPLICATION_JSON).body(conflict());

    handler(entityHandler).handleResult(exchange(), result("responseEntity", entity)).block();

    assertThat(
            ((ResponseEntity<?>) requireNonNull(entityHandler.value()))
                .getHeaders()
                .getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON);
  }

  @Test
  void givenHttpEntityWithProblem_whenHandling_thenNewHttpEntityWithProcessedProblem() {
    HttpEntity<Problem> entity = new HttpEntity<>(conflict());

    handler(entityHandler).handleResult(exchange(), result("httpEntity", entity)).block();

    assertThat(entityHandler.value())
        .isInstanceOfSatisfying(
            HttpEntity.class,
            transformed -> {
              assertThat(transformed).isNotInstanceOf(ResponseEntity.class);
              assertThat(((Problem) requireNonNull(transformed.getBody())).getExtensions())
                  .containsEntry("traceId", "no-context");
            });
  }

  @Test
  void givenHttpEntityWithOtherBody_whenHandling_thenEntityIsKept() {
    HttpEntity<Object> entity = new HttpEntity<>("value");

    handler(entityHandler).handleResult(exchange(), result("objectEntity", entity)).block();

    assertThat(entityHandler.value()).isSameAs(entity);
  }

  @Test
  void givenMonoOfProblem_whenHandling_thenContentTypeSetBeforeDelegating() {
    MockServerWebExchange exchange = exchange();

    handler(bodyHandler).handleResult(exchange, result("mono", Mono.just(conflict()))).block();

    assertThat(bodyHandler.contentTypeAtHandling.get())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(((Mono<?>) requireNonNull(bodyHandler.value())).block())
        .isInstanceOfSatisfying(
            Problem.class,
            problem -> assertThat(problem.getExtensions()).containsEntry("traceId", "no-context"));
  }

  @Test
  void givenEmptyMono_whenHandling_thenDelegatedWithEmptyValue() {
    handler(bodyHandler).handleResult(exchange(), result("mono", Mono.<Problem>empty())).block();

    assertThat(((Mono<?>) requireNonNull(bodyHandler.value())).block()).isNull();
  }

  @Test
  void givenNullValue_whenHandling_thenDelegatedWithNullValue() {
    handler(bodyHandler).handleResult(exchange(), result("object", null)).block();

    assertThat(bodyHandler.result).isNotNull();
    assertThat(bodyHandler.value()).isNull();
  }

  @Test
  void givenFailingPostProcessor_whenHandling_thenOriginalValueIsDelegated() {
    Problem problem = conflict();
    ProblemHandlerResultHandler handler =
        new ProblemHandlerResultHandler(
            ProblemTemplate.builder()
                .problemPostProcessor((context, p) -> {
                  throw new IllegalStateException("broken");
                })
                .build(),
            ReactiveAdapterRegistry.getSharedInstance(),
            () -> List.of(bodyHandler),
            codecWriters());

    handler.handleResult(exchange(), result("problem", problem)).block();

    assertThat(bodyHandler.value()).isSameAs(problem);
  }

  @Test
  void givenExceptionHandler_whenHandling_thenItIsKeptOnTransformedResult() {
    HandlerResult result = result("problem", conflict());
    result.setExceptionHandler((exchange, ex) -> Mono.empty());

    handler(bodyHandler).handleResult(exchange(), result).block();

    assertThat(requireNonNull(bodyHandler.result).getExceptionHandler())
        .isSameAs(result.getExceptionHandler());
  }

  @Test
  void givenResponseContentTypeAlreadySet_whenHandlingProblem_thenContentTypeIsKept() {
    MockServerWebExchange exchange = exchange();
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    handler(bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(exchange.getResponse().getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON);
  }

  @Test
  void givenEmptyProducibleMediaTypes_whenHandlingProblem_thenProblemContentTypeIsSet() {
    MockServerWebExchange exchange = exchange();
    exchange.getAttributes().put(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, Set.of());

    handler(bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(exchange.getResponse().getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void
      givenWriterNotWritingProblemAndDuplicateWriters_whenHandlingProblem_thenOnlyMatchingTypeIsUsed() {
    List<HttpMessageWriter<?>> writers = new ArrayList<>();
    writers.add(new DetailOnlyWriter());
    writers.addAll(codecWriters());
    writers.addAll(codecWriters());
    MockServerWebExchange exchange = exchange();

    handler(writers, bodyHandler).handleResult(exchange, result("problem", conflict())).block();

    assertThat(exchange.getResponse().getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  /** Declares a concrete media type for {@link ProblemDetail}, but cannot write {@link Problem}. */
  static class DetailOnlyWriter implements HttpMessageWriter<Object> {

    private static final MediaType DETAIL_ONLY =
        MediaType.parseMediaType("application/detail-only");

    @Override
    public List<MediaType> getWritableMediaTypes() {
      return List.of(DETAIL_ONLY);
    }

    @Override
    public boolean canWrite(ResolvableType elementType, @Nullable MediaType mediaType) {
      return ProblemDetail.class.isAssignableFrom(elementType.toClass());
    }

    @Override
    public Mono<Void> write(
        Publisher<?> inputStream,
        ResolvableType elementType,
        @Nullable MediaType mediaType,
        ReactiveHttpOutputMessage message,
        Map<String, Object> hints) {
      return Mono.empty();
    }
  }

  @SuppressWarnings("unused")
  static class Methods {

    Problem problem() {
      return conflict();
    }

    ProblemDetail problemDetail() {
      return ProblemDetail.forStatus(409);
    }

    ErrorResponse errorResponse() {
      return new ErrorResponseException(HttpStatus.GONE);
    }

    ResponseEntity<Problem> responseEntity() {
      return ResponseEntity.ok(conflict());
    }

    HttpEntity<Problem> httpEntity() {
      return new HttpEntity<>(conflict());
    }

    HttpEntity<Object> objectEntity() {
      return new HttpEntity<>("value");
    }

    ResponseEntity<String> stringEntity() {
      return ResponseEntity.ok("value");
    }

    Mono<Problem> mono() {
      return Mono.just(conflict());
    }

    Flux<Problem> flux() {
      return Flux.just(conflict());
    }

    Mono<Void> empty() {
      return Mono.empty();
    }

    String string() {
      return "value";
    }

    Object object() {
      return conflict();
    }
  }

  static class RecordingHandler implements HandlerResultHandler {

    boolean supported = true;
    @Nullable HandlerResult result;
    final AtomicReference<@Nullable MediaType> contentTypeAtHandling = new AtomicReference<>();

    @Override
    public boolean supports(HandlerResult result) {
      return supported;
    }

    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
      this.result = result;
      contentTypeAtHandling.set(exchange.getResponse().getHeaders().getContentType());
      return Mono.empty();
    }

    @Nullable Object value() {
      return requireNonNull(result).getReturnValue();
    }
  }

  static class RecordingEntityHandler extends ResponseEntityResultHandler {

    @Nullable HandlerResult result;

    RecordingEntityHandler() {
      super(codecWriters(), new RequestedContentTypeResolverBuilder().build());
    }

    @Override
    public boolean supports(HandlerResult result) {
      return true;
    }

    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
      this.result = result;
      return Mono.empty();
    }

    @Nullable Object value() {
      return requireNonNull(result).getReturnValue();
    }
  }
}
