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

package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;

import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemContextSettings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@link OncePerRequestFilter} that ensures each request processed by a Web MVC application has an
 * associated context and trace identifier.
 *
 * <p>The filter reads the trace identifier from a configured HTTP header, generates one if missing,
 * and stores it in the {@link HttpServletRequest} attributes, response headers for downstream
 * access.
 */
public class ProblemContextWebMvcFilter extends OncePerRequestFilter {

  private final ProblemContextSettings settings;

  /**
   * Creates a new {@code ProblemContextWebMvcFilter}.
   *
   * @param settings the context settings to use
   */
  public ProblemContextWebMvcFilter(ProblemContextSettings settings) {
    this.settings = settings;
  }

  /**
   * Applies the filter logic.
   *
   * <p>Ensures that a trace identifier is available for the request, stores it in the exchange
   * attributes, adds it to the response headers (if configured), and propagates it through.
   *
   * @param request the current server request
   * @param response the current server response
   * @param filterChain the filter chain to continue processing
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    ProblemContext context = buildProblemContext(request, response);

    assignContextAttributes(request, response, context);
    modifyServletExchange(request, response, context);

    filterChain.doFilter(request, response);
  }

  /**
   * Builds or retrieves an existing {@link ProblemContext} for the given request.
   *
   * <p>If the exchange already contains a {@link ProblemContext} attribute, that instance is
   * reused. Otherwise, a new one is created using {@link #findTraceId} and {@link #initTraceId}.
   *
   * @param request the current server request
   * @param response the current server response
   * @return an existing or newly created {@link ProblemContext}
   */
  protected ProblemContext buildProblemContext(
      HttpServletRequest request, HttpServletResponse response) {
    return request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) instanceof ProblemContext attribute
        ? attribute
        : ProblemContext.create()
            .put(
                "traceId",
                findTraceId(request, response).orElseGet(() -> initTraceId(request, response)));
  }

  /**
   * Attempts to locate an existing trace identifier in the exchange attributes.
   *
   * @param request the current server request
   * @param response the current server response
   * @return an {@link Optional} containing the trace identifier if present
   */
  protected Optional<String> findTraceId(HttpServletRequest request, HttpServletResponse response) {
    return Optional.ofNullable(request.getAttribute(TRACE_ID_ATTRIBUTE)).map(Object::toString);
  }

  /**
   * Initializes a new trace identifier for the request if one cannot be found.
   *
   * <p>If a tracing header name is configured in {@link ProblemContextSettings}, the header value
   * is used if present. Otherwise, a new one is generated using {@link #createNewTraceId}.
   *
   * @param request the current server request
   * @param response the current server response
   * @return existing or newly generated trace identifier
   */
  protected String initTraceId(HttpServletRequest request, HttpServletResponse response) {
    if (!StringUtils.hasLength(getSettings().getTracingHeaderName())) {
      return createNewTraceId(request, response);
    }
    String traceId = request.getHeader(getSettings().getTracingHeaderName());
    return StringUtils.hasLength(traceId) ? traceId : createNewTraceId(request, response);
  }

  /**
   * Generates a new trace identifier.
   *
   * <p>Subclasses may override this method to customize the trace identifier generation logic. By
   * default, it delegates to {@code getRandomTraceId()}.
   *
   * @param request the current server request
   * @param response the current server response
   * @return a newly generated trace identifier
   */
  protected String createNewTraceId(HttpServletRequest request, HttpServletResponse response) {
    return "urn:uuid:" + UUID.randomUUID();
  }

  /**
   * Assigns {@code PROBLEM_CONTEXT} and {@code TRACE_ID} attributes to request attributes.
   *
   * @param request the current server request
   * @param response the current server response
   * @param context the current {@link ProblemContext}
   */
  protected void assignContextAttributes(
      HttpServletRequest request, HttpServletResponse response, ProblemContext context) {
    request.setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, context);
    request.setAttribute(TRACE_ID_ATTRIBUTE, context.get("traceId"));
  }

  /**
   * Modifies request and response before passing it through the filter chain.
   *
   * @param request the current server request
   * @param response the current server response
   * @param context the current {@link ProblemContext}
   */
  protected void modifyServletExchange(
      HttpServletRequest request, HttpServletResponse response, ProblemContext context) {
    assignTracingHeader(request, response, context);
  }

  /**
   * Adds the trace identifier as an HTTP response header if tracing is enabled.
   *
   * @param request the current server request
   * @param response the current server response
   * @param context the current {@link ProblemContext}
   */
  protected void assignTracingHeader(
      HttpServletRequest request, HttpServletResponse response, ProblemContext context) {
    if (StringUtils.hasLength(getSettings().getTracingHeaderName())) {
      response.setHeader(getSettings().getTracingHeaderName(), context.get("traceId"));
    }
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
