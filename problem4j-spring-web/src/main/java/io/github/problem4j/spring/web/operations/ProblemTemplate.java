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

package io.github.problem4j.spring.web.operations;

import io.github.problem4j.core.DefaultProblemMapper;
import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Default implementation of {@link ProblemOperations}.
 *
 * <p>Exceptions are resolved in the following order:
 *
 * <ol>
 *   <li>{@link ProblemException} - its {@link Problem} is used as is,
 *   <li>{@code @ProblemMapping} annotated exceptions - mapped with {@link ProblemMapper},
 *   <li>{@link ProblemResolver} registered in {@link ProblemResolverStore},
 *   <li>{@link ResponseStatus} annotated exceptions,
 *   <li>{@link ErrorResponse} status, or {@code 500 Internal Server Error} otherwise.
 * </ol>
 *
 * @since 3.1.0
 */
public class ProblemTemplate implements ProblemOperations {

  private final ProblemMapper problemMapper;
  private final ProblemResolverStore problemResolverStore;
  private final ProblemPostProcessor problemPostProcessor;

  /**
   * Creates a new {@code ProblemTemplate} from the given builder.
   *
   * @param builder the builder holding collaborators
   * @since 3.1.0
   */
  protected ProblemTemplate(Builder builder) {
    this.problemMapper = builder.problemMapper;
    this.problemResolverStore = builder.problemResolverStore;
    this.problemPostProcessor = builder.problemPostProcessor;
  }

  /**
   * Creates a new {@link Builder} for {@code ProblemTemplate}. Collaborators not set explicitly
   * default to {@link DefaultProblemMapper}, a {@link DefaultProblemResolverStore} without
   * resolvers and {@link ProblemPostProcessor#identity()}, so that new collaborators can be added
   * in future versions without breaking existing code.
   *
   * @return a new {@link Builder}
   * @since 3.1.0
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Checks whether the exception is known to Problem4J, i.e. it is a {@link ProblemException}, it
   * is a {@code @ProblemMapping} candidate, a {@link ProblemResolver} is registered for it, it is
   * one of Spring's {@link ErrorResponse} exceptions, or it is annotated with {@link
   * ResponseStatus}.
   *
   * @param ex the exception to check
   * @return {@code true} if the exception is known to Problem4J
   * @since 3.1.0
   */
  @Override
  public boolean supports(Exception ex) {
    return ex instanceof ProblemException
        || problemMapper.isMappingCandidate(ex)
        || problemResolverStore.findResolver(ex.getClass()).isPresent()
        || ex instanceof ErrorResponse
        || AnnotatedElementUtils.hasAnnotation(ex.getClass(), ResponseStatus.class);
  }

  /**
   * Resolves an exception into a {@link Problem}, without post-processing it. Headers of {@link
   * ErrorResponse} exceptions are copied into {@code headers}, and resolvers may add their own.
   *
   * @param context the context of the current request
   * @param ex the exception to resolve
   * @param headers the response headers, modifiable
   * @return the resolved {@link Problem}
   * @since 3.1.0
   */
  @Override
  public Problem resolve(ProblemContext context, Exception ex, HttpHeaders headers) {
    HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      status = errorResponse.getStatusCode();
      headers.putAll(errorResponse.getHeaders());
    }
    return resolve(context, ex, headers, status);
  }

  /**
   * Applies post-processing to a {@link Problem}, with the configured {@link ProblemPostProcessor}.
   *
   * @param context the context of the current request, if available
   * @param problem the problem to post-process
   * @return the post-processed {@link Problem}
   * @since 3.1.0
   */
  @Override
  public Problem postProcess(@Nullable ProblemContext context, Problem problem) {
    return problemPostProcessor.process(context, problem);
  }

  /**
   * Applies post-processing to a {@link ProblemDetail}. It is converted into a {@link Problem},
   * post-processed, and the result is written back into the same {@link ProblemDetail} instance, so
   * its type (including subclasses) is retained. Status is not written back, as it was already used
   * for the response.
   *
   * @param context the context of the current request, if available
   * @param detail the problem detail to post-process, modified in place
   * @return the same {@link ProblemDetail} instance
   * @since 3.1.0
   */
  @Override
  public ProblemDetail postProcess(@Nullable ProblemContext context, ProblemDetail detail) {
    Problem problem = postProcess(context, fromProblemDetail(detail));
    applyTo(problem, detail);
    return detail;
  }

  /**
   * Resolves an exception into a {@link Problem} of the given fallback status, without
   * post-processing it.
   *
   * @param context the context of the current request
   * @param ex the exception to resolve
   * @param headers the response headers, modifiable
   * @param status the status implied by the exception, used if nothing else resolves it
   * @return the resolved {@link Problem}
   * @since 3.1.0
   */
  protected Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    if (ex instanceof ProblemException problemException) {
      return problemException.getProblem();
    }
    if (problemMapper.isMappingCandidate(ex)) {
      return problemMapper.toProblemBuilder(ex, context).build();
    }
    Optional<ProblemResolver> resolver = problemResolverStore.findResolver(ex.getClass());
    if (resolver.isPresent()) {
      return resolver.get().resolve(context, ex, headers, status);
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

  /**
   * Converts a {@link ProblemDetail} into an equivalent {@link Problem}, with its properties as
   * extensions.
   *
   * @param detail the problem detail to convert
   * @return the equivalent {@link Problem}
   * @since 3.1.0
   */
  protected Problem fromProblemDetail(ProblemDetail detail) {
    ProblemBuilder builder =
        Problem.builder()
            .type(detail.getType())
            .title(detail.getTitle())
            .status(detail.getStatus())
            .detail(detail.getDetail())
            .instance(detail.getInstance());
    Map<String, Object> properties = detail.getProperties();
    if (properties != null) {
      for (Map.Entry<String, Object> property : properties.entrySet()) {
        builder = builder.extension(property.getKey(), property.getValue());
      }
    }
    return builder.build();
  }

  /**
   * Writes members of a post-processed {@link Problem} back into a {@link ProblemDetail}, with its
   * extensions as properties. Status is not written back. A blank type ({@code about:blank}) is
   * written back as {@code null}, which is equivalent according to RFC 9457, so that it is omitted
   * from the response as by Spring itself.
   *
   * @param problem the post-processed problem
   * @param detail the problem detail to modify
   * @since 3.1.0
   */
  protected void applyTo(Problem problem, ProblemDetail detail) {
    detail.setType(problem.isTypeNonBlank() ? problem.getType() : null);
    detail.setTitle(problem.getTitle());
    detail.setDetail(problem.getDetail());
    detail.setInstance(problem.getInstance());
    Map<String, Object> extensions = problem.getExtensions();
    detail.setProperties(
        extensions.isEmpty() && detail.getProperties() == null
            ? null
            : new LinkedHashMap<>(extensions));
  }

  /**
   * Builder for {@link ProblemTemplate}.
   *
   * @since 3.1.0
   */
  public static final class Builder {

    private ProblemMapper problemMapper = new DefaultProblemMapper();
    private ProblemResolverStore problemResolverStore = new DefaultProblemResolverStore(List.of());
    private ProblemPostProcessor problemPostProcessor = ProblemPostProcessor.identity();

    private Builder() {}

    /**
     * Sets the mapper of {@code @ProblemMapping} annotated exceptions.
     *
     * @param problemMapper the problem mapper
     * @return this builder
     * @since 3.1.0
     */
    public Builder problemMapper(ProblemMapper problemMapper) {
      this.problemMapper = problemMapper;
      return this;
    }

    /**
     * Sets the store of registered {@link ProblemResolver}s.
     *
     * @param problemResolverStore the problem resolver store
     * @return this builder
     * @since 3.1.0
     */
    public Builder problemResolverStore(ProblemResolverStore problemResolverStore) {
      this.problemResolverStore = problemResolverStore;
      return this;
    }

    /**
     * Sets the post-processor applied to every resulting {@link Problem}.
     *
     * @param problemPostProcessor the problem post-processor
     * @return this builder
     * @since 3.1.0
     */
    public Builder problemPostProcessor(ProblemPostProcessor problemPostProcessor) {
      this.problemPostProcessor = problemPostProcessor;
      return this;
    }

    /**
     * Builds a new {@link ProblemTemplate}.
     *
     * @return a new {@link ProblemTemplate}
     * @since 3.1.0
     */
    public ProblemTemplate build() {
      return new ProblemTemplate(this);
    }
  }
}
