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

import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemFormatAware;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapperAware;
import io.github.problem4j.spring.web.app.TestApp;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.BindingResultSupportAware;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupportAware;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupportAware;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = {TestApp.class})
@Import(ProblemBeanPostProcessorIntegrationTest.AllAwareConfiguration.class)
class ProblemBeanPostProcessorIntegrationTest {

  @Autowired private AllAwareComponent component;

  @Autowired private ProblemFormat problemFormat;
  @Autowired private TypeNameMapper typeNameMapper;
  @Autowired private BindingResultSupport bindingResultSupport;
  @Autowired private MethodValidationResultSupport methodValidationResultSupport;
  @Autowired private MethodParameterSupport methodParameterSupport;

  @Test
  void
      givenComponentImplementingEveryAwareInterface_whenContextStarts_thenEveryCollaboratorIsInjected() {
    assertThat(component.problemFormat).isSameAs(problemFormat);
    assertThat(component.typeNameMapper).isSameAs(typeNameMapper);
    assertThat(component.bindingResultSupport).isSameAs(bindingResultSupport);
    assertThat(component.methodValidationResultSupport).isSameAs(methodValidationResultSupport);
    assertThat(component.methodParameterSupport).isSameAs(methodParameterSupport);
  }

  @TestConfiguration
  static class AllAwareConfiguration {

    @Bean
    AllAwareComponent allAwareComponent() {
      return new AllAwareComponent();
    }
  }

  @NullMarked
  static final class AllAwareComponent
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
