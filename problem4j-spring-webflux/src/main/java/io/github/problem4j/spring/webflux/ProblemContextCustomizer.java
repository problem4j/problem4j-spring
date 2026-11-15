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
import java.util.function.BiConsumer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Hook for modifying {@link ProblemContext} after {@link ProblemContextWebFilter} has built it,
 * e.g. to add a custom request identifier. All beans are invoked in their order and may overwrite
 * any entry, including {@code traceId}.
 *
 * @since 3.1.0
 */
@FunctionalInterface
public interface ProblemContextCustomizer {

  /**
   * Customizes the context of the current exchange.
   *
   * @param context the {@link ProblemContext} to customize
   * @param exchange the current {@link ServerWebExchange}
   * @return a {@link Mono} that completes when the context has been customized
   * @since 3.1.0
   */
  Mono<Void> customize(ProblemContext context, ServerWebExchange exchange);

  /**
   * Creates a {@code ProblemContextCustomizer} from a synchronous function, executed on
   * subscription.
   *
   * @param customizer the function customizing the context
   * @return a new {@code ProblemContextCustomizer}
   * @since 3.1.0
   */
  static ProblemContextCustomizer sync(BiConsumer<ProblemContext, ServerWebExchange> customizer) {
    return (context, exchange) -> Mono.fromRunnable(() -> customizer.accept(context, exchange));
  }
}
