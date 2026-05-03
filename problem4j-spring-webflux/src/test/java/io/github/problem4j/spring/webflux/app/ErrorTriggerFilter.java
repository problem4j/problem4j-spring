/*
 * Copyright 2025-2026 The Problem4J Authors
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

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;

import io.github.problem4j.core.ProblemContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Test-only filter that throws an unhandled exception for designated paths, triggering {@code
 * ProblemErrorWebExceptionHandler} instead of controller advice.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorTriggerFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    if ("/error-handler/no-context".equals(path)) {
      return Mono.error(new RuntimeException("unhandled error, no context"));
    }

    if ("/error-handler/with-context".equals(path)) {
      exchange.getAttributes().put(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create());
      return Mono.error(new RuntimeException("unhandled error, context present"));
    }

    return chain.filter(exchange);
  }
}
