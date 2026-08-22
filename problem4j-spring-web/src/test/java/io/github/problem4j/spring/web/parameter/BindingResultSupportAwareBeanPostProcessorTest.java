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

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class BindingResultSupportAwareBeanPostProcessorTest {

  @Test
  void givenBindingResultSupportAwareBean_whenPostProcess_thenSetsBindingResultSupport() {
    BindingResultSupport support = new DefaultBindingResultSupport();
    BindingResultSupportAwareBeanPostProcessor processor =
        new BindingResultSupportAwareBeanPostProcessor(objectProvider(support));
    StubBindingResultSupportAware bean = new StubBindingResultSupportAware();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.bindingResultSupport).isSameAs(support);
  }

  @Test
  void givenNonAwareBean_whenPostProcess_thenReturnsBeanUnchanged() {
    BindingResultSupportAwareBeanPostProcessor processor =
        new BindingResultSupportAwareBeanPostProcessor(
            objectProvider(new DefaultBindingResultSupport()));
    Object bean = new Object();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
  }

  private static ObjectProvider<BindingResultSupport> objectProvider(BindingResultSupport support) {
    return new ObjectProvider<>() {
      @Override
      public BindingResultSupport getObject() {
        return support;
      }
    };
  }

  private static final class StubBindingResultSupportAware implements BindingResultSupportAware {

    private @Nullable BindingResultSupport bindingResultSupport;

    @Override
    public void setBindingResultSupport(BindingResultSupport bindingResultSupport) {
      this.bindingResultSupport = bindingResultSupport;
    }
  }
}
