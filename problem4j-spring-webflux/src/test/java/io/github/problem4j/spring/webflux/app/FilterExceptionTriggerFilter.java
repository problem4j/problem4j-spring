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

package io.github.problem4j.spring.webflux.app;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.webflux.app.problem.ResolvableException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/** Fails requests from a web filter with default order, before any handler is invoked. */
@Component
public class FilterExceptionTriggerFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return switch (exchange.getRequest().getPath().value()) {
      case "/filter-error/resolvable" -> Mono.error(new ResolvableException());
      case "/filter-error/problem" ->
          Mono.error(new ProblemException(Problem.of(409, "from filter")));
      case "/filter-error/unknown" -> Mono.error(new IllegalStateException("from filter"));
      default -> chain.filter(exchange);
    };
  }
}
