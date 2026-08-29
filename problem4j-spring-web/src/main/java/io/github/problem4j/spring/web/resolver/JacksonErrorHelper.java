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
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.TypeNameMapper;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Utility for Jackson exceptions used in situations where these exceptions are the {@code cause} of
 * the exception that is being resolved.
 */
final class JacksonErrorHelper {

  private ProblemFormat problemFormat;
  private TypeNameMapper typeNameMapper;

  JacksonErrorHelper(ProblemFormat problemFormat, TypeNameMapper typeNameMapper) {
    this.problemFormat = problemFormat;
    this.typeNameMapper = typeNameMapper;
  }

  Problem resolveMismatchedInput(MismatchedInputException e) {
    Optional<String> optionalProperty = resolvePropertyPath(e);

    ProblemBuilder builder = Problem.builder().status(HttpStatus.BAD_REQUEST.value());

    optionalProperty.ifPresent(
        property -> {
          String kind = typeNameMapper.map(e.getTargetType()).orElse(null);

          builder.detail(problemFormat.formatDetail(TYPE_MISMATCH_DETAIL));
          builder.extension(PROPERTY_EXTENSION, property);
          builder.extension(KIND_EXTENSION, kind);
        });

    return builder.build();
  }

  private Optional<String> resolvePropertyPath(MismatchedInputException e) {
    String property =
        e.getPath().stream()
            .map(JacksonException.Reference::getPropertyName)
            .filter(StringUtils::hasLength)
            .collect(Collectors.joining("."));

    return StringUtils.hasLength(property) ? Optional.of(property) : Optional.empty();
  }

  void setProblemFormat(ProblemFormat problemFormat) {
    this.problemFormat = problemFormat;
  }

  void setTypeNameMapper(TypeNameMapper typeNameMapper) {
    this.typeNameMapper = typeNameMapper;
  }
}
