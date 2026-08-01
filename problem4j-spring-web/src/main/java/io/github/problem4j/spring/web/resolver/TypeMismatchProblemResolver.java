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

import static io.github.problem4j.spring.web.parameter.ViolationSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.PROPERTY_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.TYPE_MISMATCH_DETAIL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapper;
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
 *
 * @since 1.2.0
 */
public class TypeMismatchProblemResolver extends AbstractProblemResolver {

  private final TypeNameMapper typeNameMapper;

  /**
   * Creates a new {@link TypeMismatchProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public TypeMismatchProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link TypeMismatchProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public TypeMismatchProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new SimpleTypeNameMapper());
  }

  /**
   * Creates a new {@link TypeMismatchProblemResolver} with the specified problem format and type
   * name mapper.
   *
   * @param problemFormat the problem format to use
   * @param typeNameMapper the type name mapper to use
   * @since 1.2.0
   */
  public TypeMismatchProblemResolver(ProblemFormat problemFormat, TypeNameMapper typeNameMapper) {
    super(TypeMismatchException.class, problemFormat);
    this.typeNameMapper = typeNameMapper;
  }

  /**
   * Resolves a {@link TypeMismatchException} (also {@link MethodArgumentTypeMismatchException})
   * into an immutable {@link Problem} with status {@link HttpStatus#BAD_REQUEST}, a standardized
   * detail ({@code ViolationSupport#TYPE_MISMATCH_DETAIL}), and optional extensions:
   *
   * <ul>
   *   <li>{@code property} ({@code ViolationSupport#PROPERTY_EXTENSION}) - name of the parameter /
   *       property that failed conversion
   *   <li>{@code kind} ({@code ViolationSupport#KIND_EXTENSION}) - required target type in
   *       lowercase simple form
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
   * @return problem populated with status, detail and relevant extensions
   * @see io.github.problem4j.spring.web.parameter.ViolationSupport
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ProblemBuilder builder =
        Problem.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(formatDetail(TYPE_MISMATCH_DETAIL));

    TypeMismatchException e = (TypeMismatchException) ex;

    String property = e.getPropertyName();
    String kind = typeNameMapper.map(e.getRequiredType()).orElse(null);

    if (property != null) {
      builder = builder.extension(PROPERTY_EXTENSION, property);
    }
    if (kind != null) {
      builder = builder.extension(KIND_EXTENSION, kind);
    }
    return builder.build();
  }
}
