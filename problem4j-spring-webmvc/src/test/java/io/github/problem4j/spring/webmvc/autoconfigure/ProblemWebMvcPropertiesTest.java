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

package io.github.problem4j.spring.webmvc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webmvc.autoconfigure.ProblemWebMvcProperties.ErrorController;
import io.github.problem4j.spring.webmvc.autoconfigure.ProblemWebMvcProperties.ExceptionAdvice;
import io.github.problem4j.spring.webmvc.autoconfigure.ProblemWebMvcProperties.ExceptionHandler;
import io.github.problem4j.spring.webmvc.autoconfigure.ProblemWebMvcProperties.ProblemContextFilter;
import io.github.problem4j.spring.webmvc.autoconfigure.ProblemWebMvcProperties.ProblemExceptionAdvice;
import org.junit.jupiter.api.Test;

// These tests exist only to play with getters/setters and the deprecated constructors so they are
// not reported as uncovered while playing with JaCoCo test coverage.
@SuppressWarnings("removal")
class ProblemWebMvcPropertiesTest {

  @Test
  void givenNoArgConstructor_whenCreated_thenDefaultsApplied() {
    ProblemWebMvcProperties properties = new ProblemWebMvcProperties();

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getExceptionAdvice().isEnabled())
        .isEqualTo(ExceptionAdvice.DEFAULT_ENABLED);
    assertThat(properties.getProblemExceptionAdvice().isEnabled())
        .isEqualTo(ProblemExceptionAdvice.DEFAULT_ENABLED);
    assertThat(properties.getProblemContextFilter().isEnabled())
        .isEqualTo(ProblemContextFilter.DEFAULT_ENABLED);
    assertThat(properties.getExceptionHandler().isEnabled())
        .isEqualTo(ExceptionHandler.DEFAULT_ENABLED);
    assertThat(properties.getErrorController().isEnabled())
        .isEqualTo(ErrorController.DEFAULT_ENABLED);
  }

  @Test
  void givenSetters_whenInvoked_thenValuesRoundTrip() {
    ProblemWebMvcProperties properties = new ProblemWebMvcProperties();
    ExceptionAdvice exceptionAdvice = new ExceptionAdvice();
    ProblemExceptionAdvice problemExceptionAdvice = new ProblemExceptionAdvice();
    ProblemContextFilter problemContextFilter = new ProblemContextFilter();
    ExceptionHandler exceptionHandler = new ExceptionHandler();
    ErrorController errorController = new ErrorController();

    properties.setEnabled(false);
    properties.setExceptionAdvice(exceptionAdvice);
    properties.setProblemExceptionAdvice(problemExceptionAdvice);
    properties.setProblemContextFilter(problemContextFilter);
    properties.setExceptionHandler(exceptionHandler);
    properties.setErrorController(errorController);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getExceptionAdvice()).isSameAs(exceptionAdvice);
    assertThat(properties.getProblemExceptionAdvice()).isSameAs(problemExceptionAdvice);
    assertThat(properties.getProblemContextFilter()).isSameAs(problemContextFilter);
    assertThat(properties.getExceptionHandler()).isSameAs(exceptionHandler);
    assertThat(properties.getErrorController()).isSameAs(errorController);
  }

  @Test
  void givenDeprecatedConstructor_whenAllArgsProvided_thenValuesApplied() {
    ExceptionAdvice exceptionAdvice = new ExceptionAdvice(false);
    ProblemExceptionAdvice problemExceptionAdvice = new ProblemExceptionAdvice(false);
    ProblemContextFilter problemContextFilter = new ProblemContextFilter(false);
    ExceptionHandler exceptionHandler = new ExceptionHandler(false);
    ErrorController errorController = new ErrorController(false);

    ProblemWebMvcProperties properties =
        new ProblemWebMvcProperties(
            false,
            exceptionAdvice,
            problemExceptionAdvice,
            problemContextFilter,
            exceptionHandler,
            errorController);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getExceptionAdvice()).isSameAs(exceptionAdvice);
    assertThat(properties.getProblemExceptionAdvice()).isSameAs(problemExceptionAdvice);
    assertThat(properties.getProblemContextFilter()).isSameAs(problemContextFilter);
    assertThat(properties.getExceptionHandler()).isSameAs(exceptionHandler);
    assertThat(properties.getErrorController()).isSameAs(errorController);
    assertThat(properties.getExceptionAdvice().isEnabled()).isFalse();
    assertThat(properties.getProblemExceptionAdvice().isEnabled()).isFalse();
    assertThat(properties.getProblemContextFilter().isEnabled()).isFalse();
    assertThat(properties.getExceptionHandler().isEnabled()).isFalse();
    assertThat(properties.getErrorController().isEnabled()).isFalse();
  }

  @Test
  void givenDeprecatedConstructor_whenNestedGroupsNull_thenDefaultsRetained() {
    ProblemWebMvcProperties properties =
        new ProblemWebMvcProperties(true, null, null, null, null, null);

    assertThat(properties.getExceptionAdvice().isEnabled())
        .isEqualTo(ExceptionAdvice.DEFAULT_ENABLED);
    assertThat(properties.getProblemExceptionAdvice().isEnabled())
        .isEqualTo(ProblemExceptionAdvice.DEFAULT_ENABLED);
    assertThat(properties.getProblemContextFilter().isEnabled())
        .isEqualTo(ProblemContextFilter.DEFAULT_ENABLED);
    assertThat(properties.getExceptionHandler().isEnabled())
        .isEqualTo(ExceptionHandler.DEFAULT_ENABLED);
    assertThat(properties.getErrorController().isEnabled())
        .isEqualTo(ErrorController.DEFAULT_ENABLED);
  }

  @Test
  void givenNestedGroups_whenSetterInvoked_thenValueRoundTrips() {
    ExceptionAdvice exceptionAdvice = new ExceptionAdvice();
    ProblemExceptionAdvice problemExceptionAdvice = new ProblemExceptionAdvice();
    ProblemContextFilter problemContextFilter = new ProblemContextFilter();
    ExceptionHandler exceptionHandler = new ExceptionHandler();
    ErrorController errorController = new ErrorController();

    exceptionAdvice.setEnabled(false);
    problemExceptionAdvice.setEnabled(false);
    problemContextFilter.setEnabled(false);
    exceptionHandler.setEnabled(false);
    errorController.setEnabled(false);

    assertThat(exceptionAdvice.isEnabled()).isFalse();
    assertThat(problemExceptionAdvice.isEnabled()).isFalse();
    assertThat(problemContextFilter.isEnabled()).isFalse();
    assertThat(exceptionHandler.isEnabled()).isFalse();
    assertThat(errorController.isEnabled()).isFalse();
  }

  @Test
  void givenNestedGroups_whenDeprecatedConstructorUsed_thenValueApplied() {
    assertThat(new ExceptionAdvice(false).isEnabled()).isFalse();
    assertThat(new ProblemExceptionAdvice(false).isEnabled()).isFalse();
    assertThat(new ProblemContextFilter(false).isEnabled()).isFalse();
    assertThat(new ExceptionHandler(false).isEnabled()).isFalse();
    assertThat(new ErrorController(false).isEnabled()).isFalse();
  }
}
