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
import io.github.problem4j.spring.web.TypeNameMapper;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

final class JacksonErrorHelper {

  static ProblemBuilder resolveMismatchedInput(
      MismatchedInputException e, TypeNameMapper typeNameMapper) {
    Optional<String> property = resolvePropertyPath(e);
    Optional<String> kind = typeNameMapper.map(e.getTargetType());

    ProblemBuilder builder = Problem.builder().status(HttpStatus.BAD_REQUEST.value());

    property.ifPresent(
        it -> {
          builder.detail(TYPE_MISMATCH_DETAIL);
          builder.extension(PROPERTY_EXTENSION, it);
          builder.extension(KIND_EXTENSION, kind.orElse(null));
        });

    return builder;
  }

  private static Optional<String> resolvePropertyPath(MismatchedInputException e) {
    String property =
        e.getPath().stream()
            .map(JacksonException.Reference::getPropertyName)
            .filter(StringUtils::hasLength)
            .collect(Collectors.joining("."));

    return StringUtils.hasLength(property) ? Optional.of(property) : Optional.empty();
  }

  private JacksonErrorHelper() {}
}
