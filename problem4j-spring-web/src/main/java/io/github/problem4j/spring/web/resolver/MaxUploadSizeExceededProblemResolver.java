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

import static io.github.problem4j.spring.web.parameter.ViolationSupport.MAX_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MAX_UPLOAD_SIZE_EXCEEDED_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Handles {@link MaxUploadSizeExceededException} thrown when a file upload exceeds the configured
 * maximum size limit.
 *
 * <p>This occurs during multipart/form-data requests if the uploaded file is larger than the limit
 * set in the server or Spring configuration (e.g., {@code spring.servlet.multipart.max-file-size}).
 *
 * <p>The handler is responsible for returning an appropriate HTTP 413 (Payload Too Large) response
 * to inform the client that the uploaded file exceeds the allowed size.
 *
 * @since 1.2.0
 */
public class MaxUploadSizeExceededProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link MaxUploadSizeExceededProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public MaxUploadSizeExceededProblemResolver() {
    super(MaxUploadSizeExceededException.class);
  }

  /**
   * Creates a new {@link MaxUploadSizeExceededProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link
   *     io.github.problem4j.spring.web.config.DefaultProblemBeanPostProcessor
   *     ProblemBeanPostProcessor} now assigns the {@link ProblemFormat} after construction; use
   *     {@link #MaxUploadSizeExceededProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public MaxUploadSizeExceededProblemResolver(ProblemFormat problemFormat) {
    super(MaxUploadSizeExceededException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} with status {@link HttpStatus#CONTENT_TOO_LARGE}, a formatted
   * standard detail message, and an extension entry providing the maximum allowed upload size.
   * Other parameters ({@code context}, {@code headers}, {@code status}) are ignored because the
   * semantics of {@link MaxUploadSizeExceededException} dictate the response.
   *
   * @param context problem context (unused for this resolver)
   * @param ex the triggering {@link MaxUploadSizeExceededException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 413 enforced)
   * @return problem with status, detail, and max size extension
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    MaxUploadSizeExceededException e = (MaxUploadSizeExceededException) ex;
    ProblemBuilder builder =
        Problem.builder()
            .status(HttpStatus.CONTENT_TOO_LARGE.value())
            .detail(formatDetail(MAX_UPLOAD_SIZE_EXCEEDED_DETAIL));
    if (e.getMaxUploadSize() > 0) {
      builder = builder.extension(MAX_EXTENSION, e.getMaxUploadSize());
    }
    return builder.build();
  }
}
