/*
 * Copyright 2025-2026 The Problem4J Authors
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

import io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter;
import io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

class ProblemWebMvcAutoConfigurationTest {

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithEnabled {

    @Autowired(required = false)
    private ProblemWebMvcAutoConfiguration problemWebMvcAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired(required = false)
    private ProblemResolverWebMvcConfiguration problemResolverWebMvcConfiguration;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemWebMvcAutoConfiguration).isNotNull();
      assertThat(problemErrorMvcConfiguration).isNotNull();
      assertThat(problemResolverWebMvcConfiguration).isNotNull();

      assertThat(properties.isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.enabled=false"})
  @Nested
  class WithParentDisabled {

    @Autowired(required = false)
    private ProblemWebMvcAutoConfiguration problemWebMvcAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired(required = false)
    private ProblemResolverWebMvcConfiguration problemResolverWebMvcConfiguration;

    @Autowired(required = false)
    private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutConfiguration() {
      assertThat(problemWebMvcAutoConfiguration).isNull();
      assertThat(problemErrorMvcConfiguration).isNull();
      assertThat(problemResolverWebMvcConfiguration).isNull();

      assertThat(properties).isNull();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired(required = false)
    private ProblemWebMvcAutoConfiguration problemWebMvcAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired(required = false)
    private ProblemResolverWebMvcConfiguration problemResolverWebMvcConfiguration;

    @Autowired(required = false)
    private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutConfiguration() {
      assertThat(problemWebMvcAutoConfiguration).isNull();
      assertThat(problemErrorMvcConfiguration).isNull();
      assertThat(problemResolverWebMvcConfiguration).isNull();

      assertThat(properties).isNull();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithExceptionAdviceEnabled {

    @Autowired(required = false)
    private ExceptionWebMvcAdvice exceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionWebMvcAdvice).isNotNull();
      assertThat(properties.getExceptionAdvice().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.exception-advice.enabled=false"})
  @Nested
  class WithExceptionAdviceDisabled {

    @Autowired(required = false)
    private ExceptionWebMvcAdvice exceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionWebMvcAdvice).isNull();
      assertThat(properties.getExceptionAdvice().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithProblemExceptionAdviceEnabled {

    @Autowired(required = false)
    private ProblemExceptionWebMvcAdvice problemExceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemExceptionWebMvcAdvice).isNotNull();
      assertThat(properties.getProblemExceptionAdvice().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.problem-exception-advice.enabled=false"})
  @Nested
  class WithProblemExceptionAdviceDisabled {

    @Autowired(required = false)
    private ProblemExceptionWebMvcAdvice problemExceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemExceptionWebMvcAdvice).isNull();
      assertThat(properties.getProblemExceptionAdvice().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithProblemContextFilterEnabled {

    @Autowired(required = false)
    private ProblemContextWebMvcFilter problemContextWebMvcFilter;

    @Autowired(required = false)
    private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemContextWebMvcFilter).isNotNull();
      assertThat(properties.getProblemContextFilter().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.problem-context-filter.enabled=false"})
  @Nested
  class WithProblemContextFilterDisabled {

    @Autowired(required = false)
    private ProblemContextWebMvcFilter problemContextWebMvcFilter;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemContextWebMvcFilter).isNull();
      assertThat(properties.getProblemContextFilter().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithExceptionHandlerEnabled {

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionHandler).isNotNull();
      assertThat(properties.getExceptionHandler().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.exception-handler.enabled=false"})
  @Nested
  class WithExceptionHandlerDisabled {

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionHandler).isNull();
      assertThat(properties.getExceptionHandler().isEnabled()).isFalse();
    }
  }
}
