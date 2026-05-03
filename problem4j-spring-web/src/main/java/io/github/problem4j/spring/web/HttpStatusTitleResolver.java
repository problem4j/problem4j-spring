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

package io.github.problem4j.spring.web;

import io.github.problem4j.core.StatusTitleResolver;
import java.util.Optional;
import org.springframework.http.HttpStatus;

/**
 * A {@link StatusTitleResolver} implementation, based on {@link HttpStatus} from Spring Framework.
 *
 * @since 3.0.0
 */
public class HttpStatusTitleResolver implements StatusTitleResolver {

  /**
   * The priority of this resolver. Resolvers with lower priority values will be preferred over
   * those with higher values.
   *
   * @since 3.0.0
   */
  public static final int PRIORITY = Integer.MAX_VALUE - 1000;

  /**
   * Creates a new {@link HttpStatusTitleResolver}.
   *
   * @since 3.0.0
   */
  public HttpStatusTitleResolver() {}

  /**
   * Resolves the title for a given status code. If the resolver does not have a title for the
   * status, it should return an empty {@code Optional}.
   *
   * @param status the HTTP status code for which to resolve the title
   * @return an {@code Optional} containing the resolved title, or empty if no title is available
   *     for the status
   * @since 3.0.0
   */
  @Override
  public Optional<String> resolve(int status) {
    return Optional.ofNullable(HttpStatus.resolve(status)).map(HttpStatus::getReasonPhrase);
  }

  /**
   * Returns the priority of this resolver. Resolvers with lower priority values will be preferred
   * over those with higher values. The implementation returns {@code Integer.MAX_VALUE - 1000},
   * which means that all resolvers will have the same priority unless overridden.
   *
   * @return the priority of this resolver
   * @since 3.0.0
   */
  @Override
  public int getPriority() {
    return PRIORITY;
  }
}
