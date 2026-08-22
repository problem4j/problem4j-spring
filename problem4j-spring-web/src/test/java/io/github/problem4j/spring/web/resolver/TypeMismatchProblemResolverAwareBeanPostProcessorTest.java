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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class TypeMismatchProblemResolverAwareBeanPostProcessorTest {

  @Test
  void
      givenTypeMismatchProblemResolverAwareBeanAndBeanAvailable_whenPostProcess_thenSetsResolver() {
    TypeMismatchProblemResolver resolver = new TypeMismatchProblemResolver();
    TypeMismatchProblemResolverAwareBeanPostProcessor processor =
        new TypeMismatchProblemResolverAwareBeanPostProcessor(objectProvider(resolver));
    StubTypeMismatchProblemResolverAware bean = new StubTypeMismatchProblemResolverAware();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.typeMismatchProblemResolver).isSameAs(resolver);
  }

  @Test
  void
      givenTypeMismatchProblemResolverAwareBeanAndNoBeanAvailable_whenPostProcess_thenLeavesUnset() {
    TypeMismatchProblemResolverAwareBeanPostProcessor processor =
        new TypeMismatchProblemResolverAwareBeanPostProcessor(objectProvider(null));
    StubTypeMismatchProblemResolverAware bean = new StubTypeMismatchProblemResolverAware();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.typeMismatchProblemResolver).isNull();
  }

  @Test
  void givenNonAwareBean_whenPostProcess_thenReturnsBeanUnchanged() {
    TypeMismatchProblemResolverAwareBeanPostProcessor processor =
        new TypeMismatchProblemResolverAwareBeanPostProcessor(
            objectProvider(new TypeMismatchProblemResolver()));
    Object bean = new Object();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
  }

  private static ObjectProvider<TypeMismatchProblemResolver> objectProvider(
      @Nullable TypeMismatchProblemResolver resolver) {
    return new ObjectProvider<>() {
      @Override
      public @Nullable TypeMismatchProblemResolver getIfAvailable() {
        return resolver;
      }

      @Override
      public TypeMismatchProblemResolver getObject() {
        if (resolver == null) {
          throw new IllegalStateException("no resolver available");
        }
        return resolver;
      }
    };
  }

  private static final class StubTypeMismatchProblemResolverAware
      implements TypeMismatchProblemResolverAware {

    private @Nullable TypeMismatchProblemResolver typeMismatchProblemResolver;

    @Override
    public void setTypeMismatchProblemResolver(
        @NonNull TypeMismatchProblemResolver typeMismatchProblemResolver) {
      this.typeMismatchProblemResolver = typeMismatchProblemResolver;
    }
  }
}
