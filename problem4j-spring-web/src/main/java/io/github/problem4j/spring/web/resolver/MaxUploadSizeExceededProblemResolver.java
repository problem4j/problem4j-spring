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

package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.ProblemSupport.MAX_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.MAX_UPLOAD_SIZE_EXCEEDED_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
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
 */
public class MaxUploadSizeExceededProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link MaxUploadSizeExceededProblemResolver} with default problem format. */
  public MaxUploadSizeExceededProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link MaxUploadSizeExceededProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public MaxUploadSizeExceededProblemResolver(ProblemFormat problemFormat) {
    super(MaxUploadSizeExceededException.class, problemFormat);
  }

  /**
   * Builds a {@link ProblemBuilder} with status {@link HttpStatus#CONTENT_TOO_LARGE}, a formatted
   * standard detail message, and an extension entry providing the maximum allowed upload size.
   * Other parameters ({@code context}, {@code headers}, {@code status}) are ignored because the
   * semantics of {@link MaxUploadSizeExceededException} dictate the response.
   *
   * @param context problem context (unused for this resolver)
   * @param ex the triggering {@link MaxUploadSizeExceededException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; 413 enforced)
   * @return builder pre-populated with status, detail, and max size extension
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    MaxUploadSizeExceededException e = (MaxUploadSizeExceededException) ex;
    ProblemBuilder builder =
        Problem.builder()
            .status(HttpStatus.CONTENT_TOO_LARGE.value())
            .detail(formatDetail(MAX_UPLOAD_SIZE_EXCEEDED_DETAIL));
    if (e.getMaxUploadSize() > 0) {
      builder.extension(MAX_EXTENSION, e.getMaxUploadSize());
    }
    return builder;
  }
}
