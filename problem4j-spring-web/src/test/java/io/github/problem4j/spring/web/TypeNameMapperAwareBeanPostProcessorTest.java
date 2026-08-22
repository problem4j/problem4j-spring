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

package io.github.problem4j.spring.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class TypeNameMapperAwareBeanPostProcessorTest {

  @Test
  void givenTypeNameMapperAwareBean_whenPostProcess_thenSetsTypeNameMapper() {
    TypeNameMapper mapper = type -> Optional.of("mapped");
    TypeNameMapperAwareBeanPostProcessor processor =
        new TypeNameMapperAwareBeanPostProcessor(objectProvider(mapper));
    StubTypeNameMapperAware bean = new StubTypeNameMapperAware();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
    assertThat(bean.typeNameMapper).isSameAs(mapper);
  }

  @Test
  void givenNonAwareBean_whenPostProcess_thenReturnsBeanUnchanged() {
    TypeNameMapperAwareBeanPostProcessor processor =
        new TypeNameMapperAwareBeanPostProcessor(objectProvider(new SimpleTypeNameMapper()));
    Object bean = new Object();

    Object result = processor.postProcessBeforeInitialization(bean, "bean");

    assertThat(result).isSameAs(bean);
  }

  private static ObjectProvider<TypeNameMapper> objectProvider(TypeNameMapper mapper) {
    return new ObjectProvider<>() {
      @Override
      public TypeNameMapper getObject() {
        return mapper;
      }
    };
  }

  private static final class StubTypeNameMapperAware implements TypeNameMapperAware {

    private @Nullable TypeNameMapper typeNameMapper;

    @Override
    public void setTypeNameMapper(TypeNameMapper typeNameMapper) {
      this.typeNameMapper = typeNameMapper;
    }
  }
}
