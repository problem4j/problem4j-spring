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

import io.github.problem4j.spring.webflux.ProblemErrorWebExceptionHandler;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;

class ProblemErrorWebFluxConfigurationTest {

  @SpringBootTest(classes = {WebFluxTestApp.class})
  @Nested
  class WithEnabled {

    @Autowired(required = false)
    private ProblemErrorWebFluxConfiguration problemErrorWebFluxConfiguration;

    @Autowired private ErrorWebExceptionHandler errorWebExceptionHandler;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemErrorWebFluxConfiguration).isNotNull();
      assertThat(errorWebExceptionHandler).isInstanceOf(ProblemErrorWebExceptionHandler.class);
      assertThat(properties.getErrorWebExceptionHandler().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      properties = {"problem4j.webflux.error-web-exception-handler.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired(required = false)
    private ProblemErrorWebFluxConfiguration problemErrorWebFluxConfiguration;

    @Autowired private ErrorWebExceptionHandler errorWebExceptionHandler;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemErrorWebFluxConfiguration).isNull();
      assertThat(errorWebExceptionHandler).isNotInstanceOf(ProblemErrorWebExceptionHandler.class);
      assertThat(properties.getErrorWebExceptionHandler().isEnabled()).isFalse();
    }
  }
}
