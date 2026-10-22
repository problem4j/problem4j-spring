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

package io.github.problem4j.spring.web.resolver;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.ProblemResolverStore;
import java.util.Optional;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Default {@link ProblemResolutionFacade}, resolving an exception in the following order:
 *
 * <ol>
 *   <li>exception class annotated with {@code @ProblemMapping} - mapped by {@link ProblemMapper},
 *   <li>{@link ProblemResolver} registered for the exception class or its closest superclass,
 *   <li>exception class annotated with {@link ResponseStatus} - its code and non-empty reason,
 *   <li>otherwise the HTTP status determined for the error by the server (usually {@code 500
 *       Internal Server Error}).
 * </ol>
 *
 * @since 3.1.0
 */
public class DefaultProblemResolutionFacade implements ProblemResolutionFacade {

  private final ProblemMapper problemMapper;
  private final ProblemResolverStore problemResolverStore;

  /**
   * Creates a new {@link DefaultProblemResolutionFacade}.
   *
   * @param problemMapper mapper for {@code @ProblemMapping}-annotated exceptions
   * @param problemResolverStore store of registered {@link ProblemResolver}s
   * @since 3.1.0
   */
  public DefaultProblemResolutionFacade(
      ProblemMapper problemMapper, ProblemResolverStore problemResolverStore) {
    this.problemMapper = problemMapper;
    this.problemResolverStore = problemResolverStore;
  }

  /**
   * Resolves given exception into a {@link Problem}.
   *
   * @param context the current problem context
   * @param ex the exception to resolve
   * @param headers response headers, which resolvers may modify
   * @param status the HTTP status determined for the error by the server, passed to resolvers and
   *     used if nothing else resolves the exception
   * @return the resolved {@link Problem}
   * @since 3.1.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    if (problemMapper.isMappingCandidate(ex)) {
      return problemMapper.toProblemBuilder(ex, context).build();
    }
    Optional<ProblemResolver> optionalResolver = problemResolverStore.findResolver(ex.getClass());
    if (optionalResolver.isPresent()) {
      return optionalResolver.get().resolve(context, ex, headers, status);
    }
    ResponseStatus responseStatus =
        AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
    if (responseStatus != null) {
      ProblemBuilder builder = Problem.builder().status(responseStatus.code().value());
      if (StringUtils.hasLength(responseStatus.reason())) {
        builder = builder.detail(responseStatus.reason());
      }
      return builder.build();
    }
    return Problem.of(status.value());
  }
}
