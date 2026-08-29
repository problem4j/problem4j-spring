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

package io.github.problem4j.spring.webflux.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webflux.autoconfigure.ProblemWebFluxProperties.ErrorWebExceptionHandler;
import io.github.problem4j.spring.webflux.autoconfigure.ProblemWebFluxProperties.ExceptionAdvice;
import io.github.problem4j.spring.webflux.autoconfigure.ProblemWebFluxProperties.ExceptionHandler;
import io.github.problem4j.spring.webflux.autoconfigure.ProblemWebFluxProperties.ProblemContextFilter;
import io.github.problem4j.spring.webflux.autoconfigure.ProblemWebFluxProperties.ProblemExceptionAdvice;
import org.junit.jupiter.api.Test;

// These tests exist only to play with getters/setters and the deprecated constructors so they are
// not reported as uncovered while playing with JaCoCo test coverage.
@SuppressWarnings("removal")
class ProblemWebFluxPropertiesTest {

  @Test
  void givenNoArgConstructor_whenCreated_thenDefaultsApplied() {
    ProblemWebFluxProperties properties = new ProblemWebFluxProperties();

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getExceptionAdvice().isEnabled())
        .isEqualTo(ExceptionAdvice.DEFAULT_ENABLED);
    assertThat(properties.getProblemExceptionAdvice().isEnabled())
        .isEqualTo(ProblemExceptionAdvice.DEFAULT_ENABLED);
    assertThat(properties.getProblemContextFilter().isEnabled())
        .isEqualTo(ProblemContextFilter.DEFAULT_ENABLED);
    assertThat(properties.getExceptionHandler().isEnabled())
        .isEqualTo(ExceptionHandler.DEFAULT_ENABLED);
    assertThat(properties.getErrorWebExceptionHandler().isEnabled())
        .isEqualTo(ErrorWebExceptionHandler.DEFAULT_ENABLED);
  }

  @Test
  void givenSetters_whenInvoked_thenValuesRoundTrip() {
    ProblemWebFluxProperties properties = new ProblemWebFluxProperties();
    ExceptionAdvice exceptionAdvice = new ExceptionAdvice();
    ProblemExceptionAdvice problemExceptionAdvice = new ProblemExceptionAdvice();
    ProblemContextFilter problemContextFilter = new ProblemContextFilter();
    ExceptionHandler exceptionHandler = new ExceptionHandler();
    ErrorWebExceptionHandler errorWebExceptionHandler = new ErrorWebExceptionHandler();

    properties.setEnabled(false);
    properties.setExceptionAdvice(exceptionAdvice);
    properties.setProblemExceptionAdvice(problemExceptionAdvice);
    properties.setProblemContextFilter(problemContextFilter);
    properties.setExceptionHandler(exceptionHandler);
    properties.setErrorWebExceptionHandler(errorWebExceptionHandler);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getExceptionAdvice()).isSameAs(exceptionAdvice);
    assertThat(properties.getProblemExceptionAdvice()).isSameAs(problemExceptionAdvice);
    assertThat(properties.getProblemContextFilter()).isSameAs(problemContextFilter);
    assertThat(properties.getExceptionHandler()).isSameAs(exceptionHandler);
    assertThat(properties.getErrorWebExceptionHandler()).isSameAs(errorWebExceptionHandler);
  }

  @Test
  void givenDeprecatedConstructor_whenAllArgsProvided_thenValuesApplied() {
    ExceptionAdvice exceptionAdvice = new ExceptionAdvice(false);
    ProblemExceptionAdvice problemExceptionAdvice = new ProblemExceptionAdvice(false);
    ProblemContextFilter problemContextFilter = new ProblemContextFilter(false);
    ExceptionHandler exceptionHandler = new ExceptionHandler(false);
    ErrorWebExceptionHandler errorWebExceptionHandler = new ErrorWebExceptionHandler(false);

    ProblemWebFluxProperties properties =
        new ProblemWebFluxProperties(
            false,
            exceptionAdvice,
            problemExceptionAdvice,
            problemContextFilter,
            exceptionHandler,
            errorWebExceptionHandler);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getExceptionAdvice()).isSameAs(exceptionAdvice);
    assertThat(properties.getProblemExceptionAdvice()).isSameAs(problemExceptionAdvice);
    assertThat(properties.getProblemContextFilter()).isSameAs(problemContextFilter);
    assertThat(properties.getExceptionHandler()).isSameAs(exceptionHandler);
    assertThat(properties.getErrorWebExceptionHandler()).isSameAs(errorWebExceptionHandler);
    assertThat(properties.getExceptionAdvice().isEnabled()).isFalse();
    assertThat(properties.getProblemExceptionAdvice().isEnabled()).isFalse();
    assertThat(properties.getProblemContextFilter().isEnabled()).isFalse();
    assertThat(properties.getExceptionHandler().isEnabled()).isFalse();
    assertThat(properties.getErrorWebExceptionHandler().isEnabled()).isFalse();
  }

  @Test
  void givenDeprecatedConstructor_whenNestedGroupsNull_thenDefaultsRetained() {
    ProblemWebFluxProperties properties =
        new ProblemWebFluxProperties(true, null, null, null, null, null);

    assertThat(properties.getExceptionAdvice().isEnabled())
        .isEqualTo(ExceptionAdvice.DEFAULT_ENABLED);
    assertThat(properties.getProblemExceptionAdvice().isEnabled())
        .isEqualTo(ProblemExceptionAdvice.DEFAULT_ENABLED);
    assertThat(properties.getProblemContextFilter().isEnabled())
        .isEqualTo(ProblemContextFilter.DEFAULT_ENABLED);
    assertThat(properties.getExceptionHandler().isEnabled())
        .isEqualTo(ExceptionHandler.DEFAULT_ENABLED);
    assertThat(properties.getErrorWebExceptionHandler().isEnabled())
        .isEqualTo(ErrorWebExceptionHandler.DEFAULT_ENABLED);
  }

  @Test
  void givenNestedGroups_whenSetterInvoked_thenValueRoundTrips() {
    ExceptionAdvice exceptionAdvice = new ExceptionAdvice();
    ProblemExceptionAdvice problemExceptionAdvice = new ProblemExceptionAdvice();
    ProblemContextFilter problemContextFilter = new ProblemContextFilter();
    ExceptionHandler exceptionHandler = new ExceptionHandler();
    ErrorWebExceptionHandler errorWebExceptionHandler = new ErrorWebExceptionHandler();

    exceptionAdvice.setEnabled(false);
    problemExceptionAdvice.setEnabled(false);
    problemContextFilter.setEnabled(false);
    exceptionHandler.setEnabled(false);
    errorWebExceptionHandler.setEnabled(false);

    assertThat(exceptionAdvice.isEnabled()).isFalse();
    assertThat(problemExceptionAdvice.isEnabled()).isFalse();
    assertThat(problemContextFilter.isEnabled()).isFalse();
    assertThat(exceptionHandler.isEnabled()).isFalse();
    assertThat(errorWebExceptionHandler.isEnabled()).isFalse();
  }

  @Test
  void givenNestedGroups_whenDeprecatedConstructorUsed_thenValueApplied() {
    assertThat(new ExceptionAdvice(false).isEnabled()).isFalse();
    assertThat(new ProblemExceptionAdvice(false).isEnabled()).isFalse();
    assertThat(new ProblemContextFilter(false).isEnabled()).isFalse();
    assertThat(new ExceptionHandler(false).isEnabled()).isFalse();
    assertThat(new ErrorWebExceptionHandler(false).isEnabled()).isFalse();
  }
}
