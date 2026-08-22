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

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class MethodParameterSupportAwareBeanPostProcessorTest {

  @Test
  void givenMethodParameterSupportAwareBean_whenPostProcess_thenSetsSupport() {
    MethodParameterSupport support = parameter -> Optional.empty();
    MethodParameterSupportAwareBeanPostProcessor processor =
        new MethodParameterSupportAwareBeanPostProcessor(objectProvider(support));
    StubMethodParameterSupportAware bean = new StubMethodParameterSupportAware();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.methodParameterSupport).isSameAs(support);
  }

  @Test
  void givenNonAwareBean_whenPostProcess_thenReturnsBeanUnchanged() {
    MethodParameterSupportAwareBeanPostProcessor processor =
        new MethodParameterSupportAwareBeanPostProcessor(
            objectProvider(parameter -> Optional.empty()));
    Object bean = new Object();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
  }

  private static ObjectProvider<MethodParameterSupport> objectProvider(
      MethodParameterSupport support) {
    return new ObjectProvider<>() {
      @Override
      public MethodParameterSupport getObject() {
        return support;
      }
    };
  }

  private static final class StubMethodParameterSupportAware
      implements MethodParameterSupportAware {

    private @Nullable MethodParameterSupport methodParameterSupport;

    @Override
    public void setMethodParameterSupport(MethodParameterSupport methodParameterSupport) {
      this.methodParameterSupport = methodParameterSupport;
    }
  }
}
