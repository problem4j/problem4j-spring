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
import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemContextSettings;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class ProblemContextWebFilterTest {

  @Test
  void givenNoTracingHeaderName_whenFilter_thenGeneratedTraceIdIsLowercase32HexCharacters() {
    ProblemContextWebFilter filter = new ProblemContextWebFilter(new ProblemContextSettings() {});
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE)).matches("^[0-9a-f]{32}$");
  }

  @Test
  void
      givenTracingHeaderNameConfigured_whenNoHeaderInRequest_thenGeneratedTraceIdIsLowercase32HexCharacters() {
    ProblemContextWebFilter filter = new ProblemContextWebFilter(tracingHeaderName("X-Trace-Id"));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE)).matches("^[0-9a-f]{32}$");
  }

  @Test
  void
      givenTracingHeaderNameConfigured_whenHeaderPresentInRequest_thenUsesRequestHeaderAsTraceId() {
    ProblemContextWebFilter filter = new ProblemContextWebFilter(tracingHeaderName("X-Trace-Id"));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/test").header("X-Trace-Id", "custom-trace-id").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo("custom-trace-id");
  }

  private static ProblemContextSettings tracingHeaderName(String headerName) {
    return new ProblemContextSettings() {
      @Override
      public String getTracingHeaderName() {
        return headerName;
      }
    };
  }

  @Test
  void givenDefaultOrder_whenSettingOrder_thenOrderIsChanged() {
    ProblemContextSettings settings = new ProblemContextSettings() {};
    ProblemContextWebFilter filter = new ProblemContextWebFilter(settings);

    assertThat(filter.getOrder()).isEqualTo(ProblemContextWebFilter.PRECEDENCE);
    assertThat(ProblemContextWebFilter.PRECEDENCE).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 10);
    assertThat(filter.getSettings()).isSameAs(settings);
    filter.setOrder(42);
    assertThat(filter.getOrder()).isEqualTo(42);
  }

  @Test
  void givenCustomizer_whenFilter_thenCustomizerAddsEntryToStoredContext() {
    ProblemContextWebFilter filter =
        new ProblemContextWebFilter(
            new ProblemContextSettings() {},
            List.of(
                ProblemContextCustomizer.sync(
                    (context, exchange) -> context.put("requestId", "req-1"))));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    ProblemContext context = requireNonNull(exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
    assertThat(context.get("requestId")).isEqualTo("req-1");
    assertThat(context.get("traceId")).matches("^[0-9a-f]{32}$");
  }

  @Test
  void givenCustomizerOverridingTraceId_whenFilter_thenAttributesHeaderAndReactorContextUseIt() {
    ProblemContextWebFilter filter =
        new ProblemContextWebFilter(
            tracingHeaderName("X-Trace-Id"),
            List.of(
                ProblemContextCustomizer.sync(
                    (context, exchange) -> context.put("traceId", "overridden"))));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/test").header("X-Trace-Id", "from-header").build());
    AtomicReference<Object> reactorTraceId = new AtomicReference<>();

    filter
        .filter(
            exchange,
            ex ->
                Mono.deferContextual(ctx -> {
                  reactorTraceId.set(ctx.get(TRACE_ID_ATTRIBUTE));
                  return Mono.empty();
                }))
        .block();

    ProblemContext context = requireNonNull(exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
    assertThat(context.get("traceId")).isEqualTo("overridden");
    assertThat((String) exchange.getAttribute(TRACE_ID_ATTRIBUTE)).isEqualTo("overridden");
    assertThat(exchange.getResponse().getHeaders().getFirst("X-Trace-Id")).isEqualTo("overridden");
    assertThat(reactorTraceId.get()).isEqualTo("overridden");
  }

  @Test
  void givenMultipleCustomizers_whenFilter_thenInvokedInGivenOrderWithExchange() {
    ProblemContextWebFilter filter =
        new ProblemContextWebFilter(
            new ProblemContextSettings() {},
            List.of(
                ProblemContextCustomizer.sync((context, exchange) ->
                    context.put(
                        "user", exchange.getRequest().getHeaders().getFirst("X-User") + "-first")),
                ProblemContextCustomizer.sync(
                    (context, exchange) -> context.put("user", context.get("user") + "-second"))));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/test").header("X-User", "alice").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    ProblemContext context = requireNonNull(exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
    assertThat(context.get("user")).isEqualTo("alice-first-second");
  }

  @Test
  void givenContextAlreadyInExchange_whenFilter_thenCustomizersAreNotInvoked() {
    ProblemContextWebFilter filter =
        new ProblemContextWebFilter(
            new ProblemContextSettings() {},
            List.of(
                ProblemContextCustomizer.sync(
                    (context, exchange) -> context.put("requestId", "req-1"))));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
    ProblemContext existing = ProblemContext.create().put("traceId", "existing");
    exchange.getAttributes().put(PROBLEM_CONTEXT_ATTRIBUTE, existing);

    filter.filter(exchange, ex -> Mono.empty()).block();

    assertThat(exchange.<ProblemContext>getAttribute(PROBLEM_CONTEXT_ATTRIBUTE)).isSameAs(existing);
    assertThat(existing.toMap()).containsOnlyKeys("traceId");
  }

  @Test
  void givenAsyncCustomizer_whenFilter_thenContextIsCustomizedBeforeChainContinues() {
    ProblemContextWebFilter filter =
        new ProblemContextWebFilter(
            new ProblemContextSettings() {},
            List.of((context, exchange) ->
                Mono.delay(Duration.ofMillis(20))
                    .doOnNext(tick -> context.put("requestId", "async-1"))
                    .then()));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
    AtomicReference<String> seenInChain = new AtomicReference<>();

    filter
        .filter(
            exchange,
            ex ->
                Mono.fromRunnable(() ->
                    seenInChain.set(
                        String.valueOf(
                            requireNonNull(
                                    ex.<ProblemContext>getAttribute(PROBLEM_CONTEXT_ATTRIBUTE))
                                .get("requestId")))))
        .block();

    assertThat(seenInChain.get()).isEqualTo("async-1");
  }

  @Test
  void givenFailingCustomizer_whenFilter_thenErrorIsPropagatedAndChainIsNotInvoked() {
    ProblemContextWebFilter filter =
        new ProblemContextWebFilter(
            new ProblemContextSettings() {},
            List.of((context, exchange) -> Mono.error(new IllegalStateException("boom"))));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
    AtomicReference<Boolean> chainInvoked = new AtomicReference<>(false);

    assertThatThrownBy(() ->
            filter
                .filter(
                    exchange,
                    ex -> {
                      chainInvoked.set(true);
                      return Mono.empty();
                    })
                .block())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("boom");
    assertThat(chainInvoked.get()).isFalse();
  }

  @Test
  void givenNoCustomizers_whenFilter_thenContextHasOnlyTraceId() {
    ProblemContextWebFilter filter = new ProblemContextWebFilter(new ProblemContextSettings() {});
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

    filter.filter(exchange, ex -> Mono.empty()).block();

    ProblemContext context = requireNonNull(exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE));
    assertThat(context.toMap()).containsOnlyKeys("traceId");
  }
}
