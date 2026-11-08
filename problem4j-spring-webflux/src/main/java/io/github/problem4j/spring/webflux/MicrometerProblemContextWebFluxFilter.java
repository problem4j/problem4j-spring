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

import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemContextSettings;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler.TracingContext;
import java.util.Optional;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.web.server.ServerWebExchange;

/**
 * {@link ProblemContextWebFluxFilter} taking trace identifiers from the current Micrometer Tracing
 * span, so that {@code traceId} of {@code ProblemContext} is the same one that appears in logs.
 *
 * <p>The server observation of WebFlux is started before any {@code WebFilter}, but it lives in the
 * Reactor context, not in thread locals, so {@code Tracer.currentSpan()} is not available here.
 * Therefore the server span is read from the observation context stored in exchange attributes. The
 * trace identifier is resolved in the following order:
 *
 * <ol>
 *   <li>trace ID already stored in exchange attributes,
 *   <li>trace ID of the current span,
 *   <li>value of the configured tracing header,
 *   <li>newly generated trace ID.
 * </ol>
 *
 * <p>If the trace ID comes from the current span, its span ID is stored in {@code ProblemContext}
 * under {@code spanId} key as well, so that both always belong to the same trace. Spans with an
 * empty trace ID (e.g. {@code Tracer.NOOP} spans, used when no tracing bridge is present) are
 * ignored.
 *
 * <p>Requires {@code io.micrometer:micrometer-tracing} on the classpath. Registered instead of
 * {@link ProblemContextWebFluxFilter} with {@code problem4j.tracing.source=micrometer}, or with
 * {@code auto} (default) if Micrometer Tracing is on the classpath.
 *
 * @since 3.1.0
 */
public class MicrometerProblemContextWebFluxFilter extends ProblemContextWebFluxFilter {

  /**
   * Creates a new {@code MicrometerProblemContextWebFluxFilter}.
   *
   * @param settings the context settings to use
   * @since 3.1.0
   */
  public MicrometerProblemContextWebFluxFilter(ProblemContextSettings settings) {
    super(settings);
  }

  /**
   * Builds or retrieves an existing {@link ProblemContext} for the given exchange, adding {@code
   * spanId} of the current span if not present yet, and if the trace ID comes from that span.
   *
   * @param exchange the current server exchange
   * @return an existing or newly created {@link ProblemContext}
   * @since 3.1.0
   */
  @Override
  protected ProblemContext buildProblemContext(ServerWebExchange exchange) {
    ProblemContext context = super.buildProblemContext(exchange);
    if (context.get("spanId") == null) {
      findTraceContext(exchange)
          .filter(trace -> trace.traceId().equals(context.get("traceId")))
          .ifPresent(trace -> context.put("spanId", trace.spanId()));
    }
    return context;
  }

  /**
   * Initializes the trace identifier from the current span, falling back to the configured tracing
   * header or a newly generated one.
   *
   * @param exchange the current server exchange
   * @return trace identifier of the current span, or the one resolved by the superclass
   * @since 3.1.0
   */
  @Override
  protected String initTraceId(ServerWebExchange exchange) {
    return findTraceContext(exchange)
        .map(TraceContext::traceId)
        .orElseGet(() -> super.initTraceId(exchange));
  }

  /**
   * Finds the trace context of the current server span.
   *
   * @param exchange the current server exchange
   * @return trace context of the current span, or empty if no span with a trace ID is available
   * @since 3.1.0
   */
  protected Optional<TraceContext> findTraceContext(ServerWebExchange exchange) {
    return ServerRequestObservationContext.findCurrent(exchange.getAttributes())
        .map(observation -> observation.<TracingContext>get(TracingContext.class))
        .map(TracingContext::getSpan)
        .map(Span::context)
        .filter(trace -> !trace.traceId().isEmpty());
  }
}
