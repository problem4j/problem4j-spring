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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class MaxUploadSizeExceededProblemResolverTest {

  private MaxUploadSizeExceededProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new MaxUploadSizeExceededProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsMaxUploadSizeExceededException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(MaxUploadSizeExceededException.class);
  }

  @Test
  void givenExceptionWithMaxSize_whenResolve_thenReturnsContentTooLargeWithMaxExtension() {
    MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024L);

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE.value());
    assertThat(problem.getDetail()).isEqualTo("Max upload size exceeded");
    assertThat(problem.getExtensions()).containsEntry("max", 1024L);
  }

  @Test
  void
      givenExceptionWithNegativeMaxSize_whenResolve_thenReturnsContentTooLargeWithoutMaxExtension() {
    MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(-1L);

    Problem problem =
        resolver.resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE.value());
    assertThat(problem.getDetail()).isEqualTo("Max upload size exceeded");
    assertThat(problem.getExtensions()).doesNotContainKey("max");
  }
}
