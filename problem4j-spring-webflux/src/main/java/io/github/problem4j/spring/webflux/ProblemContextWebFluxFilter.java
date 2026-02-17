/*
 * Copyright (c) 2025-2026 The Problem4J Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;

import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemContextSettings;
import java.util.Optional;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * {@link WebFilter} that ensures each request processed by a WebFlux application has an associated
 * context and trace identifier.
 *
 * <p>The filter reads the trace ID from a configured HTTP header, generates one if missing, and
 * stores it in the {@link ServerWebExchange} attributes, response headers, and Reactor context for
 * downstream access.
 */
public class ProblemContextWebFluxFilter implements WebFilter {

  private final ProblemContextSettings settings;

  /**
   * Constructs a new {@link ProblemContextWebFluxFilter}.
   *
   * @param settings the context settings to use
   */
  public ProblemContextWebFluxFilter(ProblemContextSettings settings) {
    this.settings = settings;
  }

  /**
   * Applies the filter logic.
   *
   * <p>Ensures that a trace ID is available for the request, stores it in the exchange attributes,
   * adds it to the response headers (if configured), and propagates it through the Reactor context
   * for reactive downstream components.
   *
   * @param exchange the current server exchange
   * @param chain the web filter chain to continue processing
   * @return a {@link Mono} that completes when request processing is finished
   */
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ProblemContext context = buildProblemContext(exchange);

    assignContextAttributes(exchange, context);
    modifyServerExchange(exchange, context);

    return chain.filter(exchange).contextWrite(ctx -> contextWrite(ctx, exchange, context));
  }

  /**
   * Builds or retrieves an existing {@link ProblemContext} for the given request.
   *
   * <p>If the exchange already contains a {@link ProblemContext} attribute, that instance is
   * reused. Otherwise, a new one is created using {@link #findTraceId} and {@link #initTraceId}.
   *
   * @param exchange the current server exchange
   * @return an existing or newly created {@link ProblemContext}
   */
  protected ProblemContext buildProblemContext(ServerWebExchange exchange) {
    return exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) instanceof ProblemContext attribute
        ? attribute
        : ProblemContext.create()
            .put("traceId", findTraceId(exchange).orElseGet(() -> initTraceId(exchange)));
  }

  /**
   * Attempts to locate an existing trace ID in the exchange attributes.
   *
   * @param exchange the current server exchange
   * @return an {@link Optional} containing the trace ID if present
   */
  protected Optional<String> findTraceId(ServerWebExchange exchange) {
    return Optional.ofNullable(exchange.getAttribute(TRACE_ID_ATTRIBUTE)).map(Object::toString);
  }

  /**
   * Initializes a new trace ID for the request if one cannot be found.
   *
   * <p>If a tracing header name is configured in {@link ProblemContextSettings}, the header value
   * is used if present. Otherwise, a new trace ID is generated using {@link #createNewTraceId}.
   *
   * @param exchange the current server exchange
   * @return the existing or newly generated trace ID
   */
  protected String initTraceId(ServerWebExchange exchange) {
    if (!StringUtils.hasLength(getSettings().getTracingHeaderName())) {
      return createNewTraceId(exchange);
    }
    String traceId =
        exchange.getRequest().getHeaders().getFirst(getSettings().getTracingHeaderName());
    return StringUtils.hasLength(traceId) ? traceId : createNewTraceId(exchange);
  }

  /**
   * Generates a new trace identifier.
   *
   * <p>Subclasses may override this method to customize the trace ID generation logic. By default,
   * it delegates to {@code getRandomTraceId()}.
   *
   * @param exchange the current server exchange
   * @return a newly generated trace ID
   */
  protected String createNewTraceId(ServerWebExchange exchange) {
    return "urn:uuid:" + UUID.randomUUID();
  }

  /**
   * Assigns the context and trace ID attributes to the exchange.
   *
   * @param exchange the current server exchange
   * @param context the problem context to assign
   */
  protected void assignContextAttributes(ServerWebExchange exchange, ProblemContext context) {
    exchange.getAttributes().put(PROBLEM_CONTEXT_ATTRIBUTE, context);
    exchange.getAttributes().put(TRACE_ID_ATTRIBUTE, context.get("traceId"));
  }

  /**
   * Adds the trace ID as an HTTP response header if tracing is enabled.
   *
   * @param exchange the current server exchange
   * @param context the current {@link ProblemContext}
   */
  protected void modifyServerExchange(ServerWebExchange exchange, ProblemContext context) {
    assignTracingHeader(exchange, context);
  }

  /**
   * Adds the trace identifier as an HTTP response header if tracing is enabled.
   *
   * @param exchange the current server exchange
   * @param context the current {@link ProblemContext}
   */
  protected void assignTracingHeader(ServerWebExchange exchange, ProblemContext context) {
    if (StringUtils.hasLength(getSettings().getTracingHeaderName())) {
      exchange
          .getResponse()
          .getHeaders()
          .set(getSettings().getTracingHeaderName(), context.get("traceId"));
    }
  }

  /**
   * Enriches the given Reactor {@link Context} with problem-handling metadata.
   *
   * @param ctx the current Reactor {@link Context}
   * @param exchange the active {@link ServerWebExchange} for the request
   * @param context the {@link ProblemContext} containing problem details and trace information
   * @return an updated {@link Context} containing the problem context and trace ID
   */
  protected Context contextWrite(Context ctx, ServerWebExchange exchange, ProblemContext context) {
    ctx = ctx.put(PROBLEM_CONTEXT_ATTRIBUTE, context);
    String traceId = context.get("traceId");
    if (StringUtils.hasLength(traceId)) {
      ctx = ctx.put(TRACE_ID_ATTRIBUTE, traceId);
    }
    return ctx;
  }

  /**
   * Returns the active {@link ProblemContextSettings}.
   *
   * @return the current settings
   */
  protected ProblemContextSettings getSettings() {
    return settings;
  }
}
