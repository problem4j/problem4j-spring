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
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;

final class TypeMismatchHelper {

  private ProblemFormat problemFormat;
  private TypeNameMapper typeNameMapper;

  TypeMismatchHelper(ProblemFormat problemFormat, TypeNameMapper typeNameMapper) {
    this.problemFormat = problemFormat;
    this.typeNameMapper = typeNameMapper;
  }

  Problem toProblem(TypeMismatchException e) {
    ProblemBuilder builder =
        Problem.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(problemFormat.formatDetail(TYPE_MISMATCH_DETAIL));

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

  void setProblemFormat(ProblemFormat problemFormat) {
    this.problemFormat = problemFormat;
  }

  void setTypeNameMapper(TypeNameMapper typeNameMapper) {
    this.typeNameMapper = typeNameMapper;
  }
}
