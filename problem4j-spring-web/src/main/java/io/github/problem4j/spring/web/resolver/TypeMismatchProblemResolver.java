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

import static io.github.problem4j.spring.web.ProblemSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.PROPERTY_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.TYPE_MISMATCH_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import java.util.Locale;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Handles {@link TypeMismatchException} thrown when a request parameter, path variable, or property
 * cannot be converted to the required type.
 *
 * <p>This typically occurs when the client sends a value that cannot be converted to the expected
 * Java type, for example passing a string where an integer is required.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response to
 * indicate that the provided input has an invalid type.
 */
public class TypeMismatchProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link TypeMismatchProblemResolver} with default problem format. */
  public TypeMismatchProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link TypeMismatchProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public TypeMismatchProblemResolver(ProblemFormat problemFormat) {
    super(TypeMismatchException.class, problemFormat);
  }

  /**
   * Resolves a {@link TypeMismatchException} (also {@link MethodArgumentTypeMismatchException})
   * into a {@link ProblemBuilder} with status {@link HttpStatus#BAD_REQUEST}, a standardized detail
   * ({@code ProblemSupport#TYPE_MISMATCH_DETAIL}), and optional extensions:
   *
   * <ul>
   *   <li>{@code property} ({@code ProblemSupport#PROPERTY_EXTENSION}) - name of the parameter /
   *       property that failed conversion
   *   <li>{@code kind} ({@code ProblemSupport#KIND_EXTENSION}) - required target type in lowercase
   *       simple form
   * </ul>
   *
   * <p>Older Spring versions may not populate {@code propertyName} for {@link
   * MethodArgumentTypeMismatchException}; in that case this resolver falls back to {@link
   * MethodArgumentTypeMismatchException#getName()}.
   *
   * @param context problem context (unused)
   * @param ex the triggering type mismatch exception
   * @param headers HTTP headers (unused)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return builder populated with status, detail and relevant extensions
   * @see io.github.problem4j.spring.web.ProblemSupport
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ProblemBuilder builder =
        Problem.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(formatDetail(TYPE_MISMATCH_DETAIL));

    TypeMismatchException ex1 = (TypeMismatchException) ex;

    String property = ex1.getPropertyName();
    String kind =
        ex1.getRequiredType() != null
            ? ex1.getRequiredType().getSimpleName().toLowerCase(Locale.ROOT)
            : null;

    // could happen in some early 3.0.x versions of Spring Boot, cannot add tests for it as newer
    // versions assign it to propertyName in constructor
    if (property == null && ex instanceof MethodArgumentTypeMismatchException ex2) {
      property = ex2.getName();
    }

    if (property != null) {
      builder = builder.extension(PROPERTY_EXTENSION, property);
    }
    if (kind != null) {
      builder = builder.extension(KIND_EXTENSION, kind);
    }
    return builder;
  }
}
