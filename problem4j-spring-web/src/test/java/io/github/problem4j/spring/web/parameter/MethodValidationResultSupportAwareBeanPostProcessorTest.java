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

package io.github.problem4j.spring.web.parameter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class MethodValidationResultSupportAwareBeanPostProcessorTest {

  @Test
  void givenMethodValidationResultSupportAwareBean_whenPostProcess_thenSetsSupport() {
    MethodValidationResultSupport support = result -> List.of();
    MethodValidationResultSupportAwareBeanPostProcessor processor =
        new MethodValidationResultSupportAwareBeanPostProcessor(objectProvider(support));
    StubMethodValidationResultSupportAware bean = new StubMethodValidationResultSupportAware();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.methodValidationResultSupport).isSameAs(support);
  }

  @Test
  void givenNonAwareBean_whenPostProcess_thenReturnsBeanUnchanged() {
    MethodValidationResultSupportAwareBeanPostProcessor processor =
        new MethodValidationResultSupportAwareBeanPostProcessor(
            objectProvider(result -> List.of()));
    Object bean = new Object();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
  }

  private static ObjectProvider<MethodValidationResultSupport> objectProvider(
      MethodValidationResultSupport support) {
    return new ObjectProvider<>() {
      @Override
      public MethodValidationResultSupport getObject() {
        return support;
      }
    };
  }

  private static final class StubMethodValidationResultSupportAware
      implements MethodValidationResultSupportAware {

    private @Nullable MethodValidationResultSupport methodValidationResultSupport;

    @Override
    public void setMethodValidationResultSupport(
        MethodValidationResultSupport methodValidationResultSupport) {
      this.methodValidationResultSupport = methodValidationResultSupport;
    }
  }
}
