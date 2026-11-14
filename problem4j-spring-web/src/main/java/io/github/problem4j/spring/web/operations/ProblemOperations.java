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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

/**
 * Resolves exceptions into {@link Problem}s and applies post-processing to {@link Problem}s and
 * Spring's {@link ProblemDetail}s.
 *
 * <p>The two steps are available separately, as {@link #resolve} and {@link #postProcess}, and
 * combined, as {@link #translate}.
 *
 * @see ProblemTemplate
 * @since 3.1.0
 */
public interface ProblemOperations {

  /**
   * Checks whether the exception is known to Problem4J. Other exceptions are left to the default
   * error handling of the framework, unless rendering of unknown exceptions is enabled. Unknown
   * exceptions can still be resolved, into a {@link Problem} of {@code 500 Internal Server Error}.
   *
   * @param ex the exception to check
   * @return {@code true} if the exception is known to Problem4J
   * @since 3.1.0
   */
  boolean supports(Exception ex);

  /**
   * Resolves an exception into a {@link Problem}, without post-processing it.
   *
   * @param context the context of the current request
   * @param ex the exception to resolve
   * @param headers the response headers, modifiable
   * @return the resolved {@link Problem}
   * @since 3.1.0
   */
  Problem resolve(ProblemContext context, Exception ex, HttpHeaders headers);

  /**
   * Resolves an exception into a {@link Problem} with the given status, without post-processing it.
   * The status is passed to resolvers, and used if nothing else resolves the exception. Unlike
   * {@link #resolve(ProblemContext, Exception, HttpHeaders)}, the status and headers of Spring's
   * {@code ErrorResponse} are not taken from the exception, as they are expected to be given.
   *
   * @param context the context of the current request
   * @param ex the exception to resolve
   * @param headers the response headers, modifiable
   * @param status the status of the response, chosen by the caller
   * @return the resolved {@link Problem}
   * @since 3.1.0
   */
  Problem resolve(ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status);

  /**
   * Applies post-processing to a {@link Problem}.
   *
   * @param context the context of the current request, if available
   * @param problem the problem to post-process
   * @return the post-processed {@link Problem}
   * @since 3.1.0
   */
  Problem postProcess(@Nullable ProblemContext context, Problem problem);

  /**
   * Applies post-processing to a {@link ProblemDetail}, keeping it a {@link ProblemDetail}.
   *
   * @param context the context of the current request, if available
   * @param detail the problem detail to post-process
   * @return the post-processed {@link ProblemDetail}
   * @since 3.1.0
   */
  ProblemDetail postProcess(@Nullable ProblemContext context, ProblemDetail detail);

  /**
   * Translates an exception into a {@link Problem}, by {@link #resolve resolving} and {@link
   * #postProcess post-processing} it.
   *
   * @param context the context of the current request
   * @param ex the exception to translate
   * @param headers the response headers, modifiable
   * @return the resolved and post-processed {@link Problem}
   * @since 3.1.0
   */
  default Problem translate(ProblemContext context, Exception ex, HttpHeaders headers) {
    return postProcess(context, resolve(context, ex, headers));
  }
}
