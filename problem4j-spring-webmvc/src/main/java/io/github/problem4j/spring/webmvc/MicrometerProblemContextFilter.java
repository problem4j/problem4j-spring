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

import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemContextSettings;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler.TracingContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.web.filter.ServerHttpObservationFilter;

/**
 * {@link ProblemContextFilter} taking trace identifiers from the current Micrometer Tracing span,
 * so that {@code traceId} of {@code ProblemContext} is the same one that appears in logs.
 *
 * <p>The server span is read from the observation context that {@link ServerHttpObservationFilter}
 * stores in request attributes. The trace identifier is resolved in the following order:
 *
 * <ol>
 *   <li>trace ID already stored in request attributes,
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
 * {@link ProblemContextFilter} with {@code problem4j.tracing-source=micrometer}, or with {@code
 * auto} (default) if Micrometer Tracing is on the classpath.
 *
 * @since 3.1.0
 */
public class MicrometerProblemContextFilter extends ProblemContextFilter {

  /**
   * Creates a new {@code MicrometerProblemContextFilter}.
   *
   * @param settings the context settings to use
   * @since 3.1.0
   */
  public MicrometerProblemContextFilter(ProblemContextSettings settings) {
    super(settings);
  }

  /**
   * Builds or retrieves an existing {@link ProblemContext} for the given request, adding {@code
   * spanId} of the current span if not present yet, and if the trace ID comes from that span.
   *
   * @param request the current server request
   * @param response the current server response
   * @return an existing or newly created {@link ProblemContext}
   * @since 3.1.0
   */
  @Override
  protected ProblemContext buildProblemContext(
      HttpServletRequest request, HttpServletResponse response) {
    ProblemContext context = super.buildProblemContext(request, response);
    if (context.get("spanId") == null) {
      findTraceContext(request)
          .filter(trace -> trace.traceId().equals(context.get("traceId")))
          .ifPresent(trace -> context.put("spanId", trace.spanId()));
    }
    return context;
  }

  /**
   * Initializes the trace identifier from the current span, falling back to the configured tracing
   * header or a newly generated one.
   *
   * @param request the current server request
   * @param response the current server response
   * @return trace identifier of the current span, or the one resolved by the superclass
   * @since 3.1.0
   */
  @Override
  protected String initTraceId(HttpServletRequest request, HttpServletResponse response) {
    return findTraceContext(request)
        .map(TraceContext::traceId)
        .orElseGet(() -> super.initTraceId(request, response));
  }

  /**
   * Finds the trace context of the current server span.
   *
   * @param request the current server request
   * @return trace context of the current span, or empty if no span with a trace ID is available
   * @since 3.1.0
   */
  protected Optional<TraceContext> findTraceContext(HttpServletRequest request) {
    return ServerHttpObservationFilter.findObservationContext(request)
        .map(observation -> observation.<TracingContext>get(TracingContext.class))
        .map(TracingContext::getSpan)
        .map(Span::context)
        .filter(trace -> !trace.traceId().isEmpty());
  }
}
