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

package io.github.problem4j.spring.web.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemFormatAware;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapperAware;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.BindingResultSupportAware;
import io.github.problem4j.spring.web.parameter.DefaultBindingResultSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupportAware;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupportAware;
import io.github.problem4j.spring.web.parameter.Violation;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.method.MethodValidationResult;

class ProblemBeanPostProcessorTest {

  private final ProblemFormat problemFormat =
      detail -> detail == null ? null : detail.toUpperCase();
  private final TypeNameMapper typeNameMapper = type -> Optional.of("mapped");
  private final BindingResultSupport bindingResultSupport = new DefaultBindingResultSupport();
  private final MethodValidationResultSupport methodValidationResultSupport = result -> List.of();
  private final MethodParameterSupport methodParameterSupport = parameter -> Optional.empty();

  private final ProblemBeanPostProcessor processor =
      new ProblemBeanPostProcessor(
          objectProvider(problemFormat),
          objectProvider(typeNameMapper),
          objectProvider(bindingResultSupport),
          objectProvider(methodValidationResultSupport),
          objectProvider(methodParameterSupport));

  @Test
  void givenBeanImplementingEveryAwareInterface_whenPostProcess_thenInjectsEveryCollaborator() {
    StubAware bean = new StubAware();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.problemFormat).isSameAs(problemFormat);
    assertThat(bean.typeNameMapper).isSameAs(typeNameMapper);
    assertThat(bean.bindingResultSupport).isSameAs(bindingResultSupport);
    assertThat(bean.methodValidationResultSupport).isSameAs(methodValidationResultSupport);
    assertThat(bean.methodParameterSupport).isSameAs(methodParameterSupport);
  }

  @Test
  void
      givenBeanImplementingOnlyOneAwareInterface_whenPostProcess_thenInjectsOnlyThatCollaborator() {
    StubProblemFormatAware bean = new StubProblemFormatAware();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.problemFormat).isSameAs(problemFormat);
  }

  @Test
  void givenNonAwareBean_whenPostProcess_thenReturnsBeanUnchanged() {
    Object bean = new Object();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
  }

  @Test
  void givenAwareBeanForAbsentCollaborator_whenPostProcess_thenProviderForPresentOnesUntouched() {
    ProblemBeanPostProcessor partial =
        new ProblemBeanPostProcessor(
            objectProvider(problemFormat),
            failingProvider(),
            failingProvider(),
            failingProvider(),
            failingProvider());
    StubProblemFormatAware bean = new StubProblemFormatAware();

    partial.postProcessBeforeInitialization(bean, "bean");

    assertThat(bean.problemFormat).isSameAs(problemFormat);
  }

  @Test
  void givenAllCollaboratorProvidersEmpty_whenPostProcess_thenNoCollaboratorInjected() {
    ProblemBeanPostProcessor empty =
        new ProblemBeanPostProcessor(
            emptyProvider(), emptyProvider(), emptyProvider(), emptyProvider(), emptyProvider());
    StubAware bean = new StubAware();

    Object result = empty.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.problemFormat).isNull();
    assertThat(bean.typeNameMapper).isNull();
    assertThat(bean.bindingResultSupport).isNull();
    assertThat(bean.methodValidationResultSupport).isNull();
    assertThat(bean.methodParameterSupport).isNull();
  }

  @Nested
  class DebugLogging {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ProblemBeanPostProcessor.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private final ProblemBeanPostProcessor debugProcessor =
        new ProblemBeanPostProcessor(
            objectProvider(new NamedProblemFormat()),
            objectProvider(new NamedTypeNameMapper()),
            objectProvider(new NamedBindingResultSupport()),
            objectProvider(new NamedMethodValidationResultSupport()),
            objectProvider(new NamedMethodParameterSupport()));

    @BeforeEach
    void attachAppender() {
      appender.start();
      logger.addAppender(appender);
      logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void detachAppender() {
      logger.setLevel(null);
      logger.detachAppender(appender);
      appender.stop();
    }

    @Test
    void givenSingleCollaborator_whenPostProcess_thenLogsSingleName() {
      debugProcessor.postProcessBeforeInitialization(new StubProblemFormatAware(), "bean");

      assertThat(appender.list)
          .singleElement()
          .extracting(ILoggingEvent::getFormattedMessage)
          .isEqualTo("Enhanced bean bean with NamedProblemFormat");
    }

    @Test
    void givenTwoCollaborators_whenPostProcess_thenJoinsNamesWithAnd() {
      debugProcessor.postProcessBeforeInitialization(
          new StubProblemFormatAndTypeNameAware(), "bean");

      assertThat(appender.list)
          .singleElement()
          .extracting(ILoggingEvent::getFormattedMessage)
          .isEqualTo("Enhanced bean bean with NamedProblemFormat and NamedTypeNameMapper");
    }

    @Test
    void givenThreeOrMoreCollaborators_whenPostProcess_thenJoinsNamesWithCommasAndTrailingAnd() {
      debugProcessor.postProcessBeforeInitialization(new StubAware(), "bean");

      assertThat(appender.list)
          .singleElement()
          .extracting(ILoggingEvent::getFormattedMessage)
          .isEqualTo(
              "Enhanced bean bean with NamedProblemFormat, NamedTypeNameMapper,"
                  + " NamedBindingResultSupport, NamedMethodValidationResultSupport and"
                  + " NamedMethodParameterSupport");
    }

    @Test
    void givenNonAwareBean_whenPostProcess_thenLogsNothing() {
      debugProcessor.postProcessBeforeInitialization(new Object(), "bean");

      assertThat(appender.list).isEmpty();
    }
  }

  private static <T> ObjectProvider<T> objectProvider(T value) {
    return new ObjectProvider<>() {
      @Override
      public T getObject() {
        return value;
      }
    };
  }

  private static <T> ObjectProvider<T> failingProvider() {
    return new ObjectProvider<>() {
      @Override
      public T getObject() {
        throw new AssertionError("collaborator should not be queried");
      }
    };
  }

  private static <T> ObjectProvider<T> emptyProvider() {
    return new ObjectProvider<>() {
      @Override
      public T getObject() {
        throw new AssertionError("collaborator should not be queried");
      }

      @Override
      public @Nullable T getIfAvailable() {
        return null;
      }
    };
  }

  @NullMarked
  private static final class NamedProblemFormat implements ProblemFormat {
    @Override
    public @Nullable String formatDetail(@Nullable String detail) {
      return detail;
    }
  }

  @NullMarked
  private static final class NamedTypeNameMapper implements TypeNameMapper {
    @Override
    public Optional<String> map(@Nullable Class<?> type) {
      return Optional.empty();
    }
  }

  @NullMarked
  private static final class NamedBindingResultSupport implements BindingResultSupport {
    @Override
    public List<Violation> fetchViolations(BindingResult result) {
      return List.of();
    }
  }

  @NullMarked
  private static final class NamedMethodValidationResultSupport
      implements MethodValidationResultSupport {
    @Override
    public List<Violation> fetchViolations(MethodValidationResult result) {
      return List.of();
    }
  }

  @NullMarked
  private static final class NamedMethodParameterSupport implements MethodParameterSupport {
    @Override
    public Optional<String> findParameterName(@Nullable MethodParameter parameter) {
      return Optional.empty();
    }
  }

  @NullMarked
  private static final class StubProblemFormatAndTypeNameAware
      implements ProblemFormatAware, TypeNameMapperAware {

    private @Nullable ProblemFormat problemFormat;
    private @Nullable TypeNameMapper typeNameMapper;

    @Override
    public void setProblemFormat(ProblemFormat problemFormat) {
      this.problemFormat = problemFormat;
    }

    @Override
    public void setTypeNameMapper(TypeNameMapper typeNameMapper) {
      this.typeNameMapper = typeNameMapper;
    }
  }

  @NullMarked
  private static final class StubProblemFormatAware implements ProblemFormatAware {

    private @Nullable ProblemFormat problemFormat;

    @Override
    public void setProblemFormat(ProblemFormat problemFormat) {
      this.problemFormat = problemFormat;
    }
  }

  @NullMarked
  private static final class StubAware
      implements ProblemFormatAware,
          TypeNameMapperAware,
          BindingResultSupportAware,
          MethodValidationResultSupportAware,
          MethodParameterSupportAware {

    private @Nullable ProblemFormat problemFormat;
    private @Nullable TypeNameMapper typeNameMapper;
    private @Nullable BindingResultSupport bindingResultSupport;
    private @Nullable MethodValidationResultSupport methodValidationResultSupport;
    private @Nullable MethodParameterSupport methodParameterSupport;

    @Override
    public void setProblemFormat(ProblemFormat problemFormat) {
      this.problemFormat = problemFormat;
    }

    @Override
    public void setTypeNameMapper(TypeNameMapper typeNameMapper) {
      this.typeNameMapper = typeNameMapper;
    }

    @Override
    public void setBindingResultSupport(BindingResultSupport bindingResultSupport) {
      this.bindingResultSupport = bindingResultSupport;
    }

    @Override
    public void setMethodValidationResultSupport(
        MethodValidationResultSupport methodValidationResultSupport) {
      this.methodValidationResultSupport = methodValidationResultSupport;
    }

    @Override
    public void setMethodParameterSupport(MethodParameterSupport methodParameterSupport) {
      this.methodParameterSupport = methodParameterSupport;
    }
  }
}
