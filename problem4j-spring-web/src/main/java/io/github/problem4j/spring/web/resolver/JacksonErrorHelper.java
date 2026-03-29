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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.TypeNameMapper;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * Utility for Jackson exceptions used in situations where these exceptions are the {@code cause} of
 * the exception that is being resolved.
 */
final class JacksonErrorHelper {

  private final ProblemFormat problemFormat;
  private final TypeNameMapper typeNameMapper;

  JacksonErrorHelper(ProblemFormat problemFormat, TypeNameMapper typeNameMapper) {
    this.problemFormat = problemFormat;
    this.typeNameMapper = typeNameMapper;
  }

  ProblemBuilder resolveMismatchedInput(MismatchedInputException e) {
    Optional<String> optionalProperty = resolvePropertyPath(e);

    ProblemBuilder builder = Problem.builder().status(HttpStatus.BAD_REQUEST.value());

    optionalProperty.ifPresent(
        property -> {
          String kind = typeNameMapper.map(e.getTargetType()).orElse(null);

          builder.detail(problemFormat.formatDetail(TYPE_MISMATCH_DETAIL));
          builder.extension(PROPERTY_EXTENSION, property);
          builder.extension(KIND_EXTENSION, kind);
        });

    return builder;
  }

  private Optional<String> resolvePropertyPath(MismatchedInputException e) {
    String property =
        e.getPath().stream()
            .map(JsonMappingException.Reference::getFieldName)
            .filter(StringUtils::hasLength)
            .collect(Collectors.joining("."));

    return StringUtils.hasLength(property) ? Optional.of(property) : Optional.empty();
  }
}
