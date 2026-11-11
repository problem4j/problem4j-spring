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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import java.util.function.Predicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.ServerHttpObservationFilter;

/**
 * {@link AdviceWebMvcInspector} recording exceptions handled by Problem4J as errors of the current
 * HTTP server observation.
 *
 * <p>{@link ServerHttpObservationFilter} records only exceptions that are not handled by the web
 * framework. As Problem4J handles exceptions in controller advices, without this inspector {@code
 * http.server.requests} metrics and spans report {@code exception=none} even for 5xx responses. The
 * exception is set on the {@link ServerRequestObservationContext} stored in request attributes, as
 * recommended by Spring Framework for handled exceptions. An error already recorded in the
 * observation context is never replaced.
 *
 * <p>Registered automatically with {@code problem4j.observation-errors} property deciding which
 * response statuses are recorded ({@code server} by default).
 *
 * @since 3.1.0
 */
public class ObservationErrorWebMvcInspector implements AdviceWebMvcInspector {

  private final Predicate<HttpStatusCode> statusFilter;

  /**
   * Creates a new {@code ObservationErrorWebMvcInspector} recording exceptions resulting in 5xx
   * responses.
   *
   * @since 3.1.0
   */
  public ObservationErrorWebMvcInspector() {
    this(HttpStatusCode::is5xxServerError);
  }

  /**
   * Creates a new {@code ObservationErrorWebMvcInspector} recording exceptions resulting in
   * responses with status matching given predicate.
   *
   * @param statusFilter decides whether an exception resulting in given response status is recorded
   * @since 3.1.0
   */
  public ObservationErrorWebMvcInspector(Predicate<HttpStatusCode> statusFilter) {
    this.statusFilter = statusFilter;
  }

  /**
   * Records {@code ex} as an error of the current HTTP server observation, if {@code status}
   * matches the configured predicate and no error is recorded yet.
   *
   * @param context the {@link ProblemContext} of the current request
   * @param problem the {@link Problem} response body
   * @param ex the handled exception
   * @param headers the HTTP headers of the response
   * @param status the HTTP status code of the response
   * @param request the current request
   * @since 3.1.0
   */
  @Override
  public void inspect(
      ProblemContext context,
      Problem problem,
      Exception ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    if (!statusFilter.test(status)) {
      return;
    }
    Object attribute =
        request.getAttribute(
            ServerHttpObservationFilter.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
            RequestAttributes.SCOPE_REQUEST);
    if (attribute instanceof ServerRequestObservationContext observation
        && observation.getError() == null) {
      observation.setError(ex);
    }
  }
}
